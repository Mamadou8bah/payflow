import { useEffect, useRef, useState } from "react";
import { ActivityIndicator, AppState, Linking, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { kycApi, type KycStatus } from "../api/kyc";

const descriptions: Record<KycStatus["status"], string> = {
  NOT_STARTED: "Verify your identity using an identity document and a face and liveness check.",
  PENDING: "Complete the steps on your verification page, then check your status here.",
  EXPIRED: "Your verification link expired. Start again for a new link.",
  VERIFIED: "Your identity has been verified.",
  FLAGGED: "Your identity check needs review. Check back for the result.",
  REJECTED: "Your identity could not be verified. You can try again with clear, valid evidence.",
};

export function VerificationScreen({ onBack }: { onBack: () => void }) {
  const [record, setRecord] = useState<KycStatus | null>(null);
  const [busy, setBusy] = useState(true);
  const [error, setError] = useState("");
  const active = useRef(true);
  const inFlight = useRef(false);

  async function run(action: "load" | "start" | "refresh") {
    if (inFlight.current) return;
    inFlight.current = true;
    setBusy(true);
    setError("");
    try {
      if (action === "load") {
        const stored = await kycApi.status();
        if (active.current) setRecord(stored);
        if (stored.sessionId) {
          const fresh = await kycApi.refresh();
          if (active.current) setRecord(fresh);
        }
      } else {
        const value = await kycApi[action]();
        if (active.current) setRecord(value);
      }
    } catch (err) {
      if (active.current) setError(err instanceof Error ? err.message : "Verification is unavailable");
    } finally {
      inFlight.current = false;
      if (active.current) setBusy(false);
    }
  }

  useEffect(() => {
    active.current = true;
    void run("load");
    const subscription = AppState.addEventListener("change", (state) => {
      if (state === "active") void run("load");
    });
    return () => { active.current = false; subscription.remove(); };
  }, []);

  async function openVerification() {
    const url = record?.hostedUrl;
    if (!url || !/^https?:\/\//i.test(url)) { setError("Verification link is unavailable"); return; }
    try { await Linking.openURL(url); }
    catch { setError("Could not open verification. Please try again."); }
  }

  return (
    <ScrollView contentContainerStyle={styles.content}>
      <Pressable accessibilityRole="button" onPress={onBack}><Text style={styles.link}>← Back to your account</Text></Pressable>
      <Text style={styles.title}>Verify your identity</Text>
      <View style={styles.card}>
        <Text style={styles.status}>{record?.status.replace(/_/g, " ") ?? "Loading verification"}</Text>
        {record && <Text style={styles.body}>{descriptions[record.status]}</Text>}
        {busy && <ActivityIndicator color="#123c91" accessibilityLabel="Updating verification" />}
        {!!error && <Text accessibilityRole="alert" style={styles.error}>{error}</Text>}
        {record && ["NOT_STARTED", "EXPIRED", "REJECTED"].includes(record.status) && (
          <Pressable accessibilityRole="button" disabled={busy} onPress={() => void run("start")} style={[styles.button, busy && styles.disabled]}><Text style={styles.buttonText}>Start verification</Text></Pressable>
        )}
        {record?.status === "PENDING" && record.hostedUrl && (
          <Pressable accessibilityRole="button" disabled={busy} onPress={() => void openVerification()} style={styles.button}><Text style={styles.buttonText}>Continue verification</Text></Pressable>
        )}
        {record?.sessionId && <Pressable accessibilityRole="button" disabled={busy} onPress={() => void run("refresh")}><Text style={styles.link}>Refresh status</Text></Pressable>}
        {!record && !busy && <Pressable accessibilityRole="button" onPress={() => void run("load")}><Text style={styles.link}>Try again</Text></Pressable>}
      </View>
    </ScrollView>
  );
}
const styles = StyleSheet.create({
  content: { padding: 24, gap: 22 },
  title: { fontSize: 25, fontWeight: "800", color: "#0f172a" },
  card: { backgroundColor: "white", borderRadius: 20, padding: 20, gap: 18, borderWidth: 1, borderColor: "#e2e8f0" },
  status: { color: "#123c91", fontWeight: "800", fontSize: 14 },
  body: { color: "#475569", lineHeight: 23, fontSize: 15 },
  link: { color: "#123c91", fontSize: 15, fontWeight: "700" },
  error: { color: "#be123c", fontSize: 14 },
  button: { backgroundColor: "#123c91", borderRadius: 12, padding: 16, alignItems: "center" },
  buttonText: { color: "white", fontWeight: "700" },
  disabled: { opacity: 0.5 },
});
