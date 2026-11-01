param(
    [string]$Proyecto = 'D:\Proyectos\TiendaSebas\TiendaSebas'
)

$ErrorActionPreference = 'Stop'
$base = Split-Path -Parent $MyInvocation.MyCommand.Path
$origen = Join-Path $base 'archivos'
$codigo = Join-Path $Proyecto 'src\main\java\com\tiendasebas\tiendasebas'
$marca = Get-Date -Format 'yyyyMMdd_HHmmss'
$respaldo = Join-Path (Split-Path -Parent $Proyecto) ("RESPALDO_TIENDA_SEBAS_" + $marca)

if (-not (Test-Path (Join-Path $Proyecto 'pom.xml'))) {
    throw "No se encontro pom.xml en $Proyecto. Verifica la ruta."
}
if (-not (Test-Path $origen)) {
    throw "No existe la carpeta de archivos del parche: $origen"
}
New-Item -ItemType Directory -Path $respaldo -Force | Out-Null
Write-Host "Respaldo previo: $respaldo" -ForegroundColor Cyan

function Guardar-Original([string]$archivo) {
    if (Test-Path -LiteralPath $archivo) {
        $rel = $archivo.Substring($Proyecto.Length).TrimStart('\')
        $copia = Join-Path $respaldo $rel
        New-Item -ItemType Directory -Path (Split-Path $copia -Parent) -Force | Out-Null
        Copy-Item -LiteralPath $archivo -Destination $copia -Force
    }
}

# Respaldar pom y los archivos Java a actualizar
$archivosParche = Get-ChildItem -LiteralPath $origen -Recurse -File
foreach ($item in $archivosParche) {
    $rel = $item.FullName.Substring($origen.Length).TrimStart('\')
    $destino = Join-Path $Proyecto $rel
    Guardar-Original $destino
    New-Item -ItemType Directory -Path (Split-Path $destino -Parent) -Force | Out-Null
    Copy-Item -LiteralPath $item.FullName -Destination $destino -Force
    Write-Host "ACTUALIZADO $rel"
}

# Sustituir importaciones antiguas que existen en ProductoService, VentaService y CategoriaService
$reemplazos = [ordered]@{
  'com.tiendasebas.config.tenant.TenantContext' = 'com.tiendasebas.tiendasebas.config.TenantContext'
  'com.tiendasebas.repository.TiendaRepository' = 'com.tiendasebas.tiendasebas.repository.TiendaRepository'
  'com.tiendasebas.entity.Tienda' = 'com.tiendasebas.tiendasebas.entity.Tienda'
}
Get-ChildItem -LiteralPath $codigo -Recurse -File -Filter '*.java' | ForEach-Object {
    $archivo = $_.FullName
    $original = [System.IO.File]::ReadAllText($archivo)
    $nuevo = $original
    foreach ($viejo in $reemplazos.Keys) {
        $nuevo = $nuevo.Replace($viejo, $reemplazos[$viejo])
    }
    if ($nuevo -cne $original) {
        Guardar-Original $archivo
        [System.IO.File]::WriteAllText($archivo, $nuevo, (New-Object System.Text.UTF8Encoding($false)))
        Write-Host "IMPORT CORREGIDO $archivo"
    }
}

# Retirar solo las clases antiguas que registraban un segundo resolvedor de tenant
$interceptorAntiguo = Join-Path $codigo 'config\TenantInterceptor.java'
$configAntigua = Join-Path $codigo 'config\WebMvcConfig.java'
if (Test-Path $interceptorAntiguo) {
    $texto = [System.IO.File]::ReadAllText($interceptorAntiguo)
    if ($texto.Contains('class TenantInterceptor')) {
        Guardar-Original $interceptorAntiguo
        Remove-Item -LiteralPath $interceptorAntiguo -Force
        Write-Host 'RETIRADO TenantInterceptor.java (copia en respaldo)'
    }
}
if (Test-Path $configAntigua) {
    $texto = [System.IO.File]::ReadAllText($configAntigua)
    if ($texto.Contains('addInterceptor(tenantInterceptor)')) {
        Guardar-Original $configAntigua
        Remove-Item -LiteralPath $configAntigua -Force
        Write-Host 'RETIRADO WebMvcConfig.java antiguo (copia en respaldo)'
    } else {
        Write-Warning 'Revisa WebMvcConfig.java: no se retiro porque puede contener otras configuraciones.'
    }
}

# Configurar Lombok explicitamente como procesador de anotaciones de Maven
$pomPath = Join-Path $Proyecto 'pom.xml'
Guardar-Original $pomPath
$pom = [System.IO.File]::ReadAllText($pomPath)
$pluginLombok = @'
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <release>21</release>
    <proc>full</proc>
    <annotationProcessorPaths>
      <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
'@
$patron = '(?s)<plugin>\s*<groupId>org\.apache\.maven\.plugins</groupId>\s*<artifactId>maven-compiler-plugin</artifactId>.*?</plugin>'
if ([regex]::IsMatch($pom, $patron)) {
    $re = [regex]::new($patron)
    $pom = $re.Replace($pom, [System.Text.RegularExpressions.MatchEvaluator]{ param($m) $pluginLombok }, 1)
    [System.IO.File]::WriteAllText($pomPath, $pom, (New-Object System.Text.UTF8Encoding($false)))
    Write-Host 'MAVEN: annotationProcessorPaths para Lombok configurado.'
} else {
    Write-Warning 'No se encontro el plugin maven-compiler-plugin; configura Lombok manualmente antes de compilar.'
}

Write-Host ''
Write-Host 'Archivos respaldados en:' $respaldo -ForegroundColor Green
Write-Host 'Ahora ejecuta en la terminal del proyecto: .\mvnw.cmd clean test' -ForegroundColor Green
Write-Host 'No subas a GitHub hasta confirmar BUILD SUCCESS y revisar las pruebas.' -ForegroundColor Yellow
