#!/usr/bin/env python3
"""Prepare Play native symbols, verifying MapLibre ELF build IDs against the AAB."""

import argparse
import hashlib
from pathlib import Path
import shutil
import struct
import sys
import tarfile
import urllib.request
import zipfile


def elf_metadata(data):
    """Read the GNU build ID and whether a full symbol table is present."""
    if data[:4] != b"\x7fELF" or data[4] not in (1, 2) or data[5] not in (1, 2):
        raise ValueError("Invalid ELF library")
    endian = "<" if data[5] == 1 else ">"
    if data[4] == 2:
        offset = struct.unpack_from(endian + "Q", data, 40)[0]
        stride, count, strings_index = struct.unpack_from(endian + "HHH", data, 58)
        section_format = endian + "IIQQQQIIQQ"
    else:
        offset = struct.unpack_from(endian + "I", data, 32)[0]
        stride, count, strings_index = struct.unpack_from(endian + "HHH", data, 46)
        section_format = endian + "IIIIIIIIII"
    sections = [struct.unpack_from(section_format, data, offset + i * stride)
                for i in range(count)]
    strings_section = sections[strings_index]
    strings = data[strings_section[4]:strings_section[4] + strings_section[5]]
    build_id = None
    has_symbols = False
    for section in sections:
        name = strings[section[0]:].split(b"\0", 1)[0]
        has_symbols |= name == b".symtab"
        if name != b".note.gnu.build-id":
            continue
        position, end = section[4], section[4] + section[5]
        while position < end:
            name_size, desc_size, kind = struct.unpack_from(endian + "III", data, position)
            position += 12
            owner = data[position:position + name_size].rstrip(b"\0")
            position += (name_size + 3) & ~3
            description = data[position:position + desc_size]
            position += (desc_size + 3) & ~3
            if owner == b"GNU" and kind == 3:
                build_id = description.hex()
    if not build_id:
        raise ValueError("ELF library has no GNU build ID")
    return build_id, has_symbols


def checksum(path):
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def prepare(bundle_path, archive_path=None):
    root = Path(__file__).resolve().parent.parent
    properties = dict(line.split("=", 1) for line in
                      (root / "gradle.properties").read_text().splitlines()
                      if "=" in line and not line.startswith("#"))
    version = properties["mapLibreAndroidVersion"]
    expected_sha = properties["mapLibreAndroidSymbolsSha256"]
    asset = f"debug-symbols-maplibre-android-opengl-release-android-v{version}.tar.gz"
    cache = root / "build" / "native-symbols" / f"maplibre-{version}"
    cache.mkdir(parents=True, exist_ok=True)
    if archive_path is None:
        archive_path = cache / asset
        if not archive_path.exists():
            print(f"Downloading MapLibre {version} OpenGL release symbols...", file=sys.stderr)
            url = f"https://github.com/maplibre/maplibre-native/releases/download/android-v{version}/{asset}"
            partial = archive_path.with_suffix(".partial")
            try:
                with urllib.request.urlopen(url, timeout=120) as source, partial.open("wb") as target:
                    shutil.copyfileobj(source, target)
                if checksum(partial) != expected_sha:
                    raise ValueError("Downloaded MapLibre symbols failed SHA-256 verification")
                partial.replace(archive_path)
            finally:
                partial.unlink(missing_ok=True)
    if checksum(archive_path) != expected_sha:
        raise ValueError(f"MapLibre symbols failed SHA-256 verification: {archive_path}")

    output = bundle_path.with_name("native-debug-symbols.zip")
    partial_output = output.with_suffix(".partial.zip")
    try:
        with zipfile.ZipFile(bundle_path) as bundle, tarfile.open(archive_path, "r:gz") as archive:
            libraries = {Path(name).parts[2]: name for name in bundle.namelist()
                         if name.startswith("base/lib/") and name.endswith("/libmaplibre.so")}
            if not libraries:
                raise ValueError("Bundle contains no MapLibre native libraries")
            members = {}
            for member in archive.getmembers():
                path = Path(member.name)
                if member.isfile() and path.name == "libmaplibre.so" and path.parent.name in libraries:
                    abi = path.parent.name
                    if abi in members:
                        raise ValueError(f"Ambiguous MapLibre symbols for {abi}")
                    members[abi] = member
            with zipfile.ZipFile(partial_output, "w", zipfile.ZIP_DEFLATED) as symbols:
                # Retain symbols Gradle can extract from any other native dependencies.
                prefix = "BUNDLE-METADATA/com.android.tools.build.debugsymbols/"
                for name in bundle.namelist():
                    if name.startswith(prefix) and not name.endswith("/"):
                        relative = name[len(prefix):]
                        if Path(relative).name not in ("libmaplibre.so.sym", "libmaplibre.so.dbg"):
                            symbols.writestr(relative, bundle.read(name))
                for abi, library in sorted(libraries.items()):
                    if abi not in members:
                        raise ValueError(f"Missing MapLibre symbols for {abi}")
                    with archive.extractfile(members[abi]) as source:
                        data = source.read()
                    runtime_id, _ = elf_metadata(bundle.read(library))
                    symbols_id, has_symbols = elf_metadata(data)
                    if runtime_id != symbols_id or not has_symbols:
                        raise ValueError(f"MapLibre symbols do not match the bundle for {abi}")
                    symbols.writestr(f"{abi}/libmaplibre.so", data)
                    print(f"Verified MapLibre symbols: {abi} ({runtime_id})", file=sys.stderr)
        partial_output.replace(output)
    finally:
        partial_output.unlink(missing_ok=True)
    return output.resolve()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("bundle", type=Path)
    parser.add_argument("--archive", type=Path, help="Use a downloaded symbols archive (still verified)")
    args = parser.parse_args()
    try:
        print(prepare(args.bundle, args.archive))
    except (OSError, ValueError, KeyError, IndexError, struct.error, tarfile.TarError,
            zipfile.BadZipFile) as error:
        print(f"ERROR: Cannot prepare native debug symbols: {error}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
