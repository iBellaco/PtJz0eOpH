import {spawnSync} from 'node:child_process';
for(const file of ['storage.test.mjs','rules.test.mjs','economy-submission.test.mjs','../../server/account-deletion/test/emulator.integration.mjs','../../server/economy/test/emulator.integration.mjs','../../server/account-deletion/test/cleanup.integration.mjs','../../server/account-deletion/test/directory.integration.mjs','../../server/account-deletion/test/recovery.integration.mjs','../../server/account-deletion/test/recovery-media.integration.mjs']) {
  const result=spawnSync(process.execPath,[file],{stdio:'inherit',env:process.env});
  if(result.status!==0)process.exit(result.status||1);
}
const sponsors=spawnSync(process.execPath,['sponsor-review.test.mjs'],{stdio:'inherit',env:process.env});
if(sponsors.status!==0)process.exit(sponsors.status||1);
