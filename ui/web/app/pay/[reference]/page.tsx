"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { getAuthSession } from "../../../lib/auth-session";
import { walletApi, type PaymentLinkResponse, type WalletResponse } from "../../../lib/wallet-api";

export default function CollectionCheckout() {
  const { reference } = useParams<{ reference: string }>();
  const [link, setLink] = useState<PaymentLinkResponse | null>(null);
  const [wallets, setWallets] = useState<WalletResponse[]>([]);
  const [walletId, setWalletId] = useState<number | undefined>();
  const [busy, setBusy] = useState(true);
  const [signedIn, setSignedIn] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!getAuthSession()?.accessToken) { setBusy(false); return; }
    setSignedIn(true);
    let active = true;
    Promise.all([walletApi.getPaymentLink(reference), walletApi.listWallets()])
      .then(([collection, available]) => {
        if (!active) return;
        const choices = available.filter(w => w.currency === collection.currency && w.status === "ACTIVE");
        setLink(collection); setWallets(choices); setWalletId(choices[0]?.id);
      })
      .catch(e => { if (active) setError(e instanceof Error ? e.message : "Could not load payment"); })
      .finally(() => { if (active) setBusy(false); });
    return () => { active = false; };
  }, [reference]);

  async function pay() {
    if (!walletId) return;
    setBusy(true); setError(null);
    try { setLink(await walletApi.payPaymentLink(reference, walletId)); }
    catch (e) { setError(e instanceof Error ? e.message : "Payment failed. Refresh before retrying."); }
    finally { setBusy(false); }
  }

  async function refresh() {
    setBusy(true); setError(null);
    try { setLink(await walletApi.getPaymentLink(reference)); }
    catch (e) { setError(e instanceof Error ? e.message : "Could not refresh payment"); }
    finally { setBusy(false); }
  }

  return <main className="min-h-screen bg-slate-50 px-6 py-16 text-slate-900">
    <section className="mx-auto max-w-lg space-y-6 rounded-3xl bg-white p-8 shadow-sm">
      <Link href="/app" className="font-bold text-blue-800">Sutura</Link>
      <h1 className="text-2xl font-bold">Pay a merchant</h1>
      {!signedIn && !busy ? <p><Link className="text-blue-800 underline" href="/login">Sign in</Link>, then return to this payment link.</p> : null}
      {busy ? <p role="status">Loading…</p> : null}
      {error ? <p role="alert" className="text-red-700">{error}</p> : null}
      {link ? <>
        <p className="text-lg font-semibold">{link.merchantName}</p>
        <p className="text-3xl font-bold">{link.currency} {Number(link.amount).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 4 })}</p>
        <p>{link.description}</p>
        <p role="status">{({ ACTIVE: "Ready to pay", REVIEW: "Awaiting review. No money has moved.", PAID: "Payment completed", FAILED: "Payment declined", EXPIRED: "This payment link has expired", REFUNDED: "Payment refunded" } as Record<string, string>)[link.status] ?? link.status}</p>
        {link.status === "ACTIVE" ? <>
          <label className="block">Pay from
            <select className="mt-2 block w-full rounded-xl border p-3" value={walletId ?? ""} onChange={e => setWalletId(Number(e.target.value))} disabled={busy}>
              {wallets.map(w => <option key={w.id} value={w.id}>{w.name} · {w.currency}</option>)}
            </select>
          </label>
          {!wallets.length ? <p>No active wallet matches this payment’s currency.</p> : null}
          <button className="w-full rounded-xl bg-blue-800 p-3 font-bold text-white disabled:opacity-50" disabled={busy || !walletId} onClick={pay}>Confirm payment</button>
        </> : null}
        <button className="text-blue-800 underline disabled:opacity-50" disabled={busy} onClick={refresh}>Refresh payment status</button>
      </> : null}
    </section>
  </main>;
}
