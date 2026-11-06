# ORDEN MAESTRA — TERMINAR SEBAS COLLECTION SAAS

Eres el ingeniero principal responsable de llevar a estado **release candidate** el SaaS multi-tenant Sebas Collection. Tienes permiso para inspeccionar, implementar, ejecutar builds/tests, y documentar cambios en los **tres repositorios locales**, sin modificar datos reales ni desplegar producción. No te limites a proponer soluciones: implementa el código, demuestra resultados con pruebas y deja constancia de cualquier bloqueo externo.

## Repositorios autorizados

- Backend: `D:\Proyectos\TiendaSebas\TiendaSebas` — Spring Boot 4.1.1, Java 21, Maven, PostgreSQL Supabase, JWT, multitenancy por subdominio.
- Tienda: `D:\Proyectos\tienda-sebas-polvos-frontend` — Angular 22, storefront y panel admin por tenant.
- Superadmin: `D:\Proyectos\super-admin-saas` — Angular, gestión central de tiendas, propietarios y planes.

Lee el código existente y los `AGENTS.md` de cada repositorio antes de actuar. NO presupongas rutas de endpoints, campos SQL o nombres de entidades; confírmalos en el código. Los tenants existentes deben preservarse, incluidas las 9 tiendas. Tenant IDs y dominios de prueba no deben codificarse como reglas del negocio. No hay que clonar frontends por tienda.

## Entregable esperado

Quiero que termines todos los trabajos que son posibles sin credenciales de terceros ni decisiones de negocio no resueltas. Realiza todas las fases en orden, pasando pruebas tras cada fase, con cambios pequeños y verificables. No te detengas tras un análisis o un plan; sigue implementando de forma autónoma siempre que haya tareas seguras, sin pedir autorización rutinaria. Solicita autorización solo para operaciones destructivas, externas, con coste, o que requieran secretos/decisiones de negocio. Nunca declares una fase terminada sin evidencia de tests/build correctos.

### Fase A — Cerrar cancelaciones COD (prioridad máxima)

- Auditoría integral del endpoint de cancelación, `CancelacionCodService`, `VentaService`, `ContraEntregaService`, `EnvioService`, `TenantResolver`, `JwtAuthenticationFilter`, `SecurityConfig`, repositorios JPA, entidades y SQL.
- Comprobar cancelación única, autorización ADMIN, pedido en tenant correcto, estados elegibles, pago PENDIENTE, logística sin despacho, stock por `producto_talla`, reversión total ante fallo y auditoría de inventario.
- Revisar CORS para método PATCH y eliminar supuestos peligrosos de tenant predeterminado, sin romper flujos vigentes.
- Pruebas reales HTTP/JWT/host+controller+Spring Security+JPA+PostgreSQL Testcontainers, usando **exclusivamente contenedores temporales**. Probar éxito, segunda cancelación 409, carrera de solicitudes, rol CLIENTE, token de otra tienda, tienda inactiva, rollback, integridad de kardex. No usar adaptadores falsos para sustituir JPA en las pruebas E2E.
- Distinguir las pruebas ya existentes de las nuevas: 12 unitarias de cancelación, 2 smoke Testcontainers y 6 de integración con adaptadores JDBC previamente aprobadas. Estos resultados no equivalen a E2E.

### Fase B — Finanzas, informes y caja

- Auditar y corregir KPIs: venta registrada != ingreso cobrado; COD PENDIENTE no es efectivo recaudado; registrar pagos cobrados, anulaciones, devoluciones, saldo por cobrar y comisiones por método. Filtrar por tenant, rango, moneda y zona horaria `America/Lima`.
- Unificar cálculos en backend y exponer DTO/API para dashboard y reportes; evitar cálculos contables solo en Angular.
- Completar apertura, movimientos, arqueo, cierre de caja, diferencias, trazabilidad por usuario, reportes y pruebas (sin duplicar registros existentes).
- Corregir frontend tienda/admin y superadmin cuando corresponda. Preservar funciones POS, stock, checkout y reportes previos.

### Fase C — Pagos reales y devoluciones

- Separar demostración visual de pagos confirmados, estados PENDIENTE/CONFIRMADO/FALLIDO/REEMBOLSADO, idempotencia, webhooks con autenticación y firmas, conciliación, auditoría y rollback seguro.
- Crear adaptadores/interfaces de proveedor de pagos configurables sin almacenar datos de tarjetas. Si falta cuenta/API key/contrato para proveedor real, implementar integración desacoplada, sandbox/mock y pruebas, documentar `PENDIENTE_CONFIGURACION_EXTERNA` y NO afirmar transacciones reales.
- Devoluciones/reembolsos y ajustes de stock idempotentes para ventas elegibles; prevenir doble devolución.

### Fase D — Boletas y facturas electrónicas SUNAT

- Implementar modelo de comprobantes, numeración por tienda/serie, RUC y datos obligatorios, validaciones, estados, documentos XML/representación según la arquitectura elegida, bitácora, reintentos idempotentes y API para consultar comprobantes.
- Implementar adaptador hacia proveedor autorizado u homologación según decisión técnica documentada. **No emitir ni simular como válidos comprobantes tributarios reales** sin credenciales, certificados, alta y pruebas de homologación. Marcar claramente la habilitación externa pendiente.
- Pruebas unitarias y de integración para numeración concurrente, tenancy y reintentos.

### Fase E — Seguridad, UX y lanzamiento técnico

- Auditoría de autorización por endpoint y tenant, exposición de PII, JWT, roles, CORS, validación servidor, rate limits según riesgo, upload seguro, secretos, logs, errores, migraciones no destructivas, idempotencia y concurrencia.
- Mejorar formularios y feedback UX donde haya fallos reales, no reescribir diseño sin motivo. Añadir estados vacíos/carga/error y accesibilidad básica.
- Configurar tests automatizados, builds, scripts de verificación, plantillas `.env.example` sin secretos, salud y readiness, copia de seguridad/recuperación (documentadas), y manual de despliegue para backend, storefront, superadmin y dominios de cada tenant.
- No desplegar producción; entregar guía paso a paso para hacerlo luego con ChatGPT.

## Seguridad obligatoria

1. Prohibido ejecutar comandos INSERT/UPDATE/DELETE/DDL/TRUNCATE/DROP contra Supabase de las tiendas actuales, migraciones automáticas destructivas o pruebas en datos de clientes reales. No conectarse a producción por accidente: en tests establecer datasource de Testcontainers de manera inequívoca, y abortar si el JDBC URL contiene host Supabase u otro host no permitido.
2. No solicitar, imprimir, registrar en logs o versionar contraseñas DB, JWT secrets, tokens, certificados, API keys ni `.env`. No poner credenciales de prueba en archivos rastreados que sean reutilizables en producción.
3. Antes de cambios: `git status`, crear rama `codex/sebas-release-candidate` si es seguro; respetar cambios locales sin pisarlos y registrar diferencias. No `git reset --hard`, no force push, no `git clean -fd`, no borrado masivo.
4. Cambios de esquema: migraciones versionadas, revisables, reversibles cuando proceda; probar contra PostgreSQL aislado. No asumir el esquema real de Supabase por entidades JPA solamente: inventariar DDL y diffs.
5. No hacer `git push`, desplegar, contratar servicios, pagar, enviar mensajes reales, emitir facturas reales, ni activar pagos reales sin mi aprobación explícita.
6. Si falta proveedor de pagos, configuración SUNAT o secretos, concluir lo que sí se puede implementar y anotar el bloqueo exacto; **no inventar integración funcional**.

## Forma de trabajo / criterio de terminado

- Primera acción: auditar las tres carpetas, identificar builds existentes, pruebas, rutas y migraciones; escribir `docs/CODEX_PROGRESO_RELEASE.md` en el backend con checklist por fase, hallazgos y bloqueos. No reemplazar un `AGENTS.md` existente sin reconciliarlo.
- Recorre ciclo implementar → compilar → testear → reparar → documentar. Ejecuta unit tests, Testcontainers/JPA/HTTP y builds Angular. Corrige fallos detectados en lo que esté a tu alcance, sin ocultar warnings relevantes.
- Se permiten agentes/trabajos paralelos para **análisis independiente**; no dejar agentes editando simultáneamente los mismos archivos o compartiendo migraciones sin coordinación.
- Realiza commits locales acotados por funcionalidad únicamente si no hay riesgo de incluir secretos o cambios ajenos.
- Entrega `docs/RELEASE_CANDIDATE_RESULTADOS.md`: funcionalidades completas y parciales, pruebas con número de casos y resultado, archivos modificados, decisiones técnicas, riesgos, credenciales/configuraciones faltantes, pasos para levantar backend/2 frontends, y **lista final de acciones para despliegue con ChatGPT**.
- Si algo no puede verificarse, marcarlo BLOQUEADO/NO VERIFICADO; no como verde.
- No preguntes si debes continuar después de cada fase. Sigue trabajando hasta completar todas las tareas técnicamente realizables dentro de los límites y reporta progreso en los archivos.

## Inicio

Empieza ahora mismo auditando las tres carpetas. Crea el checklist, implementa la Fase A, ejecuta pruebas y continúa sucesivamente con B, C, D y E hasta un release candidate comprobado. Al final muestra resumen ejecutivo y rutas de los documentos de entrega.
