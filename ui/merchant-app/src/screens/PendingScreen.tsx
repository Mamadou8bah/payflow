import {fonts as appFonts} from "../theme";
import {useTheme,useThemedStyles} from "../context/ThemeContext";
import type {ThemeColors} from "../theme";
import { StyleSheet, Text, View } from "react-native";
import { Card, LinkButton, SuturaLogo, PrimaryButton, Screen } from "../components/ui";
import { useApp } from "../context/AppContext";
import { colors } from "../theme";

export function PendingScreen() {
 const {colors}=useTheme();
 const styles=useThemedStyles(createStyles);

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
        <Text style={styles.hint}>This usually takes 1–2 business days. We may contact you on the phone number you verified.</Text>
      </Card>

      <PrimaryButton label="Sign out" onPress={signOut} color={colors.slate700} />
    </Screen>
  );
}

const createStyles = (colors: ThemeColors) => StyleSheet.create({
  hero: { alignItems: "center", gap: 12, paddingVertical: 16 },
  title: {fontFamily: appFonts.bold, fontSize: 24, fontWeight: "800", color: colors.slate900, textAlign: "center" },
  body: {fontFamily: appFonts.regular, fontSize: 15, lineHeight: 22, color: colors.slate500, textAlign: "center" },
  status: {fontFamily: appFonts.bold, fontSize: 16, fontWeight: "800", color: colors.primary },
  hint: {fontFamily: appFonts.regular, marginTop: 8, fontSize: 14, lineHeight: 20, color: colors.slate500 },
});
