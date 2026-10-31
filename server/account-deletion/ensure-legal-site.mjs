import {getApp} from 'firebase-admin/app';
import {PROJECT} from './store.mjs';
const site='coach-legal-wild-rift-drafting';
const token=await getApp().options.credential.getAccessToken();
const headers={Authorization:`Bearer ${token.access_token}`,'Content-Type':'application/json'};
const base=`https://firebasehosting.googleapis.com/v1beta1/projects/${PROJECT}/sites`;
const existing=await fetch(`${base}/${site}`,{headers});
if(existing.status===404) {
  const created=await fetch(`${base}?siteId=${site}`,{method:'POST',headers,body:'{}'});
  if(!created.ok) throw new Error(`Legal site creation failed: HTTP ${created.status}`);
} else if(!existing.ok) throw new Error(`Legal site verification failed: HTTP ${existing.status}`);
console.log('Dedicated legal site verified.');
