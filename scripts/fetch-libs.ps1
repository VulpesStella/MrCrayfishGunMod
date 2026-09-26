param(
    [string]$OutputDirectory = (Join-Path $PSScriptRoot '../libs'),
    [ValidateSet('core', 'compat')][string]$Set = 'core',
    [switch]$VerifyOnly
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

# Explicit, version-pinned jars. Never glob the target directory: the repository
# also keeps research samples that must not be loaded by accident.
$coreDependencies = @(
    @{ Name = 'jei-15.20.0.134.jar'; Url = 'https://cdn.modrinth.com/data/u6dRKJwZ/versions/jJOr2rUn/jei-1.20.1-fabric-15.20.0.134.jar'; Hash = '2979be598b39c0c9d9a38ae47809a37c0159797ec7d77bae6b952b83f20bc522' },
    @{ Name = 'cmdcam-2.1.3.jar'; Url = 'https://cdn.modrinth.com/data/GOQ3z2ek/versions/zPOXCfOw/CMDCam-Fabric.jar'; Hash = '20dbbb6ac8080cb64c8c0b031ce281716e0e61ccaa1a2e20c2668c336cb18a9c' },
    @{ Name = 'framework-0.8.0.jar'; Url = 'https://github.com/MrCrayfish/Framework/releases/download/v1.20.1-0.8.0/framework-fabric-1.20.1-0.8.0-signed.jar'; Hash = 'fb35570ec10ad863a391c3da969d2555f72bb75aa02fc0d12c504f7362a00f7a' },
    @{ Name = 'forgeconfigapiport-8.0.3.jar'; Url = 'https://cdn.modrinth.com/data/ohNO6lps/versions/HvR3IdRE/ForgeConfigAPIPort-v8.0.3-1.20.1-Fabric.jar'; Hash = '337a8f6c5ba48ef3b3da828f4f6a75d0552ba33f48ce97d3195befdb570e427a' }
)

# Red-line compatibility targets (docs/compat-redline-1.20.1/DEPENDENCIES.md).
# Backpacked requires Fabric Loader >=0.18.4; gradle.properties declares 0.18.4.
$compatDependencies = @(
    @{ Name = 'backpacked-fabric-1.20.1-3.0.9-signed.jar'; Url = 'https://github.com/MrCrayfish/Backpacked/releases/download/v1.20.1-3.0.9/backpacked-fabric-1.20.1-3.0.9-signed.jar'; Hash = 'a1a9f7856682653bdac5cbe1fab173c91063a8b31788958b7c316b9640f651bc' },
    @{ Name = 'travelersbackpack-fabric-1.20.1-9.1.52.jar'; Url = 'https://cdn.modrinth.com/data/rlloIFEV/versions/q2caASeP/travelersbackpack-fabric-1.20.1-9.1.52.jar'; Hash = '8758e8f922ca90fa3de7fffbaf13dd923f67be5cfd604f79f79f2b128974a166' }
    @{ Name = 'cardinal-components-api-5.2.3.jar'; Url = 'https://cdn.modrinth.com/data/K01OU20C/versions/Ielhod3p/cardinal-components-api-5.2.3.jar'; Hash = 'ff1e33adde43c3e01f7e9a673cbb7f3a75453e769f80a38b057653475e296aa2' },
    @{ Name = 'cloth-config-11.1.136-fabric.jar'; Url = 'https://cdn.modrinth.com/data/9s6osm5g/versions/2xQdCMyG/cloth-config-11.1.136-fabric.jar'; Hash = '844f49c339c8f3144e167ab21b7d0059f0d571054e26285a0749348da25c49fa' },
    @{ Name = 'sophisticatedbackpacks-1.20.1-3.23.4.5.110.jar'; Url = 'https://cdn.modrinth.com/data/ouNrBQtq/versions/Jk6o7s4h/sophisticatedbackpacks-1.20.1-3.23.4.5.110.jar'; Hash = '1e4e268d079ebef8a21f41f6a7a35528435733a124a134e3ef240389f3aa87c1' },
    @{ Name = 'sophisticatedcore-1.20.1-1.2.7.15.166.jar'; Url = 'https://cdn.modrinth.com/data/9jxwkYQL/versions/BP3CQI2v/sophisticatedcore-1.20.1-1.2.7.15.166.jar'; Hash = 'dafca4311f94ed5eff18f34077e7ab91a169863a4bde2cc82ae525d869ee9396' }
)

# Nested libraries that Framework ships as jar-in-jar, extracted from the signed
# Framework jar itself so their hashes are never hard-coded.
$nestedFromFramework = @('core-3.6.6', 'toml-3.6.6', 'javassist-3.29.2-GA', 'reflections-0.10.2')

function Test-Hash([string]$Path, [string]$Expected) {
    return (Test-Path -LiteralPath $Path -PathType Leaf) -and ((Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash -eq $Expected)
}

try {
    $dependencies = if ($Set -eq 'compat') { $coreDependencies + $compatDependencies } else { $coreDependencies }
    $destination = [IO.Path]::GetFullPath($OutputDirectory)
    if (!$VerifyOnly) { New-Item -ItemType Directory -Path $destination -Force | Out-Null }

    foreach ($dependency in $dependencies) {
        $target = Join-Path $destination $dependency.Name
        if (!(Test-Hash $target $dependency.Hash)) {
            if ($VerifyOnly) { throw "Missing or corrupt dependency: $target" }
            $download = "$target.download"
            & curl.exe --fail --location --connect-timeout 15 --max-time 300 --retry 3 --output $download $dependency.Url
            if ($LASTEXITCODE -ne 0) { throw "Download failed: $($dependency.Name)" }
            if (!(Test-Hash $download $dependency.Hash)) {
                Remove-Item -LiteralPath $download -Force
                throw "SHA-256 mismatch: $($dependency.Name)"
            }
            Move-Item -LiteralPath $download -Destination $target -Force
        }
        Write-Output "Verified $($dependency.Name)"
    }

    # Framework nests these as JIJ; Loom dev does not load nested jars of dependency
    # mods, so they are provided on the runtime classpath explicitly.
    $nested = Join-Path $destination 'nested'
    if (!$VerifyOnly) { New-Item -ItemType Directory -Path $nested -Force | Out-Null }
    $archive = [IO.Compression.ZipFile]::OpenRead((Join-Path $destination 'framework-0.8.0.jar'))
    try {
        foreach ($name in $nestedFromFramework) {
            $entry = $archive.GetEntry("META-INF/jars/$name.jar")
            if (!$entry) { throw "Missing nested library: $name" }
            $stream = $entry.Open()
            $sha = [Security.Cryptography.SHA256]::Create()
            try { $expected = ([BitConverter]::ToString($sha.ComputeHash($stream))).Replace('-', '') }
            finally { $stream.Dispose(); $sha.Dispose() }
            $target = Join-Path $nested "$name.jar"
            if (!(Test-Hash $target $expected)) {
                if ($VerifyOnly) { throw "Missing or corrupt nested library: $target" }
                [IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $target, $true)
                if (!(Test-Hash $target $expected)) { throw "Nested library verification failed: $target" }
            }
            Write-Output "Verified nested/$name.jar"
        }
    } finally { $archive.Dispose() }

    # Sophisticated Core nests its porting_lib modules the same way. Only the
    # transfer module is needed, and only on the compile classpath: CGM's adapter
    # references InventoryHandler, whose supertype lives there. Extracted from the
    # pinned jar so its hash is derived, not hard-coded.
    if ($Set -eq 'compat') {
        $nestedTransfer = Join-Path $destination 'nested-transfer'
        if (!$VerifyOnly) { New-Item -ItemType Directory -Path $nestedTransfer -Force | Out-Null }
        $coreJar = Join-Path $destination 'sophisticatedcore-1.20.1-1.2.7.15.166.jar'
        $coreArchive = [IO.Compression.ZipFile]::OpenRead($coreJar)
        try {
            $name = 'transfer-2.3.2+1.20.1'
            $entry = $coreArchive.GetEntry("META-INF/jars/$name.jar")
            if (!$entry) { throw "Missing nested library in sophisticatedcore: $name" }
            $stream = $entry.Open()
            $sha = [Security.Cryptography.SHA256]::Create()
            try { $expected = ([BitConverter]::ToString($sha.ComputeHash($stream))).Replace('-', '') }
            finally { $stream.Dispose(); $sha.Dispose() }
            $target = Join-Path $nestedTransfer "$name.jar"
            if (!(Test-Hash $target $expected)) {
                if ($VerifyOnly) { throw "Missing or corrupt nested library: $target" }
                [IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $target, $true)
                if (!(Test-Hash $target $expected)) { throw "Nested library verification failed: $target" }
            }
            Write-Output "Verified nested-transfer/$name.jar"
        } finally { $coreArchive.Dispose() }
    }
    Write-Output "Set '$Set' verified: $($dependencies.Count) mod jar(s) + $($nestedFromFramework.Count) framework nested library(ies)"
} catch {
    Write-Error $_ -ErrorAction Continue
    exit 1
}
exit 0
