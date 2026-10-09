import {initializeApp, applicationDefault} from 'firebase-admin/app';
import {getAuth} from 'firebase-admin/auth';
import {getFirestore, FieldValue} from 'firebase-admin/firestore';
import {ensureAdministrator} from './claims.mjs';
import {processQueue} from './queue.mjs';
if (process.env.FIRESTORE_EMULATOR_HOST || process.env.FIREBASE_AUTH_EMULATOR_HOST) throw new Error('Production worker requires its configured identity');
initializeApp({credential: applicationDefault(), projectId: 'wild-rift-drafting'});
try {
  const db = getFirestore(), auth = getAuth();
  await ensureAdministrator(auth, db);
  const stats = await processQueue(db, auth, {manualReview: process.env.COACH_REVIEW_PENDING === 'true'});
  await db.doc('system_config/economy_service').set({enabled: true, schema: 2, transport: 'PRIVATE_QUEUE', checkedAt: FieldValue.serverTimestamp()}, {merge: true});
  console.log('Private economy worker verified:', JSON.stringify(stats));
} catch (error) { console.error('Economy worker not verified:', error.code ?? 'internal'); process.exitCode = 1; }
