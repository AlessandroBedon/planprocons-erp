import {
  Area, AreaChart, Bar, BarChart, CartesianGrid, ResponsiveContainer,
  Tooltip, XAxis, YAxis,
} from "recharts";
import type { AccessByDay, AccessByHour } from "../types";

export function AccessByHourChart({ data }: { data: AccessByHour[] }) {
  const complete = Array.from({ length: 24 }, (_, hora) => ({
    hora,
    label: `${String(hora).padStart(2, "0")}:00`,
    cantidad: data.find((item) => item.hora === hora)?.cantidad || 0,
  }));

  return <div className="chart-wrap"><ResponsiveContainer width="100%" height="100%"><AreaChart data={complete} margin={{ top: 12, right: 10, left: -18, bottom: 0 }}><defs><linearGradient id="accessFill" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#2f6ca8" stopOpacity={0.3} /><stop offset="100%" stopColor="#2f6ca8" stopOpacity={0.02} /></linearGradient></defs><CartesianGrid stroke="#e9edf2" vertical={false} /><XAxis dataKey="label" tick={{ fontSize: 11, fill: "#7b8798" }} axisLine={false} tickLine={false} interval={2} /><YAxis tick={{ fontSize: 11, fill: "#7b8798" }} axisLine={false} tickLine={false} /><Tooltip contentStyle={{ borderRadius: 10, border: "1px solid #dde3ea", boxShadow: "0 8px 24px rgba(24,39,58,.08)" }} formatter={(value) => [`${value} accesos`, "Cantidad"]} /><Area type="monotone" dataKey="cantidad" stroke="#2f6ca8" strokeWidth={2.5} fill="url(#accessFill)" activeDot={{ r: 5 }} /></AreaChart></ResponsiveContainer></div>;
}

export function AccessByDayChart({ data }: { data: AccessByDay[] }) {
  const chartData = data.map((item) => ({ ...item, label: new Date(`${item.fecha}T12:00:00`).toLocaleDateString("es-EC", { day: "2-digit", month: "short" }) }));
  return <div className="chart-wrap"><ResponsiveContainer width="100%" height="100%"><BarChart data={chartData} margin={{ top: 12, right: 10, left: -18, bottom: 0 }}><CartesianGrid stroke="#e9edf2" vertical={false} /><XAxis dataKey="label" tick={{ fontSize: 11, fill: "#7b8798" }} axisLine={false} tickLine={false} /><YAxis tick={{ fontSize: 11, fill: "#7b8798" }} axisLine={false} tickLine={false} /><Tooltip cursor={{ fill: "#f3f6f9" }} contentStyle={{ borderRadius: 10, border: "1px solid #dde3ea" }} formatter={(value) => [`${value} accesos`, "Actividad"]} /><Bar dataKey="cantidad" fill="#315f8c" radius={[5, 5, 0, 0]} maxBarSize={42} /></BarChart></ResponsiveContainer></div>;
}
