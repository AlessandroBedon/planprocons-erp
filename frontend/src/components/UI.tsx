import type { LucideIcon } from "lucide-react";
import { AlertCircle, Inbox, LoaderCircle, RefreshCw } from "lucide-react";
import type { ReactNode } from "react";

export function PageHeader({ title, subtitle, actions }: { title: string; subtitle: string; actions?: ReactNode }) {
  return <div className="page-header"><div><h1>{title}</h1><p>{subtitle}</p></div>{actions && <div className="page-actions">{actions}</div>}</div>;
}

export function MetricCard({ label, value, helper, icon: Icon, tone = "blue" }: { label: string; value: string | number; helper?: string; icon: LucideIcon; tone?: string }) {
  return <article className="metric-card"><div className={`metric-icon ${tone}`}><Icon size={20} /></div><div className="metric-copy"><span>{label}</span><strong>{value}</strong>{helper && <small>{helper}</small>}</div></article>;
}

export function StatusBadge({ value }: { value: string | boolean }) {
  const text = typeof value === "boolean" ? (value ? "ACTIVO" : "INACTIVO") : value;
  return <span className={`status-badge status-${text.toLowerCase().replaceAll("_", "-")}`}>{text}</span>;
}

export function LoadingState({ label = "Cargando información…" }: { label?: string }) {
  return <div className="state-box"><LoaderCircle className="spin" size={24} /><span>{label}</span></div>;
}

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return <div className="state-box error"><AlertCircle size={24} /><span>{message}</span>{onRetry && <button className="button secondary" onClick={onRetry}><RefreshCw size={15} /> Reintentar</button>}</div>;
}

export function EmptyState({ message = "No existen datos para mostrar." }: { message?: string }) {
  return <div className="state-box"><Inbox size={24} /><span>{message}</span></div>;
}

export function Pagination({ page, totalPages, onChange }: { page: number; totalPages: number; onChange: (page: number) => void }) {
  return <div className="pagination"><span>Página {page + 1} de {Math.max(totalPages, 1)}</span><div><button disabled={page === 0} onClick={() => onChange(page - 1)}>Anterior</button><button disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)}>Siguiente</button></div></div>;
}
