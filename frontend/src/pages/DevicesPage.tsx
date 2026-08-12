import { useCallback, useEffect, useState } from "react";
import { Cpu, MapPin, Network } from "lucide-react";
import { devicesApi } from "../api";
import { EmptyState, ErrorState, LoadingState, PageHeader, StatusBadge } from "../components/UI";
import type { Device } from "../types";
import { errorMessage, formatDateTime, formatNumber } from "../utils";

export function DevicesPage() {
  const [devices, setDevices] = useState<Device[]>([]); const [loading, setLoading] = useState(true); const [error, setError] = useState("");
  const load = useCallback(async () => { setLoading(true); setError(""); try { setDevices(await devicesApi.list()); } catch (err) { setError(errorMessage(err, "No se pudieron cargar los dispositivos.")); } finally { setLoading(false); } }, []);
  useEffect(() => { void load(); }, [load]);

  return <div className="page-stack"><PageHeader title="Dispositivos" subtitle={`${formatNumber(devices.length)} equipos registrados en el sistema`} />{loading ? <LoadingState /> : error ? <ErrorState message={error} onRetry={load} /> : !devices.length ? <EmptyState message="No existen dispositivos registrados." /> : <div className="device-grid">{devices.map((device) => <article className="device-card" key={device.id}><div className="device-card-head"><div className="device-icon"><Cpu size={22} /></div><div><span>{device.codigo}</span><h3>{device.nombre}</h3></div><StatusBadge value={device.activo} /></div><div className="device-details"><div><span>Modelo</span><strong>{device.modelo || "No especificado"}</strong></div><div><span>Serial</span><strong>{device.serial}</strong></div><div><Network size={16} /><span>IP</span><strong>{device.ip || "No configurada"}</strong></div><div><MapPin size={16} /><span>Ubicación</span><strong>{device.ubicacion || "No especificada"}</strong></div></div><div className="device-footer"><span>Último contacto registrado</span><strong>{formatDateTime(device.ultimoContacto)}</strong><small>Activo indica disponibilidad administrativa, no conectividad en tiempo real.</small></div></article>)}</div>}</div>;
}
