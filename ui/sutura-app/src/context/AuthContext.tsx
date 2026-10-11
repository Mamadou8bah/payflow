import { demoSession, isDemoMode, serverAvailable, setDemoMode, subscribeDemo } from "../api/demo";
import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { api, type AuthSession, type CustomerRegistration } from "../api/client";
import { bindTokenSession } from "../api/sessionAuth";
import { clearSession, loadSession, saveSession } from "../storage/secureSession";

const SESSION_KEY = "sutura_customer_session";

type AuthContextValue = {
  session: AuthSession | null;
  loading: boolean;
  newlyRegistered: boolean;
  signUp: (payload: CustomerRegistration) => Promise<void>;
  signIn: (email: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [newlyRegistered, setNewlyRegistered] = useState(false);
  const sessionRef = useRef(session);
  sessionRef.current = session;
  const [loading, setLoading] = useState(true);
  useEffect(() => subscribeDemo(() => setSession(isDemoMode() ? demoSession("USER") : null)), []);

  const persistSession = useCallback(async (stored: AuthSession) => {
    if (!stored.accessToken.startsWith("demo.")) await saveSession(SESSION_KEY, JSON.stringify(stored));
    setSession(stored);
  }, []);

  const signOut = useCallback(async () => {
    if (!isDemoMode()) await clearSession(SESSION_KEY);
    setDemoMode(false);
    setSession(null);
    setNewlyRegistered(false);
  }, []);

  useEffect(() => {
    bindTokenSession({
      getSession: () => sessionRef.current,
      updateSession: persistSession,
      clearSession: signOut,
    });
  }, [session, persistSession, signOut]);

  useEffect(() => {
    loadSession(SESSION_KEY)
      .then((raw) => {
        if (raw) setSession(JSON.parse(raw) as AuthSession);
      })
      .catch(() => {
        // Corrupted session — force re-login
      })
      .finally(async () => { if (!(await serverAvailable())) setDemoMode(true); setLoading(false); });
  }, []);

  const signIn = useCallback(async (email: string, password: string) => {
    const auth = await api.login(email, password);
    if (auth.twoFactorRequired) {
      throw new Error("Two-factor authentication is required for this account");
    }
    if (auth.role === "AGENT") {
      throw new Error("Agent accounts must use the Sutura Agent app");
    }
    const stored: AuthSession = {
      accessToken: auth.accessToken,
      refreshToken: auth.refreshToken,
      username: auth.username,
      role: auth.role,
      firstName: auth.firstName,
      lastName: auth.lastName,
    };
    await persistSession(stored);
  }, [persistSession]);

  const signUp = useCallback(async (payload: CustomerRegistration) => {
    const auth = await api.register(payload);
    if (auth.role !== "USER") throw new Error("Registration did not create a customer account");
    await persistSession({ ...auth, firstName: payload.firstName, lastName: payload.lastName });
    setNewlyRegistered(true);
  }, [persistSession]);

  const value = useMemo(
    () => ({ session, loading, signIn, signUp, signOut, newlyRegistered }),
    [session, loading, signIn, signUp, signOut, newlyRegistered],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
