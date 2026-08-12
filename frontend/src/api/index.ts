import { apiClient } from "./client";
import type {
  AccessByDay, AccessByHour, AccessRecord, Anomaly, AnomalySummary,
  ApiResponse, DashboardSummary, Device, FrequentPerson, PageResponse,
  PatternSummary, Person,
} from "../types";

const unwrap = <T>(response: { data: ApiResponse<T> }) => response.data.data;

export const authApi = {
  login: async (username: string, password: string) =>
    (await apiClient.post<{ token: string; type: string }>("/auth/login", { username, password })).data,
};

export const dashboardApi = {
  summary: async (fecha: string) => unwrap(await apiClient.get<ApiResponse<DashboardSummary>>("/api/dashboard/resumen", { params: { fecha } })),
  byHour: async (fecha: string) => unwrap(await apiClient.get<ApiResponse<AccessByHour[]>>("/api/dashboard/accesos-por-hora", { params: { fecha } })),
  byDay: async (desde: string, hasta: string) => unwrap(await apiClient.get<ApiResponse<AccessByDay[]>>("/api/dashboard/accesos-por-dia", { params: { desde, hasta } })),
  frequent: async (desde: string, hasta: string, limit = 5) => unwrap(await apiClient.get<ApiResponse<FrequentPerson[]>>("/api/dashboard/personas-frecuentes", { params: { desde, hasta, limit } })),
};

export const accessApi = {
  list: async (page = 0, size = 10) => unwrap(await apiClient.get<ApiResponse<PageResponse<AccessRecord>>>("/api/accesos", { params: { page, size, sort: "fechaHora,desc" } })),
};

export const analysisApi = {
  patterns: async (desde: string, hasta: string) => unwrap(await apiClient.get<ApiResponse<PatternSummary>>("/api/analisis/patrones", { params: { desde, hasta } })),
  anomalySummary: async (desde: string, hasta: string) => unwrap(await apiClient.get<ApiResponse<AnomalySummary>>("/api/analisis/anomalias/resumen", { params: { desde, hasta } })),
  anomalies: async (desde: string, hasta: string, page = 0, size = 20, tipo?: string, personaId?: string) =>
    unwrap(await apiClient.get<ApiResponse<PageResponse<Anomaly>>>("/api/analisis/anomalias", { params: { desde, hasta, page, size, tipo: tipo || undefined, personaId: personaId || undefined } })),
};

export const peopleApi = {
  list: async () => unwrap(await apiClient.get<ApiResponse<Person[]>>("/api/personas")),
};

export const devicesApi = {
  list: async () => unwrap(await apiClient.get<ApiResponse<Device[]>>("/api/dispositivos")),
};
