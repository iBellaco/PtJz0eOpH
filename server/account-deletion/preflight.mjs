import { permissions, heartbeat } from './store.mjs';
try {
  await permissions();
  if (process.argv.includes('--activate')) await heartbeat();
  console.log('ACCOUNT_DELETION_PREFLIGHT: required permissions verified; no accounts modified.');
} catch (error) {
  if (/^(IAM verification HTTP \d+|Missing lifecycle permissions: [a-z., ]+)$/.test(error.message || '')) console.error(error.message);
  console.error('ACCOUNT_DELETION_PREFLIGHT: unable to verify all required permissions; account deletion is not activated.');
  process.exitCode = 1;
}
