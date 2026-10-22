import test from 'node:test';
import assert from 'node:assert/strict';
import { unverifiedBefore } from '../cleanup-unverified.mjs';

const now = Date.parse('2026-10-22T00:00:00Z');
const cutoff = now - 7 * 24 * 60 * 60 * 1000;

test('only old unverified non-guest accounts are candidates', () => {
  assert.equal(unverifiedBefore({ uid: 'old', email: 'old@example.com', emailVerified: false,
    metadata: { creationTime: '2026-10-01T00:00:00Z' } }, cutoff), true);
  assert.equal(unverifiedBefore({ uid: 'new', email: 'new@example.com', emailVerified: false,
    metadata: { creationTime: '2026-10-21T00:00:00Z' } }, cutoff), false);
  assert.equal(unverifiedBefore({ uid: 'verified', email: 'v@example.com', emailVerified: true,
    metadata: { creationTime: '2026-10-01T00:00:00Z' } }, cutoff), false);
  assert.equal(unverifiedBefore({ uid: 'guest', email: 'coach.guest.reader@gmail.com', emailVerified: false,
    metadata: { creationTime: '2026-10-01T00:00:00Z' } }, cutoff), false);
});
