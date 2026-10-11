const {test}=require('node:test');const assert=require('node:assert/strict');const fs=require('node:fs');const path=require('node:path');const vm=require('node:vm');const ts=require('typescript');
test('merchant overview sums only active balances in the displayed currency',async()=>{
 const wallets=[{id:1,currency:'GMD',status:'ACTIVE'},{id:2,currency:'USD',status:'ACTIVE'},{id:3,currency:'GMD',status:'SUSPENDED'},{id:4,currency:'GMD',status:'ACTIVE'}];
 const values={1:100,2:999,3:500,4:25};const exports={};
 const authFetch=async url=>url==='/api/v1/wallets'?wallets:url==='/api/v1/payment-links'?[{status:'ACTIVE'},{status:'PAID'}]:{balance:values[Number(url.split('/')[4])]};
 const code=ts.transpileModule(fs.readFileSync(path.join(__dirname,'../src/api/dashboard.ts'),'utf8'),{compilerOptions:{module:ts.ModuleKind.CommonJS,target:ts.ScriptTarget.ES2022}}).outputText;
 vm.runInNewContext(code,{exports,require:()=>({authFetch,readAuthJson:async response=>response})});
 const result=await exports.fetchMerchantDashboard();assert.equal(result.currency,'GMD');assert.equal(result.totalBalance,125);assert.equal(result.activeLinks,1);
});
