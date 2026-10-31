export const buckets = new Set(['wild-rift-drafting.firebasestorage.app','wild-rift-drafting.appspot.com']);
export function legacyExternalMedia(data) {
  return ['videoUrl','expandedImageUrl','imageUrl'].map(field => data[field]).filter(url => {
    try { return ['files.catbox.moe','catbox.moe','tmpfiles.org'].includes(new URL(url).hostname); }
    catch { return false; }
  });
}
/** A user-provided URL never authorizes deletion from another project or arbitrary path. */
export function mediaObject(url) {
  if (typeof url !== 'string') return null;
  try {
    const parsed = new URL(url);
    if (parsed.protocol !== 'https:' || parsed.hostname !== 'firebasestorage.googleapis.com') return null;
    const match = parsed.pathname.match(/^\/v0\/b\/([^/]+)\/o\/(.+)$/);
    if (!match || !buckets.has(match[1])) return null;
    const path = decodeURIComponent(match[2]);
    if (!path.startsWith('notice_videos/') || path.includes('..') || path.includes('\\')) return null;
    return {bucket:match[1],path};
  } catch { return null; }
}
export function mediaObjects(data) {
  return ['videoUrl','expandedImageUrl','imageUrl'].map(field => mediaObject(data[field])).filter(Boolean);
}
