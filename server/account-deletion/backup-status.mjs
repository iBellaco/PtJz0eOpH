/** Read-only inventory: never create backups, enable billing or import live data. */
import {pathToFileURL} from 'node:url';
import {mkdir, writeFile, appendFile} from 'node:fs/promises';

export function classify(database, schedules, backups) {
  const available = value => value.status === 200;
  const completed = available(backups) ? (backups.body.backups || []).filter(entry =>
    entry.state === 'READY' && entry.database?.endsWith('/databases/(default)')) : [];
  return {
    checkedAt: new Date().toISOString(),
    databaseReadable: available(database),
    location: available(database) ? database.body.locationId || 'UNKNOWN' : 'UNKNOWN',
    productionRestoreVerified: false,
    billingChangesAuthorized: false,
    activationStatus: 'PENDING_COST_REVIEW',
    pointInTimeRecovery: available(database) ? database.body.pointInTimeRecoveryEnablement || 'UNKNOWN' : 'UNKNOWN',
    scheduleStatus: available(schedules) ? (schedules.body.backupSchedules?.length ? 'CONFIGURED' : 'NONE') : 'UNKNOWN',
    backupStatus: available(backups) ? (completed.length ? 'AVAILABLE' : 'NONE') : 'UNKNOWN',
    readyBackups: completed.length,
    lastReadyBackupAt: completed.map(entry => entry.snapshotTime || '').sort().at(-1) || null,
    latestReadyBackupBytes: completed.slice().sort((a, b) => (b.snapshotTime || '').localeCompare(a.snapshotTime || ''))[0]?.sizeBytes || null,
    http: {database: database.status, schedules: schedules.status, backups: backups.status},
    scope: 'Document data only; identity, uploaded media and restoration are not verified by this inventory.',
  };
}

export async function inventory(project, request) {
  const base = `https://firestore.googleapis.com/v1/projects/${project}`;
  const results = await Promise.all([
    `${base}/databases/(default)`, `${base}/databases/(default)/backupSchedules`, `${base}/locations/-/backups`,
  ].map(async url => {
    try {
      const field = url.endsWith('/backups') ? 'backups' : url.endsWith('/backupSchedules') ? 'backupSchedules' : null;
      let next = url;
      const entries = [], tokens = new Set();
      do {
        const response = await request(next);
        if (!response.ok) return {status: response.status, body: {}};
        const body = await response.json();
        if (!field) return {status: response.status, body};
        entries.push(...(body[field] || []));
        const token = body.nextPageToken;
        if (!token) return {status: 200, body: {[field]: entries}};
        if (tokens.has(token) || tokens.size >= 100) throw new Error('Incomplete recovery inventory');
        tokens.add(token);
        next = `${url}?pageToken=${encodeURIComponent(token)}`;
      } while (next);
    } catch { return {status: 0, body: {}}; }
  }));
  return classify(...results);
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const {PROJECT} = await import('./store.mjs');
  const {getApp} = await import('firebase-admin/app');
  const token = await getApp().options.credential.getAccessToken();
  const report = await inventory(PROJECT, url => fetch(url, {
    headers: {Authorization: `Bearer ${token.access_token}`}, signal: AbortSignal.timeout(15000),
  }));
  const output = new URL('../../app/build/reports/recovery/', import.meta.url);
  await mkdir(output, {recursive: true});
  await writeFile(new URL('backup-status.json', output), JSON.stringify(report, null, 2), {mode: 0o600});
  console.log('BACKUP_INVENTORY:', JSON.stringify(report));
  if (process.env.GITHUB_STEP_SUMMARY) await appendFile(process.env.GITHUB_STEP_SUMMARY,
    `\n### Recuperación: inventario de solo lectura\n\nProgramación: ${report.scheduleStatus}. Copias listas: ${report.backupStatus}. Última copia: ${report.lastReadyBackupAt || 'sin confirmar'}.\n\nUNKNOWN indica permisos o disponibilidad insuficientes; no confirma protección. No se cambió la configuración ni se restauraron datos reales.\n`);
}
