$apiUrl = "https://www.qclid.space/api/plugin-version"

Write-Host "`n[API Checker] Testing endpoint: $apiUrl" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri $apiUrl -Method Get -ErrorAction Stop

    Write-Host "[SUCCESS] API is reachable." -ForegroundColor Green

    $hasVersion = $null -ne $response.version
    $hasDownloadUrl = $null -ne $response.downloadUrl

    if ($hasVersion -and $hasDownloadUrl) {
        Write-Host "[SUCCESS] JSON structure is correct." -ForegroundColor Green
        Write-Host "  - Latest Version: $($response.version)"
        Write-Host "  - Download URL  : $($response.downloadUrl)"
    } else {
        Write-Host "[ERROR] JSON is missing required fields." -ForegroundColor Red
        if (-not $hasVersion) { Write-Host "  - Missing 'version' field." }
        if (-not $hasDownloadUrl) { Write-Host "  - Missing 'downloadUrl' field." }
    }
} catch {
    Write-Host "[FAILURE] Could not reach API or it returned an error." -ForegroundColor Red
    Write-Host "  - Status Code: $($_.Exception.Response.StatusCode.Value__)"
    Write-Host "  - Error: $($_.Exception.Message)"
}

Write-Host "`nCheck complete.`n"
