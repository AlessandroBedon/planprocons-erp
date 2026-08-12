import { useState } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import {
  AlertTriangle, BarChart3, ChevronLeft, ChevronRight,
  CircleGauge, Cpu, LogOut, Menu, MonitorDot, Settings, ShieldCheck, Users, X,
} from "lucide-react";
import { useAuth } from "../auth/AuthContext";

const navigation = [
  { to: "/dashboard", label: "Dashboard", icon: CircleGauge },
  { to: "/monitoreo", label: "Monitoreo", icon: MonitorDot },
  { to: "/analitica", label: "Analítica", icon: BarChart3 },
  { to: "/anomalias", label: "Anomalías", icon: AlertTriangle },
  { to: "/personas", label: "Personas", icon: Users },
  { to: "/dispositivos", label: "Dispositivos", icon: Cpu },
];

const titles: Record<string, string> = {
  "/dashboard": "Centro de Control",
  "/monitoreo": "Monitoreo de accesos",
  "/analitica": "Analítica",
  "/anomalias": "Anomalías",
  "/personas": "Personas",
  "/dispositivos": "Dispositivos",
};

export function AppLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const { username, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const closeMobile = () => setMobileOpen(false);
  const doLogout = () => { logout(); navigate("/login"); };

  return (
    <div className={`app-shell ${collapsed ? "sidebar-collapsed" : ""}`}>
      {mobileOpen && <button className="sidebar-backdrop" aria-label="Cerrar menú" onClick={closeMobile} />}
      <aside className={`sidebar ${mobileOpen ? "mobile-open" : ""}`}>
        <div className="brand-block">
          <div className="brand-mark"><ShieldCheck size={23} /></div>
          {!collapsed && <div><strong>PlanProcons</strong><span>Access Intelligence</span></div>}
          <button className="mobile-close" onClick={closeMobile} aria-label="Cerrar menú"><X size={20} /></button>
        </div>

        <nav className="sidebar-nav" aria-label="Navegación principal">
          <span className="nav-caption">OPERACIÓN</span>
          {navigation.map(({ to, label, icon: Icon }) => (
            <NavLink key={to} to={to} onClick={closeMobile} title={collapsed ? label : undefined}>
              <Icon size={19} /><span>{label}</span>
            </NavLink>
          ))}
          <div className="nav-separator" />
          <span className="nav-caption">SISTEMA</span>
          <button className="nav-disabled" title="Disponible próximamente"><Settings size={19} /><span>Configuración</span></button>
        </nav>

        <div className="sidebar-footer">
          <div className="user-avatar">{username.slice(0, 2).toUpperCase()}</div>
          {!collapsed && <div className="user-copy"><strong>{username}</strong><span>Sesión activa</span></div>}
          <button onClick={doLogout} aria-label="Cerrar sesión" title="Cerrar sesión"><LogOut size={18} /></button>
        </div>
        <button className="collapse-button" onClick={() => setCollapsed(!collapsed)} aria-label="Colapsar barra lateral">
          {collapsed ? <ChevronRight size={17} /> : <ChevronLeft size={17} />}
        </button>
      </aside>

      <div className="main-panel">
        <header className="topbar">
          <div className="topbar-left">
            <button className="mobile-menu" onClick={() => setMobileOpen(true)} aria-label="Abrir menú"><Menu size={21} /></button>
            <div><span className="eyebrow">PLANPROCONS ERP</span><h2>{titles[location.pathname] || "Access Intelligence"}</h2></div>
          </div>
          <div className="system-state"><span className="status-dot" /><div><strong>Sistema operativo</strong><span>Servicios disponibles</span></div></div>
        </header>
        <main className="content-area"><Outlet /></main>
      </div>
    </div>
  );
}
