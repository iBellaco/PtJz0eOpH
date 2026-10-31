import {initializeApp, applicationDefault} from 'firebase-admin/app';
import {getAuth} from 'firebase-admin/auth';
import {getFirestore} from 'firebase-admin/firestore';
import {ensureAdministrator} from './claims.mjs';
if (process.env.FIRESTORE_EMULATOR_HOST || process.env.FIREBASE_AUTH_EMULATOR_HOST) throw new Error('Production connection required');
initializeApp({credential: applicationDefault(), projectId: 'wild-rift-drafting'});
try {
  await ensureAdministrator(getAuth(), getFirestore());
  const marker = (await getFirestore().doc('system_config/economy_service').get()).data();
  const checkedAt = marker?.checkedAt?.toMillis?.() ?? 0;
  if (!marker?.enabled || marker.schema !== 2 || marker.transport !== 'PRIVATE_QUEUE' || checkedAt < Date.now() - 15 * 60000) throw new Error('Private worker is absent or stale');
  console.log('Private economy worker is active; trusted administrator verified.');
} catch (error) { console.error(error.code === 'failed-precondition' ? error.message : 'Economy activation not verified; restrictive permissions must not be published.'); process.exitCode = 1; }
