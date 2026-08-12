import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import { authApi } from "../api";
import { TOKEN_KEY } from "../api/client";

interface AuthValue {
  isAuthenticated: boolean;
  username: string;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [username, setUsername] = useState(() => localStorage.getItem("planprocons_username") || "Usuario");

  const value = useMemo<AuthValue>(() => ({
    isAuthenticated: Boolean(token),
    username,
    login: async (user, password) => {
      const response = await authApi.login(user, password);
      localStorage.setItem(TOKEN_KEY, response.token);
      localStorage.setItem("planprocons_username", user);
      setToken(response.token);
      setUsername(user);
    },
    logout: () => {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem("planprocons_username");
      setToken(null);
    },
  }), [token, username]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth debe utilizarse dentro de AuthProvider");
  return context;
}
