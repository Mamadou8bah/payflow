import AsyncStorage from "@react-native-async-storage/async-storage";
import {useAutoRefresh} from "../hooks/useAutoRefresh";
import { createContext, useCallback, useContext, useEffect, useRef, useState, type ReactNode } from "react";
import { merchantApi, type Wallet, type Collection, type Payment } from "../api/operations";
import { useApp } from "./AppContext";
type Data = {
    wallets: Wallet[];
    collections: Collection[];
    transactions: Payment[];
};
const empty: Data = { wallets: [], collections: [], transactions: [] };
const Context = createContext<(Data & {
    loading: boolean;
    error: string;
    refresh: () => Promise<void>;
    selectedId: number;
    selectWallet: (id: number) => void;
    hidden: boolean;
    setHidden: (v: boolean) => void;
}) | null>(null);
export function MerchantProvider({ children }: {
    children: ReactNode;
}) {
    const { session } = useApp();
    const [data, setData] = useState<Data>(empty);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [selectedId, selectWallet] = useState(0);
    const [hidden, setHidden] = useState(false);
    const generation = useRef(0);
    useEffect(()=>{let active=true;void AsyncStorage.getItem("sutura-merchant_hide-balances").then(value=>{if(active)setHidden(value==="true");}).catch(()=>{});return()=>{active=false;};},[]);
    const changeHidden=(value:boolean)=>{setHidden(value);void AsyncStorage.setItem("sutura-merchant_hide-balances",String(value)).catch(()=>setError("Could not save your privacy preference."));};
    const refresh = useCallback(async () => { if (!session || session.userStatus !== "ACTIVE")
        return; const id = ++generation.current; setLoading(true); setError(""); try {
        const [wallets, collections, transactions] = await Promise.all([merchantApi.wallets(), merchantApi.collections(), merchantApi.transactions()]);
        const balances = await Promise.all(wallets.map(async (wallet) => ({ ...wallet, balance: Number((await merchantApi.balance(wallet.id)).balance) })));
        if (id !== generation.current)
            return;
        setData({ wallets: balances, collections, transactions });
        selectWallet(current => balances.some(w => w.id === current) ? current : balances.find(w => w.status === "ACTIVE")?.id ?? balances[0]?.id ?? 0);
    }
    catch (e) {
        if (id === generation.current)
            setError(e instanceof Error ? e.message : "Could not load merchant data.");
    }
    finally {
        if (id === generation.current)
            setLoading(false);
    } }, [session?.accessToken, session?.userStatus]);
    useEffect(() => { generation.current++; setData(empty); selectWallet(0); if (session)
        void refresh(); return () => { generation.current++; }; }, [refresh]);
    useAutoRefresh(refresh,session?.userStatus==="ACTIVE");
    return <Context.Provider value={{ ...data, loading, error, refresh, selectedId, selectWallet, hidden, setHidden:changeHidden }}>{children}</Context.Provider>;
}
export function useMerchant() { const value = useContext(Context); if (!value)
    throw new Error("MerchantProvider required"); return value; }
