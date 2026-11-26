# Arranque de Boulder en un servidor Windows (campamento). Lee las variables de
# deploy\boulder.env (mismo formato que en Linux) y levanta el jar en primer plano.
# Para arrancar con el equipo: Programador de tareas, desencadenador "Al iniciar",
# acción "powershell -ExecutionPolicy Bypass -File C:\boulder\deploy\iniciar.ps1".
param(
    [string]$Jar = (Join-Path $PSScriptRoot "..\target\boulder-drilling.jar"),
    [string]$Entorno = (Join-Path $PSScriptRoot "boulder.env")
)

if (-not (Test-Path $Entorno)) { throw "Falta $Entorno (copiar de boulder.env.example)" }
Get-Content $Entorno | Where-Object { $_ -match '^\s*[A-Z_]+=' } | ForEach-Object {
    $nombre, $valor = $_ -split '=', 2
    [Environment]::SetEnvironmentVariable($nombre.Trim(), $valor.Trim(), 'Process')
}

& java -Xms256m -Xmx768m -jar $Jar
