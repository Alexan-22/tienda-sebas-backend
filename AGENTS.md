# AGENTS.md — Backend Sebas Collection SaaS

Proyecto Spring Boot 4.1.1, Java 21, Maven Wrapper y PostgreSQL; multi-tenant. Trabaja junto con `D:\Proyectos\tienda-sebas-polvos-frontend` y `D:\Proyectos\super-admin-saas`. Consulta `TAREA_MAESTRA_CODEX.md` para la orden completa.

## Reglas estrictas
- Nunca operar sobre datos reales de Supabase al probar. PostgreSQL Testcontainers aislado; impedir explícitamente datasource de producción en perfiles de prueba.
- Respetar `tienda_id` en cada lectura, escritura, bloqueo JPA, operación de inventario y reportes. Autorización no se reduce a comprobar un rol; también comprobar tienda y propietario.
- JWT, contexto de tenant/host, roles y HTTP/JPA deben probarse juntos para operaciones críticas. No reemplazar pruebas E2E con repositorios simulados.
- Conservar el backend y endpoints existentes salvo justificación y pruebas de compatibilidad. Mantener Java 21 y Spring Boot 4.1.1 hasta comprobar necesidad real de cambio.
- Preferir migraciones SQL versionadas y revisadas; NO `ddl-auto=create` sobre bases ajenas.
- Auditar cancelaciones COD: transacciones, locks, idempotencia, stock, kardex, entregas/cobros. Revisar tenant por defecto en Venta y PATCH en CORS.
- Implementar finanzas reales, caja, pagos/proveedor desacoplado, reembolsos, SUNAT vía integración aún no habilitada externamente y pruebas.
- No subir secretos, `.env`, DB passwords, llaves JWT, certificados o tokens a Git.
- No despliegue ni `git push` sin autorización.
- Registrar progreso en `docs/CODEX_PROGRESO_RELEASE.md` y resultados en `docs/RELEASE_CANDIDATE_RESULTADOS.md`.
