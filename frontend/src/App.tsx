import { lazy, Suspense, type ComponentType } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { ProtectedRoute } from "./auth/ProtectedRoute";
import { AppLayout } from "./components/AppLayout";
import { LoadingState } from "./components/UI";

const loadPage = <T extends Record<string, ComponentType>>(loader: () => Promise<T>, name: keyof T) =>
  lazy(async () => ({ default: (await loader())[name] }));

const LoginPage = loadPage(() => import("./pages/LoginPage"), "LoginPage");
const DashboardPage = loadPage(() => import("./pages/DashboardPage"), "DashboardPage");
const MonitoringPage = loadPage(() => import("./pages/MonitoringPage"), "MonitoringPage");
const AnalyticsPage = loadPage(() => import("./pages/AnalyticsPage"), "AnalyticsPage");
const AnomaliesPage = loadPage(() => import("./pages/AnomaliesPage"), "AnomaliesPage");
const PeoplePage = loadPage(() => import("./pages/PeoplePage"), "PeoplePage");
const DevicesPage = loadPage(() => import("./pages/DevicesPage"), "DevicesPage");

export function App() {
  return (
    <Suspense fallback={<LoadingState label="Cargando aplicación…" />}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/monitoreo" element={<MonitoringPage />} />
            <Route path="/analitica" element={<AnalyticsPage />} />
            <Route path="/anomalias" element={<AnomaliesPage />} />
            <Route path="/personas" element={<PeoplePage />} />
            <Route path="/dispositivos" element={<DevicesPage />} />
          </Route>
        </Route>
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </Suspense>
  );
}
