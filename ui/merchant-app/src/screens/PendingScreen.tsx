import { StyleSheet, Text, View } from "react-native";
import { Card, LinkButton, SuturaLogo, PrimaryButton, Screen } from "../components/ui";
import { useApp } from "../context/AppContext";
import { colors } from "../theme";

export function PendingScreen() {
  const { session, signOut, setScreen } = useApp();

  return (
    <Screen>
      <PrimaryButton label="Verify owner identity" onPress={() => setScreen("verification")} />
      <View style={styles.hero}>
        <SuturaLogo size={48} />
        <Text style={styles.title}>Application under review</Text>
        <Text style={styles.body}>
          {session?.businessName ? `${session.businessName} is` : "Your business is"} being reviewed by the Sutura team. You will be able to accept payments once approved.
        </Text>
      </View>

      <Card>
        <Text style={styles.status}>Status: Pending review</Text>
        <Text style={styles.hint}>This usually takes 1â€“2 business days. We may contact you on the phone number you verified.</Text>
      </Card>

      <PrimaryButton label="Sign out" onPress={signOut} color={colors.slate700} />
      <LinkButton label="Check again later" onPress={() => {}} />
    </Screen>
  );
}

const styles = StyleSheet.create({
  hero: { alignItems: "center", gap: 12, paddingVertical: 16 },
  title: { fontSize: 24, fontWeight: "800", color: colors.slate900, textAlign: "center" },
  body: { fontSize: 15, lineHeight: 22, color: colors.slate500, textAlign: "center" },
  status: { fontSize: 16, fontWeight: "800", color: colors.primary },
  hint: { marginTop: 8, fontSize: 14, lineHeight: 20, color: colors.slate500 },
});
