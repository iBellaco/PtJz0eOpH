// Used only by the operator after verifying an external request from its account owner.
import {randomUUID} from 'node:crypto';
import {FieldValue} from 'firebase-admin/firestore';
import {auth,db,ref} from './store.mjs';
const [email,confirmation]=process.argv.slice(2);
if(!email || confirmation!=='--owner-identity-and-consent-verified')
  throw new Error('Provide the account email and explicit verified-owner confirmation; never a password.');
const user=await auth.getUserByEmail(email);
if(user.email==='coach.guest.reader@gmail.com') throw new Error('Shared guest accounts are excluded.');
await db.runTransaction(async tx=>{
  const availability=await tx.get(db.doc('system_config/account_deletion_service'));
  if(availability.get('enabled')!==true || !availability.get('checkedAt') ||
     availability.get('checkedAt').toMillis()<Date.now()-36*3600000)
    throw new Error('The account deletion service must be available before accepting a request.');
  const existing=await tx.get(ref(user.uid));
  if(existing.exists && existing.get('status')!=='CANCELLED') throw new Error('An active deletion request already exists.');
  tx.set(ref(user.uid),{userId:user.uid,requestId:randomUUID(),status:'PENDING',graceDays:60,requestedAt:FieldValue.serverTimestamp()});
});
console.log('Verified external request recorded. The operator must communicate the recovery deadline to its owner.');
