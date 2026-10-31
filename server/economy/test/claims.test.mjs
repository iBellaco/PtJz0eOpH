import {test} from 'node:test';
import assert from 'node:assert/strict';
import {ensureAdministrator} from '../claims.mjs';

test('migration requires an explicitly selected verified owner and preserves unrelated claims', async () => {
  let claims = {moderator: true};
  const user = () => ({uid: 'owner', emailVerified: true, disabled: false, customClaims: claims});
  const auth = {getUserByEmail: async () => user(), getUser: async () => user(), setCustomUserClaims: async (_, value) => {claims = value;}, listUsers: async () => ({users: [user()]})};
  const db = {doc: () => ({get: async () => ({data: () => ({role: 'admin'})})})};
  await ensureAdministrator(auth, db, 'selected@example.invalid');
  assert.deepEqual(claims, {moderator: true, admin: true});
});
test('a profile flag cannot silently bootstrap an arbitrary account', async () => {
  let writes = 0;
  const auth = {listUsers: async () => ({users: [{customClaims: {}}]}), setCustomUserClaims: async () => writes++};
  await assert.rejects(ensureAdministrator(auth, {}), /Falta configurar/); assert.equal(writes, 0);
});
test('unverified email, disabled owner and a cosmetic admin border cannot become authority', async () => {
  for (const [user, profile] of [[{emailVerified: false}, {role: 'admin'}], [{emailVerified: true, disabled: true}, {role: 'admin'}], [{emailVerified: true}, {role: 'free', rankBorder: 'ADMIN'}]]) {
    let writes = 0;
    const auth = {getUserByEmail: async () => ({uid: 'owner', ...user}), setCustomUserClaims: async () => writes++};
    const db = {doc: () => ({get: async () => ({data: () => profile})})};
    await assert.rejects(ensureAdministrator(auth, db, 'selected@example.invalid'), /verificado/); assert.equal(writes, 0);
  }
});
