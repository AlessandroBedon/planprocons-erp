import { useState, type FormEvent } from "react";
import { LockKeyhole, ShieldCheck, UserRound } from "lucide-react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { errorMessage } from "../utils";

export function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const location = useLocation();

  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  const submit = async (event: FormEvent) => {
    event.preventDefault(); setLoading(true); setError("");
    try {
      await login(username, password);
      const destination = (location.state as { from?: string } | null)?.from || "/dashboard";
      navigate(destination, { replace: true });
    } catch (err) {
      setError(errorMessage(err, "Usuario o contraseña incorrectos."));
    } finally { setLoading(false); }
  };

  return <div className="login-page"><section className="login-brand"><div className="login-brand-content"><div className="brand-lockup"><div className="brand-mark large"><ShieldCheck size={28} /></div><div><strong>PlanProcons</strong><span>Access Intelligence</span></div></div><div className="login-message"><span className="eyebrow light">CONTROL · SEGURIDAD · ANALÍTICA</span><h1>Decisiones claras sobre cada acceso.</h1><p>Monitoreo biométrico, patrones de comportamiento y detección de anomalías en una plataforma empresarial.</p></div><div className="login-points"><span><i />Actividad operativa consolidada</span><span><i />Analítica procesada en PostgreSQL</span><span><i />Acceso protegido mediante JWT</span></div></div></section><section className="login-form-panel"><form className="login-card" onSubmit={submit}><div className="mobile-login-brand"><ShieldCheck size={24} /><strong>PlanProcons</strong></div><span className="eyebrow">ACCESO SEGURO</span><h2>Bienvenido de nuevo</h2><p>Ingrese sus credenciales para acceder al centro de control.</p>{error && <div className="form-error">{error}</div>}<label>Usuario<div className="input-with-icon"><UserRound size={18} /><input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" required placeholder="Nombre de usuario" /></div></label><label>Contraseña<div className="input-with-icon"><LockKeyhole size={18} /><input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" required placeholder="••••••••" /></div></label><button className="button primary login-button" disabled={loading}>{loading ? "Validando…" : "Iniciar sesión"}</button><small>Su sesión se protege con autenticación JWT.</small></form></section></div>;
}
