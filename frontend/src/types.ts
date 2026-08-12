export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface DashboardSummary {
  totalAccesos: number;
  entradas: number;
  salidas: number;
  permitidos: number;
  rechazados: number;
  personasUnicas: number;
  horaPico: number | null;
  cantidadHoraPico: number;
}

export interface AccessByHour { hora: number; cantidad: number }
export interface AccessByDay { fecha: string; cantidad: number }
export interface FrequentPerson {
  personaId: number;
  codigoBiometrico: string;
  nombre: string;
  cantidadAccesos: number;
}

export interface AccessRecord {
  id: number;
  personaId: number;
  codigoPersona: string;
  nombrePersona: string;
  dispositivoId: number;
  codigoDispositivo: string;
  nombreDispositivo: string;
  fechaHora: string;
  tipoEvento: "ENTRADA" | "SALIDA";
  metodoVerificacion: string;
  estado: "PERMITIDO" | "RECHAZADO";
  codigoEvento?: string;
  fechaRecepcion: string;
}

export interface PatternSummary {
  registrosAnalizados: number;
  horaPicoGeneral: number | null;
  cantidadHoraPicoGeneral: number;
  horaPicoEntradas: number | null;
  cantidadHoraPicoEntradas: number;
  horaPicoSalidas: number | null;
  cantidadHoraPicoSalidas: number;
  fechaMayorActividad: string | null;
  cantidadFechaMayorActividad: number;
  diaSemanaMayorActividad: string | null;
}

export interface AnomalySummary {
  registrosAnalizados: number;
  totalAnomalias: number;
  accesosNocturnos: number;
  accesosTempranos: number;
  accesosTardios: number;
  accesosRepetitivos: number;
  rechazosRepetitivos: number;
  desviacionesHorario: number;
}

export interface Anomaly {
  tipo: string;
  nivel: "BAJO" | "MEDIO" | "ALTO";
  personaId: number;
  codigoPersona: string;
  nombrePersona: string;
  registroAccesoId: number;
  fechaHora: string;
  descripcion: string;
}

export interface Person {
  id: number;
  codigoBiometrico: string;
  cedula: string;
  nombres: string;
  apellidos: string;
  departamento?: string;
  cargo?: string;
  activo: boolean;
}

export interface Device {
  id: number;
  codigo: string;
  nombre: string;
  modelo?: string;
  serial: string;
  ip?: string;
  ubicacion?: string;
  ultimoContacto?: string;
  activo: boolean;
}
