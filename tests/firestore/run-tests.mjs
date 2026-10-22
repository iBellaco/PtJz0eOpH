import {spawnSync} from 'node:child_process';
for(const file of ['storage.test.mjs','rules.test.mjs','../../server/account-deletion/test/emulator.integration.mjs','../../server/economy/test/emulator.integration.mjs','../../server/account-deletion/test/cleanup.integration.mjs','../../server/account-deletion/test/directory.integration.mjs','../../server/account-deletion/test/recovery.integration.mjs']) {
  const result=spawnSync(process.execPath,[file],{stdio:'inherit',env:process.env});
  if(result.status!==0)process.exit(result.status||1);
}
