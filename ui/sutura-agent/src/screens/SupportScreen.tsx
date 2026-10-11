import { SupportTickets } from "../components/SupportTickets";
import { useAgent } from "../context/AgentContext";
export function SupportScreen() {
  const { setTab } = useAgent();
  return <SupportTickets onBack={() => setTab("more")} />;
}
