import {initializeApp, applicationDefault} from 'firebase-admin/app';
import {getAuth} from 'firebase-admin/auth';
import {getFirestore} from 'firebase-admin/firestore';
import {ensureAdministrator} from './claims.mjs';
if (process.env.FIRESTORE_EMULATOR_HOST || process.env.FIREBASE_AUTH_EMULATOR_HOST) throw new Error('Production connection required');
initializeApp({credential: applicationDefault(), projectId: 'wild-rift-drafting'});
try {
  await ensureAdministrator(getAuth(), getFirestore(), process.env.COACH_BOOTSTRAP_ADMIN_EMAIL ?? '');
  console.log('Verified at least one trusted administrator; existing claims preserved.');
} catch (error) { console.error(error.code === 'failed-precondition' ? error.message : `Cannot verify administrator (${String(error.code ?? 'unknown').replace(/[^a-zA-Z0-9_/-]/g, '')}) through the configured deployment connection.`); process.exitCode = 1; }
