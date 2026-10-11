"use client";
import {getDemoSession} from "../../../lib/demo-auth";
import {getAuthSession} from "../../../lib/auth-session";
import { useCallback, useEffect, useState } from "react";
import { authenticatedFetch, readAuthenticatedJson } from "../../../lib/authenticated-fetch";
import { inputClass, PanelCard, PrimaryButton, SecondaryButton } from "../../dashboard/dashboard-shell";

type Ticket = { id: number; userId: number; subject: string; message: string; status: string; staffReply: string | null; version: number };
export function SupportDesk() {
  const demo=!!getDemoSession()&&!getAuthSession();
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [drafts, setDrafts] = useState<Record<number, string>>({});
  const [page, setPage] = useState(0);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const load = useCallback(async () => {
    if(demo){setTickets(current=>current.length?current:[{id:101,userId:12,subject:"Cash deposit pending",message:"My cash deposit is waiting for agent confirmation.",status:"OPEN",staffReply:null,version:0},{id:102,userId:19,subject:"Merchant collection",message:"How do I check whether a customer payment has completed?",status:"OPEN",staffReply:null,version:0}]);return;}
    const response = await authenticatedFetch(`/api/v1/support/admin?page=${page}`);
    setTickets(await readAuthenticatedJson<Ticket[]>(response));
  }, [page,demo]);
  useEffect(() => { load().catch((err) => setError(err instanceof Error ? err.message : "Could not load tickets")); }, [load]);
  async function reply(ticket: Ticket, resolved: boolean) {
    if (busy) return;
    if(demo){const message=(drafts[ticket.id]??ticket.staffReply??"").trim();if(!message){setError("Enter a reply before saving.");return;}setError("");setTickets(current=>current.map(item=>item.id===ticket.id?{...item,staffReply:message,status:resolved?"RESOLVED":"OPEN",version:item.version+1}:item));setDrafts(current=>{const next={...current};delete next[ticket.id];return next;});return;}
    setBusy(true); setError("");
    try {
      await readAuthenticatedJson<Ticket>(await authenticatedFetch(`/api/v1/support/admin/${ticket.id}`, {
        method: "PATCH", body: JSON.stringify({ message: drafts[ticket.id] ?? ticket.staffReply ?? "", resolved, version: ticket.version }),
      }));
      setDrafts((prev) => { const next = { ...prev }; delete next[ticket.id]; return next; });
      await load();
    } catch (err) { setError(err instanceof Error ? err.message : "Could not save reply"); }
    finally { setBusy(false); }
  }
  return <PanelCard title="Support tickets" subtitle="Customer and agent requests">
    {error && <p role="alert" className="mb-4 text-red-700">{error}</p>}
    <div className="space-y-4">{tickets.map((ticket) => <article key={ticket.id} className="rounded-xl border border-slate-200 p-4 space-y-3">
      <h3 className="font-bold">#{ticket.id} · {ticket.subject}</h3>
      <p className="text-sm text-slate-500">Customer #{ticket.userId} · {ticket.status}</p>
      <p className="whitespace-pre-wrap">{ticket.message}</p>
      <textarea aria-label={`Reply to ticket ${ticket.id}`} className={inputClass} maxLength={5000} rows={3} value={drafts[ticket.id] ?? ticket.staffReply ?? ""} onChange={(event) => setDrafts((prev) => ({ ...prev, [ticket.id]: event.target.value }))} />
      <div className="flex gap-3"><PrimaryButton disabled={busy} onClick={() => reply(ticket, false)}>Save reply</PrimaryButton><SecondaryButton disabled={busy} onClick={() => reply(ticket, true)}>Reply and resolve</SecondaryButton></div>
    </article>)}</div>
    {!tickets.length && <p className="py-4 text-slate-500">No tickets on this page.</p>}
    <div className="mt-4 flex gap-3"><SecondaryButton disabled={busy || page === 0} onClick={() => setPage((value) => value - 1)}>Previous</SecondaryButton><SecondaryButton disabled={busy || tickets.length < 50} onClick={() => setPage((value) => value + 1)}>Next</SecondaryButton><SecondaryButton disabled={busy} onClick={() => { setError(""); load().catch((err) => setError(err instanceof Error ? err.message : "Could not refresh")); }}>Refresh</SecondaryButton></div>
  </PanelCard>;
}
