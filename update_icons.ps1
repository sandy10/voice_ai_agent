Add-Type -AssemblyName System.Drawing

$srcImg = "C:\Users\hp\.gemini\antigravity\brain\24390e3b-d2d3-460d-b8c3-409841a759f7\agora_ai_app_icon_1787339226541.jpg"
$resDir = "d:\AI_agent\voice_ai_agent\app\src\main\res"

$sizes = @{
    "mdpi" = 48
    "hdpi" = 72
    "xhdpi" = 96
    "xxhdpi" = 144
    "xxxhdpi" = 192
}

# Remove old XML files
$fgXml = Join-Path $resDir "drawable\ic_launcher_foreground.xml"
$bgXml = Join-Path $resDir "drawable\ic_launcher_background.xml"
if (Test-Path $fgXml) { Remove-Item $fgXml -Force }
if (Test-Path $bgXml) { Remove-Item $bgXml -Force }

$img = [System.Drawing.Image]::FromFile($srcImg)

# Foreground for adaptive icon
$fgPath = Join-Path $resDir "drawable\ic_launcher_foreground.png"
$fgBmp = New-Object System.Drawing.Bitmap(432, 432)
$g = [System.Drawing.Graphics]::FromImage($fgBmp)
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$g.DrawImage($img, 0, 0, 432, 432)
$g.Dispose()
if (!(Test-Path (Join-Path $resDir "drawable"))) { New-Item -ItemType Directory -Path (Join-Path $resDir "drawable") | Out-Null }
$fgBmp.Save($fgPath, [System.Drawing.Imaging.ImageFormat]::Png)
$fgBmp.Dispose()

# Create a solid color for the background
$bgPath = Join-Path $resDir "drawable\ic_launcher_background.xml"
$bgXmlContent = @"
<?xml version="1.0" encoding="utf-8"?>
<color xmlns:android="http://schemas.android.com/apk/res/android"
    android:color="#FFFFFF"/>
"@
Set-Content -Path $bgPath -Value $bgXmlContent -Encoding UTF8

# Update legacy icons
foreach ($key in $sizes.Keys) {
    $size = $sizes[$key]
    $folder = Join-Path $resDir "mipmap-$key"
    if (!(Test-Path $folder)) { New-Item -ItemType Directory -Path $folder | Out-Null }
    
    $bmp = New-Object System.Drawing.Bitmap($size, $size)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.DrawImage($img, 0, 0, $size, $size)
    $g.Dispose()
    
    $bmp.Save((Join-Path $folder "ic_launcher.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save((Join-Path $folder "ic_launcher_round.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
}

$img.Dispose()
Write-Output "Icons updated successfully!"
