# Extracts the jar-in-jar modules that Fabric Loader would expand in production but Loom
# does not load in the development runtime. Needed on the compile classpath.
#
# Extraction is recursive: Porting Lib's modules each nest porting_lib_core, which nests
# further modules, so a single level is not enough.
#
# Output: libs/nested-dev/*.jar.
# MixinExtras is deliberately skipped: the toolchain already supplies it.
param(
    [string]$OutputDirectory = (Join-Path $PSScriptRoot '../libs/nested-dev'),
    [string[]]$Seeds = @(
        'sophisticatedcore-1.20.1-1.2.7.15.166.jar',
        'sophisticatedbackpacks-1.20.1-3.23.4.5.110.jar',
        'cardinal-components-api-5.2.3.jar',
        'travelersbackpack-fabric-1.20.1-9.1.52.jar',
        'cloth-config-11.1.136-fabric.jar'
    ),
    [switch]$VerifyOnly
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$libs = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../libs'))
$destination = [IO.Path]::GetFullPath($OutputDirectory)
$skip = @('mixinextras')

function Get-Sha256([string]$Path) {
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash
}

if (!$VerifyOnly) { New-Item -ItemType Directory -Path $destination -Force | Out-Null }

$written = New-Object System.Collections.Generic.List[string]
$queue = New-Object System.Collections.Generic.Queue[string]
foreach ($seed in $Seeds) {
    $path = Join-Path $libs $seed
    if (!(Test-Path -LiteralPath $path)) { throw "Seed jar missing: $path" }
    $queue.Enqueue($path)
}

$seenSources = @{}
$depthGuard = 0
while ($queue.Count -gt 0) {
    if (++$depthGuard -gt 2000) { throw 'Extraction did not terminate' }
    $source = $queue.Dequeue()
    if ($seenSources.ContainsKey($source)) { continue }
    $seenSources[$source] = $true

    $archive = [IO.Compression.ZipFile]::OpenRead($source)
    try {
        foreach ($entry in $archive.Entries) {
            if ($entry.FullName -notlike 'META-INF/jars/*.jar') { continue }
            $name = Split-Path $entry.FullName -Leaf
            if ($skip | Where-Object { $name -like "$_*" }) { continue }
            $target = Join-Path $destination $name
            if (!(Test-Path -LiteralPath $target)) {
                if ($VerifyOnly) { throw "Missing extracted module: $target" }
                [IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $target, $true)
            }
            if (!$written.Contains($name)) { $written.Add($name) }
            # Recurse into the module we just wrote so second-level nesting is covered.
            $queue.Enqueue($target)
        }
    } finally { $archive.Dispose() }
}

# Modules are identified by mod id, not filename: the same module can be nested under
# several parents with different file names (fluids-2.3.2 vs porting_lib_fluids-2.3.2),
# and loading two copies would be a duplicate-mod error.
function Get-ModId([string]$JarPath) {
    $archive = [IO.Compression.ZipFile]::OpenRead($JarPath)
    try {
        $entry = $archive.GetEntry('fabric.mod.json')
        if (!$entry) { return $null }
        $reader = New-Object IO.StreamReader($entry.Open())
        try { return ($reader.ReadToEnd() | ConvertFrom-Json).id } finally { $reader.Dispose() }
    } finally { $archive.Dispose() }
}

$byModId = @{}
$duplicates = New-Object System.Collections.Generic.List[string]
foreach ($name in ($written | Sort-Object)) {
    $path = Join-Path $destination $name
    $id = Get-ModId $path
    if (!$id) { continue }
    if ($byModId.ContainsKey($id)) {
        Remove-Item -LiteralPath $path -Force
        $duplicates.Add("$name (duplicate of $($byModId[$id]) for mod id $id)")
        continue
    }
    $byModId[$id] = $name
}

foreach ($id in ($byModId.Keys | Sort-Object)) {
    $path = Join-Path $destination $byModId[$id]
    Write-Output ("{0}  {1}  id={2}" -f (Get-Sha256 $path), $byModId[$id], $id)
}
if ($duplicates.Count -gt 0) {
    Write-Output "Removed $($duplicates.Count) duplicate module(s):"
    $duplicates | ForEach-Object { Write-Output "  $_" }
}
Write-Output "Kept $($byModId.Count) module(s) in $destination"
exit 0
