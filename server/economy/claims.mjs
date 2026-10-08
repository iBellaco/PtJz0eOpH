import {ensure} from './policy.mjs';

/** Explicit operator input only: never infer privilege from an editable frame or arbitrary role. */
export async function ensureAdministrator(auth, db, bootstrapEmail = '') {
  if (bootstrapEmail) {
    const user = await auth.getUserByEmail(bootstrapEmail.trim().toLowerCase());
    const profile = (await db.doc(`users/${user.uid}`).get()).data();
    ensure(user.emailVerified && !user.disabled && profile && (profile.admin === true || ['admin', 'administrador'].includes((profile.role ?? '').toLowerCase())), 'failed-precondition', 'La identidad indicada no corresponde a un titular de administración verificado');
    await auth.setCustomUserClaims(user.uid, {...user.customClaims, admin: true});
    ensure((await auth.getUser(user.uid)).customClaims?.admin === true, 'failed-precondition', 'No se pudo verificar el permiso firmado');
  }
  let token, found = false;
  do {
    const page = await auth.listUsers(1000, token);
    found ||= page.users.some(user => !user.disabled && user.customClaims?.admin === true);
    token = page.pageToken;
  } while (token && !found);
  ensure(found, 'failed-precondition', 'Falta configurar al menos un permiso de administración firmado antes de activar las nuevas reglas');
  return true;
}
