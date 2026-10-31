import {initializeApp} from 'firebase-admin/app';
import {getFirestore} from 'firebase-admin/firestore';
import {onCall, HttpsError} from 'firebase-functions/v2/https';
import {executeEconomy} from './service.mjs';
import {EconomyError} from './policy.mjs';
initializeApp();
export const coachEconomy = onCall({region: 'us-central1', maxInstances: 10, timeoutSeconds: 60, memory: '256MiB'}, async request => {
  try { return await executeEconomy(getFirestore(), request.auth, request.data); }
  catch (error) {
    if (error instanceof EconomyError) throw new HttpsError(error.code, error.message);
    console.error('Economy operation failed:', error.code ?? 'internal');
    throw new HttpsError('internal', 'No se pudo completar la operación. Reintenta con el mismo identificador.');
  }
});
