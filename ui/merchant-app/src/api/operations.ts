import { authFetch, readAuthJson } from "./sessionAuth";
export type Wallet = {
    id: number;
    name: string;
    currency: string;
    status: string;
    balance?: number;
};
export type Collection = {
    id: number;
    reference: string;
    merchantName?: string;
    walletId: number;
    amount: number;
    currency: string;
    status: string;
    description?: string;
    expiresAt?: string;
    transactionReference?: string;
};
export type Payment = {
    id: number;
    reference: string;
    type: string;
    status: string;
    amount: number;
    currency: string;
    description?: string;
    sourceWalletId?: number;
    destinationWalletId?: number;
    createdAt: string;
};
async function request<T>(path: string, init?: RequestInit) { return readAuthJson<T>(await authFetch(path, init)); }
export const merchantApi = {
    wallets: () => request<Wallet[]>("/api/v1/wallets"),
    balance: (id: number) => request<{
        balance: number;
    }>(`/api/v1/wallets/${id}/balance`),
    createWallet: (name: string, currency: string) => request<Wallet>("/api/v1/wallets", { method: "POST", body: JSON.stringify({ name, currency }) }),
    collections: () => request<Collection[]>("/api/v1/payment-links"),
    createCollection: (payload: {
        walletId: number;
        amount: string;
        currency: string;
        description: string;
        expiresDays: number;
    }) => request<Collection>("/api/v1/payment-links", { method: "POST", body: JSON.stringify(payload) }),
    collection: (reference: string) => request<Collection>(`/api/v1/payment-links/ref/${encodeURIComponent(reference)}`),
    transactions: () => request<Payment[]>("/api/v1/transactions"),
};
