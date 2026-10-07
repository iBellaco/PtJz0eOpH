import {spawnSync} from 'node:child_process';
for(const file of ['rules.test.mjs','../../server/account-deletion/test/emulator.integration.mjs']) {
  const result=spawnSync(process.execPath,[file],{stdio:'inherit',env:process.env});
  if(result.status!==0)process.exit(result.status||1);
}
