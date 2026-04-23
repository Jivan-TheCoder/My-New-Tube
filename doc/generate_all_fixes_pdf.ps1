$ErrorActionPreference = "Stop"

$outPath = Join-Path (Get-Location) "doc\all-errors-fixes-detailed-hinglish.pdf"

$blocks = @(
    @{ Kind = "TITLE"; Text = "My New Tube - Full Error Fix and Change Report" },
    @{ Kind = "SUBTITLE"; Text = "Detailed Hinglish Team Document (Problem -> Root Cause -> Fix -> Impact)" },
    @{ Kind = "HIGHLIGHT"; Text = "Scope: This report summarizes major fixes implemented from project customization start till current state." },
    @{ Kind = "SPACE"; Text = "" },

    @{ Kind = "H1"; Text = "1) Executive Summary" },
    @{ Kind = "P"; Text = "Project ko YouTube-only banane ke saath humne stability, UI simplification, import reliability, release build safety, aur user-facing error UX pe focused fixes kiye." },
    @{ Kind = "P"; Text = "Most critical wins: service isolation, non-YouTube import option removal, ZIP/CSV import fallback parsing, localization refresh correctness, and crash-noise reduction." },
    @{ Kind = "HIGHLIGHT"; Text = "Main outcome: app now behaves more predictable for regular users, especially during extractor-side temporary failures." },
    @{ Kind = "SPACE"; Text = "" },

    @{ Kind = "H1"; Text = "2) Detailed Fix Log" },

    @{ Kind = "H2"; Text = "Issue A: Subscriptions Import menu me SoundCloud option dikhta tha, click par crash risk" },
    @{ Kind = "P"; Text = "Problem: Import from menu me SoundCloud service aa raha tha, jabki app YouTube-only hai." },
    @{ Kind = "P"; Text = "Root cause: menu build loop ServiceList.all() use kar raha tha." },
    @{ Kind = "P"; Text = "Fix: loop ko ServiceHelper.getSupportedServices() pe switch kiya (already YouTube-only)." },
    @{ Kind = "P"; Text = "Extra safety: unsupported service id aaye to safe fallback service id use hota hai." },
    @{ Kind = "CODE"; Text = "for (service in ServiceHelper.getSupportedServices()) { ... }" },
    @{ Kind = "CODE"; Text = "val safeServiceId = if (serviceId in supportedServiceIds) serviceId else supportedServiceIds.firstOrNull() ?: serviceId" },
    @{ Kind = "FILES"; Text = "Files: app/src/main/java/org/schabi/newpipe/local/subscription/SubscriptionFragment.kt" },
    @{ Kind = "RESULT"; Text = "Result: Import menu me SoundCloud option removed; click path YouTube-only safe." },
    @{ Kind = "SPACE"; Text = "" },

    @{ Kind = "H2"; Text = "Issue B: Subscription ZIP/CSV import unreliable tha" },
    @{ Kind = "P"; Text = "Problem: valid ZIP ya CSV file bhi kuch devices par fail hoti thi." },
    @{ Kind = "P"; Text = "Root cause: parsing me single guessed MIME type use hota tha, SAF MIME inconsistency handle nahi hoti thi." },
    @{ Kind = "P"; Text = "Fix: multi-MIME candidate + retry parser + partial-success import." },
    @{ Kind = "CODE"; Text = "DEFAULT_MIME_CANDIDATES = application/zip, zip, text/csv, csv, application/json, json" },
    @{ Kind = "CODE"; Text = "parseSubscriptionsFromBytes(...) tries each MIME until success" },
    @{ Kind = "CODE"; Text = "Partial toast: Imported X subscriptions, skipped Y" },
    @{ Kind = "FILES"; Text = "Files: .../SubscriptionImportWorker.kt, .../values/strings.xml" },
    @{ Kind = "RESULT"; Text = "Result: import flow significantly more resilient across file managers/devices." },
    @{ Kind = "SPACE"; Text = "" },

    @{ Kind = "H2"; Text = "Issue C: App me non-YouTube services ka leftover behavior" },
    @{ Kind = "P"; Text = "Problem: drawer/services/router/settings me legacy services remnants the." },
    @{ Kind = "P"; Text = "Root cause: old multi-service architecture still active in some paths." },
    @{ Kind = "P"; Text = "Fix set:" },
    @{ Kind = "BULLET"; Text = "ServiceHelper restricted to SUPPORTED_SERVICES = [YouTube]." },
    @{ Kind = "BULLET"; Text = "RouterActivity rejects non-YouTube URLs." },
    @{ Kind = "BULLET"; Text = "Manifest deep links for Hooktube/Invidious/SoundCloud/PeerTube/Bandcamp/media.ccc removed." },
    @{ Kind = "BULLET"; Text = "SelectKioskFragment now enumerates YouTube only." },
    @{ Kind = "BULLET"; Text = "Migration v9 cleans non-YouTube tabs from stored tabs." },
    @{ Kind = "FILES"; Text = "Files: ServiceHelper.kt, RouterActivity.java, AndroidManifest.xml, SelectKioskFragment.java, SettingMigrations.java" },
    @{ Kind = "RESULT"; Text = "Result: service-layer behavior aligned with YouTube-only product direction." },
    @{ Kind = "SPACE"; Text = "" },

    @{ Kind = "H2"; Text = "Issue D: Drawer me unwanted complexity aur wrong options" },
    @{ Kind = "P"; Text = "Problem: drawer me extra options/services and UX confusion." },
    @{ Kind = "P"; Text = "Fix set:" },
    @{ Kind = "BULLET"; Text = "Service switch/toggle interaction disabled in header." },
    @{ Kind = "BULLET"; Text = "Drawer header service row visibility hidden." },
    @{ Kind = "BULLET"; Text = 'Trending drawer entry hidden by shouldHideDrawerKiosk("Trending").' },
    @{ Kind = "BULLET"; Text = "Rate App and Share App actions added below settings section." },
    @{ Kind = "BULLET"; Text = "Filled star icon added for rate app." },
    @{ Kind = "FILES"; Text = "Files: MainActivity.java, drawer_header.xml, values/strings.xml, drawable/ic_star_filled.xml" },
    @{ Kind = "RESULT"; Text = "Result: drawer cleaner, action-oriented, and YouTube-focused." },
    @{ Kind = "SPACE"; Text = "" },

    @{ Kind = "H2"; Text = "Issue E: Video detail controls mismatch (Add To/Popup/Share/Download requirements)" },
    @{ Kind = "P"; Text = "Problem: requested controls order/visibility ke hisab se UI consistent nahi tha." },
    @{ Kind = "P"; Text = "Fix set:" },
    @{ Kind = "BULLET"; Text = "Primary control row behavior adjusted; download path enabled directly." },
    @{ Kind = "BULLET"; Text = "Background slot reused for share in requested layout customization." },
    @{ Kind = "BULLET"; Text = "Description tab creation path removed from tab adapter flow." },
    @{ Kind = "BULLET"; Text = "Default selected tab moved to related/next side where needed." },
    @{ Kind = "FILES"; Text = "Files: VideoDetailFragment.java, layout/fragment_video_detail.xml" },
    @{ Kind = "RESULT"; Text = "Result: controls now closer to requested simplified action model." },
    @{ Kind = "SPACE"; Text = "" },

    @{ Kind = "H2"; Text = "Issue F: Player top globe icon (open in browser) hide request" },
    @{ Kind = "P"; Text = "Problem: player top controls me globe icon visible tha." },
    @{ Kind = "P"; Text = "Fix: openInBrowser button ko hidden parent container me wrap kiya." },
    @{ Kind = "CODE"; Text = "<FrameLayout android:visibility='gone'> ... openInBrowser ... </FrameLayout>" },
    @{ Kind = "FILES"; Text = "Files: app/src/main/res/layout/player.xml" },
    @{ Kind = "RESULT"; Text = "Result: globe icon no longer appears in player controls." },
    @{ Kind = "SPACE"; Text = "" },

    @{ Kind = "H2"; Text = "Issue G: Settings pages ko hide/simplify karna tha without full removal in many cases" },
    @{ Kind = "P"; Text = "Approach: Preference.setVisible(false) based hiding used across fragments, so rollback easy rahe." },
    @{ Kind = "P"; Text = "Covered sections:" },
    @{ Kind = "BULLET"; Text = "Video & Audio: extra player behavior/debug style options hidden." },
    @{ Kind = "BULLET"; Text = "Download: advanced retry/network/queue/file-char options hidden." },
    @{ Kind = "BULLET"; Text = "Appearance: non-essential toggles hidden." },
    @{ Kind = "BULLET"; Text = "Content: feed and extra switches hidden as requested." },
    @{ Kind = "FILES"; Text = "Files: VideoAudioSettingsFragment.java, DownloadSettingsFragment.java, AppearanceSettingsFragment.java, ContentSettingsFragment.java" },
    @{ Kind = "RESULT"; Text = "Result: settings UX cleaner and lower-risk for end users." },
    @{ Kind = "SPACE"; Text = "" },

    @{ Kind = "H2"; Text = "Issue H: Main settings me Notifications, Updates, Backup/Restore remove request" },
    @{ Kind = "P"; Text = "Fix set: entries removed from main_settings.xml and registry mappings removed." },
    @{ Kind = "P"; Text = "Related module removals: update checker worker/settings and notification settings xml fragments deleted." },
    @{ Kind = "FILES"; Text = "Files: main_settings.xml, SettingsResourceRegistry.java, UpdateSettingsFragment.java (deleted), NewVersionWorker.kt (deleted), notifications/backup xml files (deleted)" },
    @{ Kind = "RESULT"; Text = "Result: user-facing settings surface strictly reduced to required categories." },
    @{ Kind = "SPACE"; Text = "" },

    @{ Kind = "H2"; Text = "Issue I: Trending content country/language change immediately reflect nahi ho raha tha" },
    @{ Kind = "P"; Text = "Problem: content settings change ke baad kiosk content stale lag raha tha." },
    @{ Kind = "P"; Text = "Root cause: localization and cache refresh not applied consistently on resume/change." },
    @{ Kind = "P"; Text = "Fix set:" },
    @{ Kind = "BULLET"; Text = "ContentSettingsFragment now applies localization on language/country preference change." },
    @{ Kind = "BULLET"; Text = "InfoCache clear + PlayerHelper.resetFormat called after localization update." },
    @{ Kind = "BULLET"; Text = "KioskFragment now tracks country + language; if changed, re-setup localization + clear cache + reload." },
    @{ Kind = "FILES"; Text = "Files: ContentSettingsFragment.java, KioskFragment.java" },
    @{ Kind = "RESULT"; Text = "Result: trending/music/live feeds respond better to selected content region/language." },
    @{ Kind = "SPACE"; Text = "" },

    @{ Kind = "H2"; Text = "Issue J: Parsing error snackbar/report UX too technical" },
    @{ Kind = "P"; Text = "Problem: users ko technical error screen/log clutter dikh raha tha, trust impact hota tha." },
    @{ Kind = "P"; Text = "Fix set:" },
    @{ Kind = "BULLET"; Text = "ErrorActivity me parsing_error case detect karke friendly message mode enabled." },
    @{ Kind = "BULLET"; Text = "Headline and message text size increased for readability." },
    @{ Kind = "BULLET"; Text = "Technical report controls hidden in friendly mode." },
    @{ Kind = "BULLET"; Text = "Non-fatal extractor-only errors in stream/player paths suppressed where primary data exists." },
    @{ Kind = "FILES"; Text = "Files: ErrorActivity.kt, activity_error.xml, values/strings.xml, VideoDetailFragment.java, Player.java, FeedFragment.kt" },
    @{ Kind = "RESULT"; Text = "Result: users get clearer message, less panic, less noisy false-critical reporting." },
    @{ Kind = "SPACE"; Text = "" },

    @{ Kind = "H2"; Text = "Issue K: Keep Android Open dialog removal" },
    @{ Kind = "P"; Text = "Problem: startup par unwanted policy dialog dikh raha tha." },
    @{ Kind = "P"; Text = "Fix direction: update-check and related consent/popup flow removed from active path." },
    @{ Kind = "FILES"; Text = "Files: MainActivity.java updates + removal of update utility stack" },
    @{ Kind = "RESULT"; Text = "Result: dialog no longer appears in regular flow." },
    @{ Kind = "SPACE"; Text = "" },

    @{ Kind = "H2"; Text = "Issue L: Release-only build requirement, debug APK not wanted" },
    @{ Kind = "P"; Text = "Problem: workflow needed run/install through release path only." },
    @{ Kind = "P"; Text = "Fix set:" },
    @{ Kind = "BULLET"; Text = "Release signing config added via gradle/env/keystore.properties lookup." },
    @{ Kind = "BULLET"; Text = "Release signing missing ho to fail-fast GradleException." },
    @{ Kind = "BULLET"; Text = "Debug variants disabled in androidComponents." },
    @{ Kind = "BULLET"; Text = "keystore.properties.example added for team setup consistency." },
    @{ Kind = "FILES"; Text = "Files: app/build.gradle.kts, keystore.properties.example" },
    @{ Kind = "RESULT"; Text = "Result: release pipeline strict and controlled, accidental debug build reduced." },
    @{ Kind = "SPACE"; Text = "" },

    @{ Kind = "H2"; Text = "Issue M: String encoding artifact risk" },
    @{ Kind = "P"; Text = "Problem: image_quality_summary me corrupted dash style text artifact visible tha." },
    @{ Kind = "P"; Text = "Fix: safe ASCII hyphen style summary text applied." },
    @{ Kind = "FILES"; Text = "Files: app/src/main/res/values/strings.xml (image_quality_summary)" },
    @{ Kind = "RESULT"; Text = "Result: stable render and lower chances of malformed display." },
    @{ Kind = "SPACE"; Text = "" },

    @{ Kind = "H1"; Text = "3) QA Checklist (Must Pass)" },
    @{ Kind = "BULLET"; Text = "Subscriptions -> Import from menu me sirf expected YouTube options dikhen." },
    @{ Kind = "BULLET"; Text = "Import flow ZIP + CSV both test karo; partial failure case me skip count confirm karo." },
    @{ Kind = "BULLET"; Text = "Drawer me Rate App + Share App working verify karo." },
    @{ Kind = "BULLET"; Text = "Player top controls me globe icon visible na ho." },
    @{ Kind = "BULLET"; Text = "Content country/language change ke turant baad kiosk reload behavior verify karo." },
    @{ Kind = "BULLET"; Text = "Parsing error scenario me friendly message screen and larger text confirm karo." },
    @{ Kind = "BULLET"; Text = "Release build signing config check + debug variant disabled validation." },
    @{ Kind = "SPACE"; Text = "" },

    @{ Kind = "H1"; Text = "4) Final Team Note" },
    @{ Kind = "P"; Text = "Is report ka goal transparency hai: exactly kya issue tha, kyu hua, aur code me kis level par fix kiya gaya. Future maintenance me isi template ka reuse recommended hai." },
    @{ Kind = "HIGHLIGHT"; Text = "Recommended process going forward: Every production bug fix = mini section with Problem, Root Cause, Fix, Files, QA proof." }
)

function Escape-PdfText([string]$text) {
    return ($text -replace "\\", "\\\\" -replace "\(", "\\(" -replace "\)", "\\)")
}

function Wrap-Text([string]$text, [int]$maxChars) {
    if ([string]::IsNullOrWhiteSpace($text)) { return @("") }
    $words = $text -split " "
    $lines = New-Object System.Collections.Generic.List[string]
    $current = ""
    foreach ($word in $words) {
        if ($current.Length -eq 0) {
            $current = $word
            continue
        }
        if (($current.Length + 1 + $word.Length) -le $maxChars) {
            $current = "$current $word"
        } else {
            $lines.Add($current)
            $current = $word
        }
    }
    if ($current.Length -gt 0) { $lines.Add($current) }
    return $lines
}

$pageWidth = 595
$pageHeight = 842
$marginLeft = 38
$marginRight = 38
$topY = 806
$bottomY = 42

$pages = New-Object System.Collections.Generic.List[string]
$currentContent = New-Object System.Text.StringBuilder
$currentY = [double]$topY
$pageNumber = 1

function Start-NewPage {
    param(
        [System.Collections.Generic.List[string]]$Pages,
        [ref]$CurrentContent,
        [ref]$CurrentY,
        [int]$PageNo
    )
    if ($CurrentContent.Value.Length -gt 0) {
        $Pages.Add($CurrentContent.Value.ToString())
    }
    $CurrentContent.Value = New-Object System.Text.StringBuilder
    $CurrentY.Value = [double]$topY

    # Page header
    [void]$CurrentContent.Value.AppendLine("BT")
    [void]$CurrentContent.Value.AppendLine("/F1 9 Tf")
    [void]$CurrentContent.Value.AppendLine("0.30 0.30 0.30 rg")
    [void]$CurrentContent.Value.AppendLine("1 0 0 1 38 824 Tm")
    [void]$CurrentContent.Value.AppendLine("(Project Fix Report - Internal Team Copy) Tj")
    [void]$CurrentContent.Value.AppendLine("ET")

    # Page number
    [void]$CurrentContent.Value.AppendLine("BT")
    [void]$CurrentContent.Value.AppendLine("/F1 9 Tf")
    [void]$CurrentContent.Value.AppendLine("0.35 0.35 0.35 rg")
    [void]$CurrentContent.Value.AppendLine("1 0 0 1 520 824 Tm")
    [void]$CurrentContent.Value.AppendLine("(" + (Escape-PdfText ("Page " + $PageNo)) + ") Tj")
    [void]$CurrentContent.Value.AppendLine("ET")
}

Start-NewPage -Pages $pages -CurrentContent ([ref]$currentContent) -CurrentY ([ref]$currentY) -PageNo $pageNumber

foreach ($block in $blocks) {
    $kind = [string]$block.Kind
    $text = [string]$block.Text

    if ($kind -eq "SPACE") {
        $currentY -= 8
        continue
    }

    $font = "/F1"
    $fontSize = 10.5
    $lineHeight = 14.5
    $color = "0.07 0.07 0.07"
    $prefix = ""
    $maxChars = 100
    $drawHighlight = $false

    switch ($kind) {
        "TITLE"    { $font = "/F2"; $fontSize = 19; $lineHeight = 24; $color = "0.08 0.08 0.08"; $maxChars = 62 }
        "SUBTITLE" { $font = "/F1"; $fontSize = 11; $lineHeight = 16; $color = "0.25 0.25 0.25"; $maxChars = 90 }
        "H1"       { $font = "/F2"; $fontSize = 14.5; $lineHeight = 19; $color = "0.65 0.08 0.08"; $maxChars = 80 }
        "H2"       { $font = "/F2"; $fontSize = 12; $lineHeight = 16; $color = "0.12 0.22 0.62"; $maxChars = 88 }
        "P"        { $font = "/F1"; $fontSize = 10.5; $lineHeight = 14.5; $color = "0.07 0.07 0.07"; $maxChars = 102 }
        "BULLET"   { $font = "/F1"; $fontSize = 10.5; $lineHeight = 14.5; $color = "0.09 0.09 0.09"; $prefix = "- "; $maxChars = 98 }
        "CODE"     { $font = "/F1"; $fontSize = 10; $lineHeight = 13.8; $color = "0.18 0.18 0.18"; $prefix = "CODE: "; $maxChars = 94; $drawHighlight = $true }
        "FILES"    { $font = "/F2"; $fontSize = 10; $lineHeight = 14; $color = "0.12 0.43 0.23"; $maxChars = 100 }
        "RESULT"   { $font = "/F2"; $fontSize = 10.5; $lineHeight = 14.5; $color = "0.07 0.35 0.62"; $maxChars = 98 }
        "HIGHLIGHT"{ $font = "/F2"; $fontSize = 10.5; $lineHeight = 15.5; $color = "0.15 0.15 0.15"; $maxChars = 96; $drawHighlight = $true }
    }

    $wrapped = Wrap-Text -text ($prefix + $text) -maxChars $maxChars
    foreach ($line in $wrapped) {
        if (($currentY - $lineHeight) -lt $bottomY) {
            $pageNumber++
            Start-NewPage -Pages $pages -CurrentContent ([ref]$currentContent) -CurrentY ([ref]$currentY) -PageNo $pageNumber
        }

        if ($drawHighlight) {
            $rectY = [Math]::Round($currentY - ($lineHeight - 3), 2)
            $rectW = $pageWidth - $marginLeft - $marginRight
            [void]$currentContent.AppendLine("0.96 0.96 0.88 rg")
            [void]$currentContent.AppendLine("$marginLeft $rectY $rectW $lineHeight re")
            [void]$currentContent.AppendLine("f")
        }

        [void]$currentContent.AppendLine("BT")
        [void]$currentContent.AppendLine("$font $fontSize Tf")
        [void]$currentContent.AppendLine("$color rg")
        [void]$currentContent.AppendLine("1 0 0 1 $marginLeft $currentY Tm")
        [void]$currentContent.AppendLine("(" + (Escape-PdfText $line) + ") Tj")
        [void]$currentContent.AppendLine("ET")

        $currentY -= $lineHeight
    }
}

if ($currentContent.Length -gt 0) {
    $pages.Add($currentContent.ToString())
}

# Build PDF objects
$objects = New-Object 'System.Collections.Generic.SortedDictionary[int,string]'
$objects[1] = "<< /Type /Catalog /Pages 2 0 R >>"
$objects[3] = "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"
$objects[4] = "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>"

$nextId = 5
$pageObjectIds = New-Object System.Collections.Generic.List[int]

foreach ($contentStream in $pages) {
    $streamLength = [System.Text.Encoding]::ASCII.GetByteCount($contentStream)
    $contentObjectId = $nextId
    $nextId++
    $pageObjectId = $nextId
    $nextId++

    $objects[$contentObjectId] = "<< /Length $streamLength >>`nstream`n$contentStream`nendstream"
    $objects[$pageObjectId] = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 $pageWidth $pageHeight] /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents $contentObjectId 0 R >>"
    $pageObjectIds.Add($pageObjectId)
}

$kids = ($pageObjectIds | ForEach-Object { "$_ 0 R" }) -join " "
$objects[2] = "<< /Type /Pages /Kids [ $kids ] /Count $($pageObjectIds.Count) >>"

$maxId = ($objects.Keys | Measure-Object -Maximum).Maximum
$sb = New-Object System.Text.StringBuilder
$offsets = @{}

[void]$sb.AppendLine("%PDF-1.4")
[void]$sb.AppendLine("%1234")

for ($id = 1; $id -le $maxId; $id++) {
    $body = $objects[$id]
    if ($null -eq $body) { continue }
    $offsets[$id] = [System.Text.Encoding]::ASCII.GetByteCount($sb.ToString())
    [void]$sb.AppendLine("$id 0 obj")
    [void]$sb.AppendLine($body)
    [void]$sb.AppendLine("endobj")
}

$xrefPos = [System.Text.Encoding]::ASCII.GetByteCount($sb.ToString())
[void]$sb.AppendLine("xref")
[void]$sb.AppendLine("0 $($maxId + 1)")
[void]$sb.AppendLine("0000000000 65535 f ")

for ($id = 1; $id -le $maxId; $id++) {
    if ($offsets.ContainsKey($id)) {
        [void]$sb.AppendLine(([string]::Format("{0:0000000000} 00000 n ", [int]$offsets[$id])))
    } else {
        [void]$sb.AppendLine("0000000000 00000 f ")
    }
}

[void]$sb.AppendLine("trailer")
[void]$sb.AppendLine("<< /Size $($maxId + 1) /Root 1 0 R >>")
[void]$sb.AppendLine("startxref")
[void]$sb.AppendLine([string]$xrefPos)
[void]$sb.AppendLine("%%EOF")

[System.IO.File]::WriteAllBytes($outPath, [System.Text.Encoding]::ASCII.GetBytes($sb.ToString()))

$info = Get-Item -LiteralPath $outPath
Write-Output ("PDF_CREATED=" + $info.FullName)
Write-Output ("SIZE_BYTES=" + $info.Length)
Write-Output ("PAGES=" + $pages.Count)
