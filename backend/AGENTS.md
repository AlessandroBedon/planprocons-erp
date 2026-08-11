# PlanProcons ERP

## Proyecto

Sistema ERP empresarial desarrollado con Spring Boot.

Este proyecto también será utilizado como base para una tesis:

"Aplicación de técnicas Big Data para el análisis de patrones
de registros de acceso ZKTeco mediante IoT."

## Stack actual

Backend:
- Java 17
- Spring Boot
- Maven
- Spring Data JPA
- Hibernate
- Spring Security
- JWT
- Swagger / OpenAPI

Base de datos:
- PostgreSQL
- pgAdmin

Frontend futuro:
- React
- Vite
- Axios
- React Router

IDE:
- IntelliJ IDEA
- Visual Studio Code

## Arquitectura

Mantener siempre que sea posible:

controller
service
service.impl
repository
entity
dto
security
config
exception

## Reglas importantes

NO crear un proyecto Spring Boot nuevo.

Trabajar siempre sobre este proyecto existente.

NO duplicar clases existentes.

Antes de crear una clase nueva, revisar si ya existe una
implementación equivalente.

Mantener la seguridad existente basada en:

- Spring Security
- JWT
- Roles
- Permisos

No reemplazar SecurityConfig, JwtFilter, JwtService,
User, Role o Permission sin analizar primero la implementación existente.

No realizar cambios destructivos en PostgreSQL.

No eliminar tablas o columnas automáticamente.

No exponer contraseñas en responses.

Utilizar DTO cuando corresponda.

Mantener respuestas API consistentes.

Después de cambios importantes:

1. Revisar errores de compilación.
2. Ejecutar Maven.
3. Verificar que Spring Boot inicia.
4. Probar el endpoint afectado.

## Módulo de tesis

El nuevo módulo será:

CONTROL Y ANÁLISIS DE ACCESOS ZKTECO

Flujo objetivo:

ZKTeco / simulador
→ Spring Boot
→ PostgreSQL
→ procesamiento
→ análisis de patrones
→ REST API
→ React
→ Dashboard

Funcionalidades objetivo:

- Personas
- Dispositivos ZKTeco
- Registros de acceso
- Entradas
- Salidas
- Historial
- Monitoreo prácticamente en tiempo real
- Hora pico
- Accesos por hora
- Accesos por día
- Personas actualmente dentro
- Patrones de comportamiento
- Anomalías
- Alertas
- Dashboard
- Datos masivos de prueba

## Restricción de tiempo

El proyecto de tesis debe estar funcional en 9 días.

Priorizar:

1. MVP funcional
2. Estabilidad
3. Captura de registros
4. Análisis
5. Dashboard
6. Funciones avanzadas

Evitar sobreingeniería.

No introducir Kafka, Hadoop, Spark u otra infraestructura
pesada salvo que exista una justificación técnica real.

## Forma de trabajo

Antes de cambios grandes:

- analizar código existente;
- reutilizar componentes actuales;
- realizar cambios pequeños y comprobables;
- explicar los archivos modificados;
- evitar refactorizaciones innecesarias.

Este proyecto ya contiene trabajo realizado y no debe
reiniciarse desde cero.