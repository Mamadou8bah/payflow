const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');const path=require('node:path');const vm=require('node:vm');const ts=require('typescript');
function load(file,extra={}){const exports={};const code=ts.transpileModule(fs.readFileSync(path.join(__dirname,file),'utf8'),{compilerOptions:{module:ts.ModuleKind.CommonJS,target:ts.ScriptTarget.ES2022}}).outputText;vm.runInNewContext(code,{exports,URL,Response,AbortController,setTimeout,clearTimeout,require:()=>({API_BASE:'https://test.invalid'}),...extra});return exports;}
const {parseMerchantQr}=load('../src/utils/merchantQr.ts');
test('merchant QR accepts references and payment links without trusting embedded destinations or amounts',()=>{
 for(const value of ['plink-123456abcdef','/pay/plink-123456abcdef','https://example.test/pay/plink-123456abcdef?amount=1&walletId=999','suturaapp://pay/plink-123456abcdef'])assert.equal(parseMerchantQr(value),'plink-123456abcdef');
 assert.equal(parseMerchantQr('DEMO-COLLECTION'),'DEMO-COLLECTION');
});
test('merchant QR rejects unrelated, executable, agent and malformed payloads',()=>{
 for(const value of ['','javascript:alert(1)','https://test.invalid/other/plink-123456abcdef','/pay/../../wallets','{"operation":"WITHDRAWAL","reference":"DEMO-1"}','plink-invalid','a'.repeat(2049)])assert.throws(()=>parseMerchantQr(value));
});
test('demo merchant payment debits once, records activity and rejects mismatched or insufficient wallets',async()=>{
 const demo=load('../src/api/demo.ts');demo.setDemoMode(true);
 const pay=walletId=>demo.demoResponse('/api/v1/payment-links/ref/DEMO-COLLECTION/pay',{method:'POST',body:JSON.stringify({walletId})});
 const create=async(name,currency)=>(await demo.demoResponse('/api/v1/wallets',{method:'POST',body:JSON.stringify({name,currency})}).json());
 const usd=await create('USD wallet','USD');assert.equal(pay(usd.id).status,400);
 const empty=await create('Empty','GMD');assert.equal(pay(empty.id).status,400);
 const before=await demo.demoResponse('/api/v1/wallets/1/balance').json();
 assert.equal((await pay(1).json()).status,'PAID');assert.equal((await pay(1).json()).status,'PAID');
 const after=await demo.demoResponse('/api/v1/wallets/1/balance').json();assert.equal(before.balance-after.balance,250);
 const history=await demo.demoResponse('/api/v1/transactions').json();assert.equal(history.length,1);assert.equal(history[0].status,'COMPLETED');
 assert.equal(pay(empty.id).status,400);
 assert.equal((await demo.demoResponse('/api/v1/payment-links/ref/DEMO-COLLECTION').json()).status,'PAID');
});
