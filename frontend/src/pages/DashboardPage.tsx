import { useCallback, useEffect, useRef, useState } from "react";
import { Activity, ArrowDownToLine, ArrowUpFromLine, Clock3, ShieldCheck, UsersRound } from "lucide-react";
import { Link } from "react-router-dom";
import { accessApi, analysisApi, dashboardApi } from "../api";
import { AccessByDayChart, AccessByHourChart } from "../components/Charts";
import { EmptyState, ErrorState, LoadingState, MetricCard, PageHeader, StatusBadge } from "../components/UI";
import type { AccessByDay, AccessByHour, AccessRecord, AnomalySummary, DashboardSummary, FrequentPerson, PatternSummary } from "../types";
import { daysBefore, errorMessage, formatHour, formatNumber, formatTime, today } from "../utils";
import { useAccessStream } from "../hooks/useAccessStream";

export function DashboardPage() {
  const [date, setDate] = useState(today());
  const [data, setData] = useState<{ summary: DashboardSummary; hours: AccessByHour[]; days: AccessByDay[]; frequent: FrequentPerson[]; recent: AccessRecord[]; anomalies: AnomalySummary; patterns: PatternSummary } | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const refreshTimer = useRef<number | undefined>(undefined);

  const load = useCallback(async () => {
    setLoading(true); setError("");
    const from = daysBefore(date, 6);
    try {
      const [summary, hours, days, frequent, recent, anomalies, patterns] = await Promise.all([
        dashboardApi.summary(date), dashboardApi.byHour(date), dashboardApi.byDay(from, date), dashboardApi.frequent(from, date, 5), accessApi.list(0, 10), analysisApi.anomalySummary(from, date), analysisApi.patterns(from, date),
      ]);
      setData({ summary, hours, days, frequent, recent: recent.content, anomalies, patterns });
    } catch (err) { setError(errorMessage(err)); }
    finally { setLoading(false); }
  }, [date]);

  useEffect(() => { void load(); }, [load]);

  const refreshRealtime = useCallback(async () => {
    try {
      const [summary, hours, recent] = await Promise.all([
        dashboardApi.summary(date), dashboardApi.byHour(date), accessApi.list(0, 10),
      ]);
      setData((current) => current ? { ...current, summary, hours, recent: recent.content } : current);
    } catch {
      // La próxima marcación o recarga manual volverá a intentar la actualización.
    }
  }, [date]);

  useAccessStream(useCallback(() => {
    if (refreshTimer.current !== undefined) window.clearTimeout(refreshTimer.current);
    refreshTimer.current = window.setTimeout(() => {
      refreshTimer.current = undefined;
      void refreshRealtime();
    }, 1_500);
  }, [refreshRealtime]));

  useEffect(() => () => {
    if (refreshTimer.current !== undefined) window.clearTimeout(refreshTimer.current);
  }, []);

  if (loading) return <LoadingState label="Preparando el centro de control…" />;
  if (error || !data) return <ErrorState message={error || "No se encontró información."} onRetry={load} />;

  const { summary, hours, days, frequent, recent, anomalies, patterns } = data;
  return <div className="page-stack"><PageHeader title="Centro de Control de Accesos" subtitle="Monitoreo y análisis inteligente de registros biométricos" actions={<label className="date-control"><span>Fecha de análisis</span><input type="date" value={date} onChange={(e) => setDate(e.target.value)} /></label>} /><section className="metrics-grid"><MetricCard label="Accesos totales" value={formatNumber(summary.totalAccesos)} helper={`${formatNumber(summary.permitidos)} permitidos`} icon={Activity} /><MetricCard label="Entradas" value={formatNumber(summary.entradas)} helper="Eventos registrados" icon={ArrowDownToLine} tone="green" /><MetricCard label="Salidas" value={formatNumber(summary.salidas)} helper="Eventos registrados" icon={ArrowUpFromLine} tone="indigo" /><MetricCard label="Personas únicas" value={formatNumber(summary.personasUnicas)} helper="Con actividad" icon={UsersRound} tone="slate" /></section><section className="insight-strip"><div><ShieldCheck size={18} /><span>Permitidos</span><strong>{formatNumber(summary.permitidos)}</strong></div><div className="attention"><Activity size={18} /><span>Rechazados</span><strong>{formatNumber(summary.rechazados)}</strong></div><div><Clock3 size={18} /><span>Hora pico</span><strong>{formatHour(summary.horaPico)}</strong><small>{formatNumber(summary.cantidadHoraPico)} accesos</small></div></section><section className="dashboard-grid"><article className="panel span-2"><div className="panel-heading"><div><span className="eyebrow">DISTRIBUCIÓN TEMPORAL</span><h3>Flujo de accesos por hora</h3></div><span className="panel-note">24 horas</span></div><AccessByHourChart data={hours} /></article><article className="panel"><div className="panel-heading"><div><span className="eyebrow">ÚLTIMOS 7 DÍAS</span><h3>Actividad diaria</h3></div></div><AccessByDayChart data={days} /></article></section><section className="dashboard-grid lower"><article className="panel"><div className="panel-heading"><div><span className="eyebrow">RANKING</span><h3>Personas frecuentes</h3></div><Link to="/analitica">Ver análisis</Link></div>{frequent.length === 0 ? <EmptyState /> : <div className="ranking-list">{frequent.map((person, index) => <div key={person.personaId}><span className="rank">{index + 1}</span><div className="table-person"><strong>{person.nombre}</strong><small>{person.codigoBiometrico}</small></div><strong>{formatNumber(person.cantidadAccesos)}</strong></div>)}</div>}</article><article className="panel"><div className="panel-heading"><div><span className="eyebrow">MOTOR ANALÍTICO</span><h3>Patrones detectados</h3></div><Link to="/analitica">Explorar</Link></div><div className="pattern-grid"><div><span>Pico general</span><strong>{formatHour(patterns.horaPicoGeneral)}</strong></div><div><span>Pico entradas</span><strong>{formatHour(patterns.horaPicoEntradas)}</strong></div><div><span>Pico salidas</span><strong>{formatHour(patterns.horaPicoSalidas)}</strong></div><div><span>Día predominante</span><strong>{patterns.diaSemanaMayorActividad || "—"}</strong></div></div></article><article className="panel anomaly-panel"><div className="panel-heading"><div><span className="eyebrow">SEGURIDAD</span><h3>Anomalías detectadas</h3></div><Link to="/anomalias">Ver análisis completo</Link></div><div className="anomaly-total"><strong>{formatNumber(anomalies.totalAnomalias)}</strong><span>hallazgos en el periodo</span></div><div className="anomaly-mini"><span>Nocturnos <b>{anomalies.accesosNocturnos}</b></span><span>Repetitivos <b>{anomalies.accesosRepetitivos}</b></span><span>Desviaciones <b>{anomalies.desviacionesHorario}</b></span></div></article></section><article className="panel"><div className="panel-heading"><div><span className="eyebrow">TRAZABILIDAD</span><h3>Actividad reciente</h3></div><Link to="/monitoreo">Abrir monitoreo</Link></div>{recent.length === 0 ? <EmptyState /> : <div className="table-scroll"><table><thead><tr><th>Hora</th><th>Persona</th><th>Dispositivo</th><th>Tipo</th><th>Método</th><th>Estado</th></tr></thead><tbody>{recent.map((item) => <tr key={item.id}><td>{formatTime(item.fechaHora)}</td><td><div className="table-person"><strong>{item.nombrePersona}</strong><small>{item.codigoPersona}</small></div></td><td>{item.nombreDispositivo}</td><td><StatusBadge value={item.tipoEvento} /></td><td>{item.metodoVerificacion}</td><td><StatusBadge value={item.estado} /></td></tr>)}</tbody></table></div>}</article></div>;
}
