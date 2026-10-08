"""Exercise the actual Kotlin HTML parser and synchronization against offline fixtures."""
import runpy
from pathlib import Path

if __name__ == '__main__':
    runpy.run_path(str(Path(__file__).parent / 'tools/run-jvm-regression.py'))['verify'](['com.example.MetaRegionRegressionTest'])
