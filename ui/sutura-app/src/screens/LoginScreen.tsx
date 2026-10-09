import { useState } from "react";
import { ActivityIndicator, StyleSheet, Text, TextInput, View } from "react-native";
import { PrimaryButton, Screen } from "../components/ui";
import { useAuth } from "../context/AuthContext";
import { colors } from "../theme";

export function LoginScreen() {
  const { signIn, signUp } = useAuth();
  const [registering, setRegistering] = useState(false);
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [phone, setPhone] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleLogin() {
    setError("");
    setLoading(true);
    try {
      if (registering) {
        if (!firstName.trim() || !lastName.trim() || !phone.trim() || password.length < 8) {
          throw new Error("Enter your name, phone number, and a password of at least 8 characters");
        }
        await signUp({ firstName: firstName.trim(), lastName: lastName.trim(), phoneNumber: phone.trim(), email: email.trim() || undefined, password });
      } else {
        await signIn(email.trim(), password);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Screen>
      <Text style={styles.title}>Sutura Wallet</Text>
      <Text style={styles.subtitle}>{registering ? "Create your customer account, then verify your identity" : "Sign in to manage your wallets and transfers"}</Text>

      {registering && <>
        <TextInput style={styles.input} placeholder="First name" value={firstName} onChangeText={setFirstName} />
        <TextInput style={styles.input} placeholder="Last name" value={lastName} onChangeText={setLastName} />
        <TextInput style={styles.input} placeholder="Phone number with country code" keyboardType="phone-pad" value={phone} onChangeText={setPhone} />
      </>}
      <TextInput
        style={styles.input}
        placeholder={registering ? "Email (optional)" : "Email or phone"}
        autoCapitalize="none"
        keyboardType="email-address"
        value={email}
        onChangeText={setEmail}
      />
      <TextInput
        style={styles.input}
        placeholder="Password"
        secureTextEntry
        value={password}
        onChangeText={setPassword}
      />

      {error ? <Text style={styles.error}>{error}</Text> : null}

      {loading ? (
        <ActivityIndicator color={colors.primary} />
      ) : (
        <PrimaryButton label={registering ? "Create account" : "Sign in"} onPress={handleLogin} />
      )}
      {!loading && <Text onPress={() => { setRegistering(!registering); setError(""); }} style={styles.switchLink}>
        {registering ? "Already have an account? Sign in" : "New to Sutura? Create an account"}
      </Text>}
    </Screen>
  );
}

const styles = StyleSheet.create({
  switchLink: { marginTop: 18, color: colors.primary, fontWeight: "700", textAlign: "center" },
  title: { fontSize: 28, fontWeight: "800", color: colors.slate900 },
  subtitle: { marginTop: 8, marginBottom: 24, fontSize: 14, color: colors.slate500 },
  input: {
    marginBottom: 12,
    borderWidth: 1,
    borderColor: colors.slate300,
    borderRadius: 14,
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 15,
    backgroundColor: "#fff",
  },
  error: { marginBottom: 12, color: colors.rose500, fontSize: 13, fontWeight: "600" },
});
