import {isPhoneDraft,normalizePhone,isValidEmail} from "../utils/inputValidation";
import {fonts as appFonts} from "../theme";
import {useTheme,useThemedStyles} from "../context/ThemeContext";
import type {ThemeColors} from "../theme";
import { useRef, useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { AppInput, PasswordInput, Field, LinkButton, SuturaLogo, PrimaryButton, Screen } from "../components/ui";
import { useApp } from "../context/AppContext";
import { colors } from "../theme";

export function LoginScreen() {
 const {colors}=useTheme();
 const styles=useThemedStyles(createStyles);

  const { signIn, setScreen, setError, error } = useApp();
  const submitting=useRef(false);
  const [method,setMethod]=useState<"email"|"phone">("email");
  const [phone,setPhone]=useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit() {
    if(submitting.current)return;
    const username=method==="email"?email.trim():normalizePhone(phone);
    if(method==="email"&&!isValidEmail(email.trim())){setError("Enter a valid email address.");return;}
    if(!username||!password){setError("Enter your phone number and password.");return;}
    submitting.current=true;
    setError("");
    setLoading(true);
    try {
      await signIn(username, password);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      submitting.current=false;
      setLoading(false);
    }
  }

  return (
    <Screen>
      <View style={{marginBottom:24}}><SuturaLogo size={64} /></View>
      <Text style={styles.title}>Log in</Text>
      <Text style={styles.sub}>Sign in to your merchant account</Text>

      <View accessibilityRole="tablist" style={{flexDirection:"row",gap:24,borderBottomWidth:1,borderBottomColor:colors.border}}>{(["email","phone"] as const).map(value=><Pressable accessibilityRole="tab" accessibilityLabel={value==="email"?"Email login":"Phone login"} accessibilityState={{selected:method===value}} key={value} disabled={loading} onPress={()=>{setMethod(value);setError("");}} style={{paddingVertical:12,borderBottomWidth:2,borderBottomColor:method===value?colors.orange:"transparent"}}><Text style={{fontFamily:appFonts.bold,fontSize:15,color:method===value?colors.primary:colors.slate500}}>{value==="email"?"Email":"Phone number"}</Text></Pressable>)}</View>
      {method==="email"?<Field label="Email"><AppInput accessibilityLabel="Email" inputMode="email" keyboardType="email-address" autoComplete="email" autoCapitalize="none" autoCorrect={false} value={email} onChangeText={v=>{if(!/\s/.test(v))setEmail(v);}} maxLength={254} editable={!loading} placeholder="name@example.com"/></Field>:<Field label="Phone number"><AppInput accessibilityLabel="Phone number" inputMode="tel" keyboardType="phone-pad" autoComplete="tel" value={phone} onChangeText={v=>{if(isPhoneDraft(v))setPhone(v);}} maxLength={40} editable={!loading} placeholder="+220 700 0001"/></Field>}
      <Field label="Password">
        <PasswordInput accessibilityLabel="Password" editable={!loading} autoComplete="current-password" autoCapitalize="none" secureTextEntry value={password} onChangeText={setPassword} placeholder="Your password" />
      </Field>

      {error ? <Text style={styles.error}>{error}</Text> : null}

      <PrimaryButton label={loading ? "Please wait…" : "Log in"} onPress={handleSubmit} disabled={loading} />
      <LinkButton label="New to Sutura? Create a merchant account" onPress={() => setScreen("reg-phone")} />
    </Screen>
  );
}

const createStyles = (colors: ThemeColors) => StyleSheet.create({
  title: {fontFamily: appFonts.bold, fontSize: 34, fontWeight: "800", color: colors.slate900 },
  sub: {fontFamily: appFonts.regular, fontSize: 14, color: colors.slate500, marginBottom: 8 },
  error: {fontFamily: appFonts.medium, color: colors.rose800, fontWeight: "600", fontSize: 14 },
});
