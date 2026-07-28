from pathlib import Path
import subprocess
import sys

from version import set_version
from notes import create_notes
from package import package_release
from zip_release import create_zip

ROOT = Path(__file__).resolve().parent.parent



def run_command(command):
    print()
    print("="*60)
    print("Running:", " ".join(command))
    print("="*60)

    result = subprocess.run(command, cwd=ROOT, shell=True)
    if result.returncode != 0:
        raise RuntimeError(f"Command failed with exit code {result.returncode}")

def main():
    if len(sys.argv) != 3:
        print("Usage:")
        print(" python scripts/release.py VERSION TYPE")
        print()
        print("Examples:")
        print(" python scripts/release.py 0.0.3 pre-release")
        print(" python scripts/release.py 1.0.0 release")
        sys.exit(1)

    version = sys.argv[1]
    release_type = sys.argv[2]

    print()
    print("Conductino Study Release Tool")
    print("------------------------------")
    print(f"Version: {version}")
    print(f"Type: {release_type}")

    #Update version
    print()
    print("[1/7] Updating version...")
    set_version(version)

    #Run unit tests
    print()
    print("[2/7] Running unit tests...")
    run_command([
        "gradle",
        "testDebugUnitTest",
        "-g",
        r"D:\.gradle"
        ])

    #Build Debug APK
    print()
    print("[3/7] Building Debug APK...")
    run_command([
        "gradle",
        "assembleDebug",
        "-g",
        r"D:\.gradle"
        ])

    #Build release APK
    print()
    print("[4/7] Building Release APK...")
    run_command([
        "gradle",
        "assembleRelease",
        "-g",
        r"D:\.gradle"
        ])

    #Package APKs
    print()
    print("[5/7 Packaging APKs....]")
    package_release(version)

    #Create release notes
    print()
    print("[6/7] Creating release notes")
    create_notes(version, release_type)

    #Create ZIP
    print()
    print("[7/7] Creating release ZIP")
    create_zip(version)

    print()
    print("=" * 60)
    print("RELEASE PACKAGE COMPLETE")
    print("=" * 60)
    print()
    print(f"Output: {ROOT / 'release' / version}")
    print(f"Notes:  {ROOT / 'release_notes' / f'v{version}.md'}")


if __name__ == '__main__':
    try:
        main()
    except Exception as e:
        print()
        print("RELEASE FAILED")
        print(e)
        sys.exit(1)