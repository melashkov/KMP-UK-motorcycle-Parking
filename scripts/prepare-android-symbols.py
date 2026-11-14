#!/usr/bin/env python3
"""Prepare the native debug symbols available in an Android App Bundle."""

import argparse
from pathlib import Path, PurePosixPath
import sys
import zipfile


SYMBOLS_PREFIX = "BUNDLE-METADATA/com.android.tools.build.debugsymbols/"


def prepare(bundle_path):
    output = bundle_path.with_name("native-debug-symbols.zip")
    partial_output = output.with_suffix(".partial.zip")
    try:
        with zipfile.ZipFile(bundle_path) as bundle:
            libraries = {
                name.removeprefix("base/lib/")
                for name in bundle.namelist()
                if name.startswith("base/lib/") and name.endswith(".so")
            }
            symbols = {}
            for name in bundle.namelist():
                if not name.startswith(SYMBOLS_PREFIX) or name.endswith("/"):
                    continue
                relative = name.removeprefix(SYMBOLS_PREFIX)
                path = PurePosixPath(relative)
                if len(path.parts) != 2 or ".." in path.parts or path.is_absolute():
                    raise ValueError(f"Invalid native symbols path: {relative}")
                if path.suffix not in (".sym", ".dbg"):
                    raise ValueError(f"Unexpected native symbols file: {relative}")
                library = relative.removesuffix(path.suffix)
                if library not in libraries:
                    raise ValueError(f"Native symbols have no matching library: {relative}")
                data = bundle.read(name)
                if not data:
                    raise ValueError(f"Empty native symbols file: {relative}")
                symbols[relative] = data

            for library in sorted(libraries):
                if not any(library + suffix in symbols for suffix in (".sym", ".dbg")):
                    print(
                        f"WARNING: No full native debug symbols available for {library}. "
                        "Native crash reports may lack function names and source lines. "
                        "R8 mapping for Kotlin/Java crashes is unaffected.",
                        file=sys.stderr,
                    )

            if not symbols:
                output.unlink(missing_ok=True)
                print("WARNING: No native debug symbols to upload; continuing release.", file=sys.stderr)
                return None

            with zipfile.ZipFile(partial_output, "w", zipfile.ZIP_DEFLATED) as archive:
                for relative, data in sorted(symbols.items()):
                    archive.writestr(relative, data)
                    print(f"Included bundle native symbols: {relative}", file=sys.stderr)
        partial_output.replace(output)
    finally:
        partial_output.unlink(missing_ok=True)
    return output.resolve()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("bundle", type=Path)
    args = parser.parse_args()
    try:
        output = prepare(args.bundle)
        if output is not None:
            print(output)
    except (OSError, ValueError, KeyError, zipfile.BadZipFile) as error:
        print(f"ERROR: Cannot prepare native debug symbols: {error}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
