# CORREGIR BUILD FAILURE — Tienda Sebas (30-10-2026)

Este parche está basado en el ZIP del backend que compartiste y el log de `mvnw.cmd clean test`.
Es un PARCHE DE COMPILACIÓN, no una migración SaaS ni prueba de seguridad.

## Causas observadas
1. Package incorrecto en `entity/Tienda.java` y `repository/TiendaRepository.java`.
2. Import antiguo de TenantContext en CategoriaService, ProductoService y VentaService.
3. JwtAuthenticationFilter antiguo llamando `extractCorreo` del servicio JWT actualizado.
4. Clases TenantInterceptor y WebMvcConfig antiguas que compiten con JwtAuthenticationFilter.
5. Numerosos getters/setters inexistentes: las clases usan Lombok, así que se declara su procesador explícitamente en Maven; esto es hipótesis probable y debe confirmarse compilando.

## Instalación
1. Cierra el servidor Spring Boot y conserva intacta tu base Supabase.
2. Extrae este ZIP en `D:\Proyectos\TiendaSebas\parche_compilacion` (una carpeta fuera del repositorio).
3. En PowerShell, desde esa carpeta ejecuta:
   `& .\APLICAR_PARCHE.ps1 -Proyecto 'D:\Proyectos\TiendaSebas\TiendaSebas'`
4. Si PowerShell impide ejecutar scripts, ábrelo en IntelliJ y aplica los cambios manualmente copiando el contenido de `archivos/` a las mismas rutas de tu proyecto. No desactives la seguridad global de Windows.
5. En la terminal de IntelliJ, ejecuta `./mvnw.cmd clean test`.
6. Si la compilación falla, comparte la NUEVA primera causa; no ejecutes `git push` aún.

## ¿Qué cambia?
Reemplaza archivos nuevos/antiguos de resolución tenant, repositorios Tienda/Usuario, JWT y configuración de seguridad; arregla imports; configura compilación de Lombok y retira dos clases antiguas, guardando respaldo de cada archivo en una carpeta `RESPALDO_TIENDA_SEBAS_fecha` fuera del proyecto.

**Importante**: se quitó el mapeo de `ruc` y `telefono_soporte` de Tienda.java porque tus capturas de Supabase muestran que esas columnas no existen. Reintroducirlas solo después de migrar la base de datos.

El parche no modifica Supabase ni ejecuta SQL. No se pudo ejecutar Maven con dependencias en este entorno. `BUILD SUCCESS` debe confirmarse en tu computadora.
