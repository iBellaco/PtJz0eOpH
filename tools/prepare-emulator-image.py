"""Retry only transient SDK image installation failures, before starting any test."""
import subprocess
import time

PACKAGE = 'system-images;android-34;google_apis;x86_64'
TRANSIENT = ('error reading zip content from a seekablebytechannel',
             'connection reset', 'connection timed out', 'read timed out',
             'unexpected end of', 'unexpected eof', 'http 502', 'http 503', 'http 504')


def install(run=subprocess.run, pause=time.sleep):
    for attempt in range(1, 4):
        print(f'EMULATOR_IMAGE: intento {attempt}/3: {PACKAGE}', flush=True)
        try:
            result = run(['sdkmanager', '--install', PACKAGE, '--channel=0'],
                         input='y\n' * 100, capture_output=True, text=True, timeout=180)
            output = (result.stdout or '') + (result.stderr or '')
            code = result.returncode
            retry = any(marker in output.lower() for marker in TRANSIENT)
        except subprocess.TimeoutExpired as error:
            def decoded(value):
                return value.decode(errors='replace') if isinstance(value, bytes) else (value or '')
            output = decoded(error.stdout) + decoded(error.stderr) + '\nSDK download timeout (180s)'
            code, retry = 124, True
        print(output, flush=True)
        if code == 0:
            print('EMULATOR_IMAGE: instalada; las pruebas aún deben ejecutarse', flush=True)
            return 0
        if not retry or attempt == 3:
            print(f'EMULATOR_IMAGE_FAILED: instalación fallida (exit={code}); OCR no ejecutado', flush=True)
            return code
        pause(5 * attempt)
    return 1


if __name__ == '__main__':
    raise SystemExit(install())
