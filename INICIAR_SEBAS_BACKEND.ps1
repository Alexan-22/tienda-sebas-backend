
cd "D:\Proyectos\TiendaSebas\TiendaSebas"

$ErrorActionPreference = "Stop"

Write-Host "`n=== CONFIGURANDO SUPABASE ===" -ForegroundColor Cyan

# Datos reales desde Supabase > Connect > Session pooler
$servidor = (Read-Host "HOST real del Session Pooler").Trim()
$usuario = (Read-Host "USER real del Session Pooler").Trim()

if ($servidor -notmatch '^[a-zA-Z0-9.-]+\.pooler\.supabase\.com$') {
    throw "Introduce el HOST real del Session Pooler, sin puerto ni protocolo."
}

if ($usuario -notmatch '^postgres\.[a-zA-Z0-9_-]+$') {
    throw "Introduce el usuario completo del Session Pooler."
}

$env:DB_URL = "jdbc:postgresql://${servidor}:5432/postgres?sslmode=require"
$env:DB_USERNAME = $usuario

# Contraseña de PostgreSQL de Supabase
$segura = Read-Host "DB_PASSWORD de Supabase" -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($segura)

try {
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
}
finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
}

# JWT original del sistema
if ([string]::IsNullOrWhiteSpace($env:JWT_SECRET)) {
    $jwt = Read-Host "JWT_SECRET original" -AsSecureString
    $ptrJwt = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($jwt)

    try {
        $env:JWT_SECRET = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptrJwt)
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptrJwt)
    }
}

$env:SPRING_PROFILES_ACTIVE = "dev"
$env:APP_TENANT_BASE_DOMAIN = "saas.test"
$env:APP_TENANT_LOCAL_SUBDOMAIN = "sebascollection"

Write-Host "`nCONFIGURACION PREPARADA" -ForegroundColor Green

# Iniciar Spring Boot
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--app.tenant.base-domain=saas.test --app.tenant.local-subdomain=sebascollection"
