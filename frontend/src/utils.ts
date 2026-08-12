export const today = () => new Date().toISOString().slice(0, 10);

export function daysBefore(date: string, days: number) {
  const value = new Date(`${date}T12:00:00`);
  value.setDate(value.getDate() - days);
  return value.toISOString().slice(0, 10);
}

export const formatNumber = (value?: number | null) => new Intl.NumberFormat("es-EC").format(value || 0);
export const formatHour = (value?: number | null) => value == null ? "—" : `${String(value).padStart(2, "0")}:00`;
export const formatDateTime = (value?: string) => value ? new Date(value).toLocaleString("es-EC", { dateStyle: "medium", timeStyle: "short" }) : "—";
export const formatTime = (value?: string) => value ? new Date(value).toLocaleTimeString("es-EC", { hour: "2-digit", minute: "2-digit" }) : "—";

export function errorMessage(error: unknown, fallback = "No se pudo cargar la información.") {
  if (typeof error === "object" && error && "response" in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response;
    return response?.data?.message || fallback;
  }
  return fallback;
}
