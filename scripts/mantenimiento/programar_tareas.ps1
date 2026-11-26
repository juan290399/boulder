# Registra en el Programador de tareas de Windows el respaldo diario (2:00) y
# la verificación de salud (cada 5 minutos). En Linux, el equivalente está en
# crontab.txt. Ejecutar en una consola de PowerShell como el usuario del servicio.
#
# Uso:  powershell -ExecutionPolicy Bypass -File scripts\mantenimiento\programar_tareas.ps1 [-Respaldos D:\respaldos\boulder]
param(
    [string]$Respaldos = "D:\respaldos\boulder",
    [string]$Url = "http://localhost:8080"
)

$raiz = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$bash = "C:\Program Files\Git\bin\bash.exe"
if (-not (Test-Path $bash)) { throw "Hace falta Git Bash en $bash para correr los scripts .sh" }

$respaldo = "`"$raiz\scripts\mantenimiento\respaldo_bd.sh`" `"$Respaldos`""
$salud = "`"$raiz\scripts\mantenimiento\verificar_salud.sh`" $Url"

schtasks /Create /F /TN "Boulder\Respaldo diario" /SC DAILY /ST 02:00 /TR "`"$bash`" $respaldo"
schtasks /Create /F /TN "Boulder\Verificar salud" /SC MINUTE /MO 5 /TR "`"$bash`" $salud"

schtasks /Query /TN "Boulder\Respaldo diario"
schtasks /Query /TN "Boulder\Verificar salud"
