# PlanProcons — Control de Accesos ZKTeco

Frontend empresarial del módulo de control y análisis de accesos. Está construido con React, TypeScript y Vite, y consume el backend Spring Boot existente.

## Requisitos

- Node.js 22 o superior
- Backend disponible en `http://localhost:8080`

## Desarrollo local

```bash
npm install
npm run dev
```

Abrir `http://127.0.0.1:5173`. Vite redirige `/auth` y `/api` al backend local, por lo que no es necesario modificar CORS durante el desarrollo.

## Variables de entorno

`VITE_API_URL` es opcional. Sin ella se usa el proxy local de Vite. Para otro entorno, copiar `.env.example` a `.env` y ajustar la URL.

## Validación

```bash
npm run lint
npm run build
```

El JWT se conserva en `localStorage` y Axios lo envía como `Authorization: Bearer <token>` en las rutas protegidas.
