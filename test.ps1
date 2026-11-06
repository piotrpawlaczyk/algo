# Szybki smoke test Twojego serwera (Czesc 2). Odpal przy dzialajacym serwerze:
#     powershell -ExecutionPolicy Bypass -File test.ps1
# Inny port:  $env:PORT = "8081"; powershell -ExecutionPolicy Bypass -File test.ps1
#
# To tylko pomoc, a nie ocena. Skrypt nie rusza Twojego kodu, tylko wysyla requesty.
# (A -ExecutionPolicy Bypass jest tylko dla tego jednego skryptu, systemu nie rozwalamy.)

$port = if ($env:PORT) { $env:PORT } else { "8080" }
$base = "http://127.0.0.1:$port"
$script:passed = 0
$script:total = 0
$stamp = Get-Date -Format "HHmmss"

# Zwraca Code (0 = nie ma polaczenia) i Body. PS 5 rzuca wyjatkiem przy 4xx, stad ten try/catch.
function Send-Request($method, $path, $body) {
    $params = @{ Method = $method; Uri = "$base$path"; UseBasicParsing = $true; TimeoutSec = 5 }
    if ($null -ne $body) {
        $params.ContentType = "application/json"
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes($body)
    }
    try {
        $r = Invoke-WebRequest @params
        return @{ Code = [int]$r.StatusCode; Body = [string]$r.Content }
    } catch {
        $resp = $_.Exception.Response
        if ($null -eq $resp) { return @{ Code = 0; Body = "" } }
        $text = ""
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) { $text = $_.ErrorDetails.Message }
        return @{ Code = [int]$resp.StatusCode; Body = $text }
    }
}

function Check($label, $ok, $r) {
    $script:total++
    if ($ok) { $script:passed++; $mark = "OK  " } else { $mark = "ZLE " }
    $oneLine = ($r.Body -replace '\s*\r?\n\s*', ' ').Trim()
    "{0}  {1,-52} {2} {3}" -f $mark, $label, $r.Code, $oneLine
}

function Get-Id($text) {
    if ($text -match '"id"\s*:\s*(\d+)') { return $Matches[1] }
    return $null
}

$r = Send-Request GET "/health" $null
if ($r.Code -eq 0) {
    "Serwer nie odpowiada pod $base - na pewno go odpaliles/odpalilas?"
    exit 1
}
Check "GET /health -> 200" ($r.Code -eq 200) $r

$r = Send-Request GET "/tasks" $null
Check "GET /tasks -> 200 i lista [...]" ($r.Code -eq 200 -and $r.Body.TrimStart().StartsWith("[")) $r

$r = Send-Request POST "/tasks" "{`"title`": `"Test A $stamp`"}"
$idA = Get-Id $r.Body
Check "POST poprawny tytul -> 201 z id" ($r.Code -eq 201 -and $idA) $r

$r = Send-Request POST "/tasks" "{`"title`": `"Test B $stamp`"}"
$idB = Get-Id $r.Body
Check "POST drugi tytul -> 201 i inne id" ($r.Code -eq 201 -and $idB -and $idA -ne $idB) $r

$r = Send-Request POST "/tasks" '{"title": "   "}'
Check "POST same spacje -> 400" ($r.Code -eq 400) $r

$r = Send-Request POST "/tasks" '{}'
Check "POST bez tytulu -> 400" ($r.Code -eq 400) $r

$r = Send-Request GET "/tasks" $null
$ok = $r.Body.Contains("Test A $stamp") -and $r.Body.Contains("Test B $stamp") -and -not ($r.Body -match '"title"\s*:\s*"\s*"')
Check "GET /tasks -> sa oba nowe zadania, nie ma pustych" $ok $r

""
"Wynik: $($script:passed)/$($script:total)"
