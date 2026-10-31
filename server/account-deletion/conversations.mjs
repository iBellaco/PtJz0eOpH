const staff=new Set(['SUPPORT','ADMIN','MODERATOR','MODERADOR']);
const closed=new Set(['SOLVED','SOLUCIONADO','RESUELTO','CLOSED','CERRADO','COMPLETED','COMPLETADO']);
export function conversationCleanup(data,uid,email) {
  const rows=data.conversation;
  const matches=row=>row?.senderUid===uid || Boolean(email && row?.senderEmail===email);
  if(!Array.isArray(rows) || !rows.some(matches))return null;
  const remaining=rows.filter(row=>!matches(row));
  const patch={conversation:remaining};
  const oldAnswer=rows.findLast(row=>staff.has(row?.senderRole) && !row.isGreeting);
  if(matches(oldAnswer)) {
    const answer=remaining.findLast(row=>staff.has(row?.senderRole) && !row.isGreeting);
    Object.assign(patch,{adminReply:answer?.text||'',lastAdminReply:answer?.text||'',
      repliedBy:answer?.senderName||'',repliedEmail:answer?.senderEmail||'',repliedAt:answer?.timestampMillis||null,
      lastReplyRole:remaining.at(-1)?.senderRole||'SYSTEM',lastReplySenderRole:remaining.at(-1)?.senderRole||'SYSTEM',
      userCanReply:Boolean(answer)&&!closed.has(data.status),
      hasNewAdminReply:Boolean(answer)&&data.userRead!==true,hasNewReply:Boolean(answer)&&data.userRead!==true});
  }
  return patch;
}
