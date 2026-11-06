import {initializeApp} from 'firebase-admin/app';
import {getFirestore} from 'firebase-admin/firestore';
import {getAuth} from 'firebase-admin/auth';
import {onCall, HttpsError} from 'firebase-functions/v2/https';
import {onDocumentCreated} from 'firebase-functions/v2/firestore';
import {executeEconomy} from './service.mjs';
import {processQueueRow} from './queue.mjs';
import {EconomyError} from './policy.mjs';
initializeApp();
export const coachEconomy = onCall({region: 'us-central1', minInstances: 1, maxInstances: 10, timeoutSeconds: 60, memory: '256MiB'}, async request => {
  try { return await executeEconomy(getFirestore(), request.auth, request.data); }
  catch (error) {
    if (error instanceof EconomyError) throw new HttpsError(error.code, error.message);
    console.error('Economy operation failed:', error.code ?? 'internal');
    throw new HttpsError('internal', 'No se pudo completar la operación. Reintenta con el mismo identificador.');
  }
});

// Queue fallback is event-driven so a temporary callable outage does not delay
// essence grants, inbox notices, or sponsor decisions until the recovery cron.
// The scheduled worker remains enabled as a safety net for transient failures.
export const processEconomyRequest = onDocumentCreated({
  document: 'economy_requests/{requestId}',
  region: 'us-central1',
  minInstances: 1,
  timeoutSeconds: 60,
  memory: '256MiB',
  retry: true
}, async event => {
  const snapshot = event.data;
  if (!snapshot?.exists) return;
  await processQueueRow(getFirestore(), getAuth(), snapshot);
});
