import contextlib
import importlib.util
import io
from pathlib import Path
import tempfile
import unittest
import zipfile


spec = importlib.util.spec_from_file_location(
    "prepare_android_symbols", Path(__file__).resolve().parents[1] / "prepare-android-symbols.py"
)
symbols_module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(symbols_module)


class PrepareAndroidSymbolsTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.bundle = Path(self.directory.name) / "app.aab"

    def write_bundle(self, entries):
        with zipfile.ZipFile(self.bundle, "w") as bundle:
            for name, data in entries.items():
                bundle.writestr(name, data)

    def test_packages_available_symbols_and_warns_for_stripped_maplibre(self):
        self.write_bundle({
            "base/lib/arm64-v8a/libmaplibre-native-c.so": b"stripped",
            "base/lib/arm64-v8a/libother.so": b"runtime",
            symbols_module.SYMBOLS_PREFIX + "arm64-v8a/libother.so.sym": b"symbols",
        })
        warnings = io.StringIO()
        with contextlib.redirect_stderr(warnings):
            output = symbols_module.prepare(self.bundle)
        with zipfile.ZipFile(output) as archive:
            self.assertEqual(["arm64-v8a/libother.so.sym"], archive.namelist())
            self.assertEqual(b"symbols", archive.read("arm64-v8a/libother.so.sym"))
        self.assertIn("No full native debug symbols available for arm64-v8a/libmaplibre-native-c.so", warnings.getvalue())

    def test_no_symbols_removes_stale_archive_and_returns_no_upload(self):
        self.write_bundle({"base/lib/arm64-v8a/libmaplibre-native-c.so": b"stripped"})
        stale = self.bundle.with_name("native-debug-symbols.zip")
        stale.write_bytes(b"old release symbols")
        with contextlib.redirect_stderr(io.StringIO()) as warnings:
            self.assertIsNone(symbols_module.prepare(self.bundle))
        self.assertFalse(stale.exists())
        self.assertIn("No native debug symbols to upload", warnings.getvalue())

    def test_invalid_symbol_metadata_is_not_silently_ignored(self):
        for relative, data in [
            ("arm64-v8a/libmissing.so.sym", b"symbols"),
            ("../libother.so.sym", b"symbols"),
            ("arm64-v8a/libother.so.sym", b""),
        ]:
            with self.subTest(relative=relative, data=data):
                self.write_bundle({
                    "base/lib/arm64-v8a/libother.so": b"runtime",
                    symbols_module.SYMBOLS_PREFIX + relative: data,
                })
                with self.assertRaises(ValueError):
                    symbols_module.prepare(self.bundle)


if __name__ == "__main__":
    unittest.main()
