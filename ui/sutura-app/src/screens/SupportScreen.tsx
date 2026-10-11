import { SupportTickets } from "../components/SupportTickets";
import { useWallet } from "../context/WalletContext";
export function SupportScreen() {
  const { setTab } = useWallet();
  return <SupportTickets onBack={() => setTab("more")} />;
}
