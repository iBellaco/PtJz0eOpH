import {test} from 'node:test';
import assert from 'node:assert/strict';
import {reconcileDirectory} from '../reconcile-directory.mjs';

test('hides orphan profiles and restores recreated identities without deleting data', async () => {
  const marks=[];
  const report=await reconcileDirectory({
    async *profiles() { yield [{uid:'missing'}, {uid:'live',authAccountExists:false}];
      yield [{uid:'unchanged',authAccountExists:true}]; },
    exists:async uid=>uid!=='missing',
    mark:async(uid,exists)=>{marks.push([uid,exists]);return true;}
  });
  assert.deepEqual(marks,[['missing',false],['live',true]]);
  assert.deepEqual(report,{checked:3,hidden:1,restored:1,unchanged:1,errors:0});
});
test('permission errors, outages and ambiguous responses never hide accounts', async()=>{
  let writes=0;
  const result=await reconcileDirectory({
    async *profiles(){yield [{uid:'error'},{uid:'ambiguous'}];},
    exists:async uid=>{if(uid==='error')throw new Error('permission denied');return null;},
    mark:async()=>{writes++;return true;}
  });
  assert.equal(writes,0);assert.equal(result.errors,2);
});
test('a disappearing profile is never recreated by reconciliation',async()=>{
  const result=await reconcileDirectory({async *profiles(){yield [{uid:'deleted'}];},
    exists:async()=>false,mark:async()=>false});
  assert.equal(result.hidden,0);assert.equal(result.errors,0);
});
