"""Verify distributable project metadata, pinned binaries, and obvious secret leaks."""
from pathlib import Path
import hashlib
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
REQUIRED = [
    "README.md", "LICENSE", "NOTICE", "CONTRIBUTING.md", "SECURITY.md",
    "SUPPORT.md", "CHANGELOG.md", "settings.gradle.kts", "app/build.gradle.kts",
    "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.jar",
    ".github/workflows/android.yml", ".github/ISSUE_TEMPLATE/bug_report.yml",
    "docs/BUILDING.md", "docs/PRIVACY.md", "vendor/SHA256SUMS",
]
for name in REQUIRED:
    assert (ROOT / name).is_file(), f"Missing project file: {name}"

wrapper = ROOT / "gradle/wrapper/gradle-wrapper.jar"
assert hashlib.sha256(wrapper.read_bytes()).hexdigest() == "2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046", "Wrapper hash mismatch"
for line in (ROOT / "vendor/SHA256SUMS").read_text().splitlines():
    digest, name = line.split("  ", 1)
    assert hashlib.sha256((ROOT / "vendor" / name).read_bytes()).hexdigest() == digest, f"Vendor hash mismatch: {name}"

font = ROOT / "app/src/main/assets/fonts/NotoSerifSC.ttf"
assert font.stat().st_size == 3403608, "Unexpected font replacement"
assert (ROOT / "app/src/main/assets/licenses/NotoSerifSC.txt").is_file()

if (ROOT / ".git").exists():
    tracked = subprocess.check_output(["git", "ls-files", "-z"], cwd=ROOT).decode().split("\0")
    paths = [ROOT / p for p in tracked if p]
else:
    skipped = {".git", ".gradle", ".signing", "build", "build-final-native", "build-host", "build-stream-host", "build-instrumentation", "__pycache__"}
    paths = [p for p in ROOT.rglob("*") if p.is_file() and not skipped.intersection(p.relative_to(ROOT).parts)]

for path in paths:
    name = path.relative_to(ROOT).as_posix()
    assert not any(part in (".signing", ".env", "local.properties", "test-key.txt", "history.json", "memory.enc") for part in path.relative_to(ROOT).parts), f"Private local file included: {name}"
    assert path.suffix.lower() not in (".keystore", ".jks", ".apk", ".aab", ".idsig"), f"Private key or build output included: {name}"
    if path.suffix.lower() in (".java", ".py", ".md", ".kts", ".yml", ".json", ".properties", ".ps1"):
        text = path.read_text("utf-8")
        assert not re.search(r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----", text), f"Private key text in {name}"
        for key in re.findall(r"sk-[A-Za-z0-9_-]{24,}", text):
            assert key == "sk-abcdefghijklmnopqrstuvwx" or key.startswith(("sk-test-", "sk-invalid-")), f"Possible API credential in {name}; inspect locally without logging its value"

print(f"Project checks passed: {len(REQUIRED)} required files, wrapper/vendor hashes, font/license, and {len(paths)} distributable files")
