import { useCallback, useEffect, useState } from "react";
import { accessApi } from "../api";
import { EmptyState, ErrorState, LoadingState, PageHeader, Pagination, StatusBadge } from "../components/UI";
import type { AccessRecord, PageResponse } from "../types";
import { errorMessage, formatDateTime } from "../utils";

export function MonitoringPage() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<AccessRecord> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const load = useCallback(async () => { setLoading(true); setError(""); try { setData(await accessApi.list(page, 20)); } catch (err) { setError(errorMessage(err, "No se pudieron cargar los accesos.")); } finally { setLoading(false); } }, [page]);
  useEffect(() => { void load(); }, [load]);

  return <div className="page-stack"><PageHeader title="Monitoreo de accesos" subtitle="Trazabilidad operativa de los registros biométricos" actions={<div className="operational-pill"><span className="status-dot" /> Sistema operativo</div>} />{loading ? <LoadingState /> : error ? <ErrorState message={error} onRetry={load} /> : !data?.content.length ? <EmptyState message="No existen registros de acceso." /> : <article className="panel"><div className="table-scroll"><table><thead><tr><th>Fecha y hora</th><th>Persona</th><th>Dispositivo</th><th>Tipo</th><th>Método</th><th>Estado</th></tr></thead><tbody>{data.content.map((item) => <tr key={item.id}><td>{formatDateTime(item.fechaHora)}</td><td><div className="table-person"><strong>{item.nombrePersona}</strong><small>{item.codigoPersona}</small></div></td><td><div className="table-person"><strong>{item.nombreDispositivo}</strong><small>{item.codigoDispositivo}</small></div></td><td><StatusBadge value={item.tipoEvento} /></td><td>{item.metodoVerificacion}</td><td><StatusBadge value={item.estado} /></td></tr>)}</tbody></table></div><Pagination page={data.number} totalPages={data.totalPages} onChange={setPage} /></article>}</div>;
}
