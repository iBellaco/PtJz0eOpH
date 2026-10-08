import {initializeApp, applicationDefault} from 'firebase-admin/app';
import {getAuth} from 'firebase-admin/auth';
import {getFirestore} from 'firebase-admin/firestore';
import {ensureAdministrator} from './claims.mjs';
if (process.env.FIRESTORE_EMULATOR_HOST || process.env.FIREBASE_AUTH_EMULATOR_HOST) throw new Error('Production connection required');
initializeApp({credential: applicationDefault(), projectId: 'wild-rift-drafting'});
try {
  await ensureAdministrator(getAuth(), getFirestore());
  const response = await fetch('https://us-central1-wild-rift-drafting.cloudfunctions.net/coachEconomy', {method: 'POST', headers: {'content-type': 'application/json'}, body: JSON.stringify({data: {action: 'PURCHASE', id: 'deployment_probe', plan: 'MONTHLY', currency: 'BLUE'}}), signal: AbortSignal.timeout(30000)});
  const result = await response.json();
  if (response.status !== 401 || result.error?.status !== 'UNAUTHENTICATED') throw new Error('Callable is absent or does not reject unauthenticated transactions');
  await getFirestore().doc('system_config/economy_service').set({enabled: true, schema: 1, checkedAt: Date.now()}, {merge: true});
  console.log('Authoritative economy is deployed and rejects unauthenticated transactions.');
} catch (error) { console.error(error.code === 'failed-precondition' ? error.message : 'Economy activation not verified; restrictive permissions must not be published.'); process.exitCode = 1; }
