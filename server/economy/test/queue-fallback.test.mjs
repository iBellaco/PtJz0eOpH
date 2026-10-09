import assert from 'node:assert/strict';
import test from 'node:test';
import {readQueueRows} from '../queue.mjs';

test('missing composite indexes still select the oldest eligible requests', async () => {
  const row = (id, status, createdAt, leaseUntil) => ({
    id,
    get: field => field === 'createdAt' ? {toMillis: () => createdAt} : field === 'leaseUntil' ? leaseUntil : status
  });
  const rows = [row('new', 'PENDING', 300), row('unexpired', 'PROCESSING', 50, 1000),
    row('old', 'PENDING', 100), row('expired', 'PROCESSING', 150, 0), row('review', 'REVIEW', 80)];
  const requests = {
    where(field, operator, status) {
      assert.equal(field, 'status');
      assert.equal(operator, '==');
      return {
        orderBy() { throw Object.assign(new Error('missing composite index'), {code: 9}); },
        get: async () => ({docs: rows.filter(entry => entry.get('status') === status)})
      };
    }
  };
  const selected = await readQueueRows(requests, {startedAt: 500, limit: 2, manualReview: false});
  assert.deepEqual(selected.map(entry => entry.id), ['old', 'expired']);
  const review = await readQueueRows(requests, {startedAt: 500, limit: 2, manualReview: true});
  assert.deepEqual(review.map(entry => entry.id), ['review']);
});
