# Tienda Sebas – Backend

**Sistema de gestión de tienda de ropa, ventas online y punto de venta (POS)**

Tienda Sebas es una aplicación de comercio electrónico desarrollada con **Java 21**, **Spring Boot** y **PostgreSQL**. Su API REST permite administrar usuarios, catálogo de productos, categorías, géneros, tallas, inventario, imágenes, compras online, ventas presenciales y comprobantes. Se integra con un frontend desarrollado en **Angular, TypeScript y Tailwind CSS**.

## 1. Tecnologías utilizadas

- Java 21
- Spring Boot 4.1.1
- Spring Web y Spring Data JPA (Hibernate)
- Spring Security y autenticación JWT
- PostgreSQL
- Maven / Maven Wrapper
- IntelliJ IDEA
- pgAdmin 4 y Postman (opcionales)
- Git y GitHub

## 2. Arquitectura del proyecto

El backend es una **aplicación Spring Boot única** organizada por responsabilidades; no utiliza microservicios ni Eureka.

```text
TiendaSebas/
├── mvnw / mvnw.cmd
├── pom.xml
├── .gitignore
└── src/
    └── main/
        ├── java/com/tiendasebas/tiendasebas/
        │   ├── config/         # Configuración y CORS
        │   ├── controller/     # Endpoints REST
        │   ├── dto/            # Solicitudes y respuestas
        │   ├── entity/         # Entidades JPA
        │   ├── exception/      # Manejo de errores
        │   ├── repository/     # Acceso a PostgreSQL
        │   ├── security/       # Seguridad JWT
        │   └── service/        # Reglas de negocio
        └── resources/
            ├── application.properties
            ├── application-dev.properties
            ├── application-prod.properties
            └── db/migration/  # SQL de referencia del proyecto
```

El frontend Angular consume la API en `http://localhost:8080` durante el desarrollo.

## 3. Requisitos previos

1. **JDK 21** instalado y configurado.
2. **PostgreSQL** instalado y en ejecución (por defecto, puerto `5432`).
3. **IntelliJ IDEA** o un editor compatible con Java y Maven.
4. **Git** para clonar el repositorio.
5. **pgAdmin 4** y **Postman** (opcionales).

Verificar desde PowerShell:

```powershell
java -version
git --version
```

El proyecto incluye **Maven Wrapper** (`mvnw.cmd`), por lo que no es obligatorio instalar Maven globalmente.

## 4. Descargar el proyecto desde GitHub

```powershell
cd D:\Proyectos
git clone https://github.com/Alexan-22/tienda-sebas-backend.git
cd tienda-sebas-backend
```

> Si el repositorio es privado, necesitarás una cuenta con permisos de acceso. También puedes abrir la carpeta local del proyecto en IntelliJ IDEA.

## 5. Abrir el proyecto en IntelliJ IDEA

1. Abrir **IntelliJ IDEA → File → Open**.
2. Seleccionar la carpeta del backend.
3. Esperar la importación de dependencias Maven.
4. Configurar el **Project SDK** en **Java 21**.
5. Comprobar que PostgreSQL esté activo.

## 6. Configurar PostgreSQL

Desde **pgAdmin 4**:

1. Conectarse al servidor PostgreSQL local.
2. En **Databases**, seleccionar **Create → Database**.
3. Crear una base de datos llamada **`TiendaSebas`**.
4. Comprobar que el usuario configurado tenga acceso a esa base de datos.

Valores locales de referencia:

| Parámetro | Valor |
|---|---|
| Host | `localhost` |
| Puerto | `5432` |
| Base de datos | `TiendaSebas` |
| Usuario de ejemplo | `postgres` |

**Importante:** crear la base de datos vacía no equivale a recuperar los datos existentes. Para trasladar la tienda real se necesita una **copia de seguridad y restauración** de PostgreSQL. No subir respaldos con datos de clientes ni credenciales al repositorio.

## 7. Configuración de perfiles y variables de entorno

Los archivos de configuración son:

- `application.properties`: configuración común y selección del perfil.
- `application-dev.properties`: desarrollo local; Hibernate utiliza `ddl-auto=update`.
- `application-prod.properties`: producción; Hibernate utiliza `ddl-auto=validate`.

**Nunca escribir contraseñas reales ni claves JWT en archivos versionados.** Para iniciar localmente, configurar en la **misma terminal** PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:DB_PASSWORD = "TU_PASSWORD_LOCAL"
$env:JWT_SECRET = "TU_CLAVE_JWT_SEGURA"
```

Reemplazar los valores de ejemplo en tu computadora, **sin compartirlos ni copiarlos a GitHub**. La clave JWT debe ser suficientemente larga y aleatoria según el algoritmo configurado.

Otras variables admitidas:

| Variable | Uso | Desarrollo |
|---|---|---|
| `DB_URL` | JDBC PostgreSQL | `jdbc:postgresql://localhost:5432/TiendaSebas` |
| `DB_USERNAME` | Usuario de PostgreSQL | `postgres` |
| `DB_PASSWORD` | Contraseña PostgreSQL | Obligatoria |
| `JWT_SECRET` | Clave de firma JWT | Obligatoria |
| `SPRING_PROFILES_ACTIVE` | Perfil | `dev` |
| `PORT` | Puerto HTTP | `8080` |
| `JWT_EXPIRATION` | Vigencia del token en ms | `86400000` |

Para **producción** también se requieren `UPLOAD_DIR` (directorio persistente de imágenes) y `CORS_ALLOWED_ORIGINS` (origen HTTPS real del frontend), además de los datos de conexión y la clave JWT. No utilizar valores de prueba en producción.

## 8. Ejecutar el backend

Desde la carpeta raíz del backend, en la misma terminal donde configuraste las variables:

```powershell
.\mvnw.cmd spring-boot:run
```

Si inicia correctamente, el servidor estará disponible en:

**http://localhost:8080**

No cerrar la terminal mientras se utiliza la aplicación. Para detenerla, presionar **Ctrl + C**.

Para comprobar compilación:

```powershell
.\mvnw.cmd clean package
```

## 9. Funcionalidades principales

| Módulo | Descripción |
|---|---|
| Autenticación y usuarios | Registro, inicio de sesión, roles `ADMIN` y `CLIENTE` |
| Catálogo | Productos por género y categoría, precios e imágenes |
| Inventario | Tallas, stock y estado de productos |
| Compra online | Checkout, pedidos y detalle de compra |
| Administración | Gestión de productos, usuarios, ventas y existencias |
| POS | Clientes presenciales y ventas en tienda |
| Imágenes | Carga y consulta de imágenes de productos |
| Reportes | Datos de ventas para el frontend administrativo |

Las operaciones administrativas requieren autenticación y permisos apropiados.

## 10. Probar la API con Postman

Iniciar el backend y abrir Postman. Algunas rutas conocidas del proyecto:

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `http://localhost:8080/api/generos` | Consultar géneros |
| `GET` | `http://localhost:8080/api/categorias` | Consultar categorías |
| `GET` | `http://localhost:8080/api/productos/public` | Listar productos públicos |
| `GET` | `http://localhost:8080/api/productos/public/{id}` | Consultar producto por ID |

Ejemplo de consulta:

```http
GET http://localhost:8080/api/productos/public
```

Para rutas protegidas, iniciar sesión con una cuenta autorizada y enviar el JWT correspondiente en el encabezado:

```http
Authorization: Bearer TU_TOKEN
```

Las rutas y cuerpos completos de administración, compra y POS deben consultarse en los controladores y DTO actuales del proyecto antes de integrarlos o probarlos.

## 11. Imágenes de productos

En desarrollo se utiliza:

```text
uploads/productos/
```

El proyecto admite cargas de hasta **10 MB** según la configuración multipart. Las imágenes locales están excluidas de Git mediante `.gitignore`.

En producción se necesita un **almacenamiento persistente** configurado mediante `UPLOAD_DIR`, con una estrategia de respaldo y migración de las imágenes existentes. No confiar en un disco temporal del servicio de alojamiento.

## 12. Problemas frecuentes y soluciones

### Error de autenticación PostgreSQL

```text
FATAL: password authentication failed for user "postgres"
```

Verificar `DB_PASSWORD`, `DB_USERNAME`, permisos y que las variables estén definidas en la misma terminal del proceso.

### Base de datos inexistente

```text
database "TiendaSebas" does not exist
```

Crear o restaurar la base de datos y revisar `DB_URL`.

### Puerto 8080 ocupado

```powershell
netstat -ano | findstr :8080
```

Identificar qué aplicación utiliza el puerto antes de cerrar procesos.

### JWT no configurado o inválido

Comprobar `JWT_SECRET`, el perfil activo y el formato del token. Cambiar la clave JWT invalida los tokens anteriores.

### CORS: Angular no puede conectarse

Comprobar que el frontend de desarrollo use `http://localhost:4200` y que `app.cors.allowed-origins` tenga el origen correcto. En producción configurar `CORS_ALLOWED_ORIGINS` con la dirección real del frontend.

### Producción falla al validar tablas

`ddl-auto=validate` **no crea tablas**. Restaurar o migrar el esquema PostgreSQL compatible antes de arrancar el backend de producción. Revisar también los cambios SQL aplicados manualmente en la base local.

## 13. Seguridad y recomendaciones

- No subir contraseñas, secretos JWT, archivos `.env` ni respaldos privados.
- Rotar las credenciales que hayan sido incluidas anteriormente en commits, incluso si después se quitaron.
- Utilizar HTTPS y restringir CORS al frontend autorizado.
- No usar cuentas administrativas compartidas ni contraseñas de ejemplo.
- Hacer copias de seguridad de PostgreSQL y del almacenamiento de imágenes.
- Validar permisos y roles antes de ejecutar operaciones administrativas.
- **Importante para la puesta en producción:** la confirmación de Yape en el flujo actual no verifica automáticamente un pago real y los pagos con tarjeta de demostración no constituyen una pasarela real. Verificar manualmente los pagos antes de entregar pedidos y no aceptar datos reales de tarjeta mediante el flujo de demostración.

## 14. Preparación para producción

1. Preparar PostgreSQL de producción y migrar datos y esquema.
2. Rotar `DB_PASSWORD` y `JWT_SECRET` previamente expuestos.
3. Configurar variables de entorno del servicio de alojamiento.
4. Configurar almacenamiento persistente para `UPLOAD_DIR`.
5. Activar el perfil `prod` y validar que las tablas coincidan con las entidades.
6. Desplegar el backend y obtener su URL HTTPS definitiva.
7. Configurar el frontend Angular con esa URL y actualizar `CORS_ALLOWED_ORIGINS`.
8. Probar autenticación, catálogo, carrito, compras, POS, imágenes y reportes.
9. Configurar copias de seguridad y supervisión de errores.

## 15. Repositorio

**Proyecto:** Tienda Sebas – Backend  
**Repositorio:** https://github.com/Alexan-22/tienda-sebas-backend  
**Arquitectura:** API REST monolítica por capas con Spring Boot y PostgreSQL.  
**Frontend:** Angular, TypeScript y Tailwind CSS.
