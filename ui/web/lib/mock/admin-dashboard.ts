export type WalletStatus = "ACTIVE" | "SUSPENDED" | "CLOSED";

export type AdminWallet = {
  id: number;
  owner: string;
  email: string;
  status: WalletStatus;
  balance: string;
  currency: string;
  kycLevel: string;
};

export type AdminTransaction = {
  id: number;
  reference: string;
  amount: string;
  status: "PENDING" | "COMPLETED" | "FAILED" | "REVERSED";
  sourceWalletId: number;
  destinationWalletId: number;
  createdAt: string;
};

export type AdminWebhook = {
  id: number;
  eventType: string;
  status: string;
  provider: string;
  receivedAt: string;
  payloadRef: string;
};

export type AdminRiskFlag = {
  id: number;
  walletId: number;
  level: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  rule: string;
  description: string;
  resolved: boolean;
  transactionId?: number;
  createdAt: string;
};

export type ReconciliationReport = {
  id: number;
  reconciliationType: "WALLET_LEDGER" | "WEBHOOK_DEPOSIT";
  status: "COMPLETED" | "RUNNING" | "FAILED";
  startedAt: string;
  completedAt?: string;
  totalRecordsChecked: number;
  mismatchesFound: number;
  mismatchesResolved: number;
  summary: string;
  triggeredBy: string;
  automated: boolean;
};

export type AuditLogEntry = {
  id: string;
  actionType: string;
  entityType: string;
  entityId: number;
  actor: string;
  changeDescription: string;
  success: boolean;
  timestamp: string;
};

export type ReconciliationMismatch = {
  id: number;
  reportId: number;
  mismatchType: string;
  entityId: number;
  entityType: string;
  expectedValue: string;
  actualValue: string;
  variance: string;
  description: string;
  resolved: boolean;
  resolutionAction?: string;
  createdAt: string;
};

export type AdminDashboardData = {
  totalWallets: number;
  activeWallets: number;
  frozenWallets: number;
  totalTransactions: number;
  failedTransactions: number;
  pendingTransactions: number;
  totalRiskFlags: number;
  criticalRiskFlags: number;
  unresolvedReconciliationMismatches: number;
  failedOperations: number;
  operationCounts: {
    freezes: number;
    reversals: number;
    reprocesses: number;
    reconciliations: number;
  };
  systemHealth: {
    status: "HEALTHY" | "DEGRADED" | "DOWN";
    uptime: string;
    lastCheck: string;
  };
  volumeToday: { label: string; amount: string; change: string }[];
  wallets: AdminWallet[];
  transactions: AdminTransaction[];
  webhooks: AdminWebhook[];
  riskFlags: AdminRiskFlag[];
  reconciliationReports: ReconciliationReport[];
  auditLogs: AuditLogEntry[];
  riskRuleStats: Record<string, number>;
  riskEngineConfig: Record<string, string | number | boolean>;
  reconciliationMismatches: ReconciliationMismatch[];
  reconciliationStatus: {
    unresolvedMismatchCount: number;
    reportsWithMismatches: number;
  };
  charts: {
    volumeByRange: Record<"7d" | "30d" | "90d", { label: string; value: number }[]>;
    transactionStatus: { label: string; value: number; color: string }[];
    walletStatus: { label: string; value: number; color: string }[];
    operationsWeekly: { label: string; value: number; color?: string }[];
    kpiSparklines: Record<string, number[]>;
  };
};

export const initialAdminDashboard: AdminDashboardData = {
  totalWallets: 0,
  activeWallets: 0,
  frozenWallets: 0,
  totalTransactions: 0,
  failedTransactions: 0,
  pendingTransactions: 0,
  totalRiskFlags: 0,
  criticalRiskFlags: 0,
  unresolvedReconciliationMismatches: 0,
  failedOperations: 0,
  operationCounts: { freezes: 0, reversals: 0, reprocesses: 0, reconciliations: 0 },
  systemHealth: { status: "HEALTHY", uptime: "—", lastCheck: new Date(0).toISOString() },
  volumeToday: [],
  wallets: [],
  transactions: [],
  webhooks: [],
  riskFlags: [],
  reconciliationReports: [],
  auditLogs: [],
  riskRuleStats: {},
  riskEngineConfig: {},
  reconciliationMismatches: [],
  reconciliationStatus: {
    unresolvedMismatchCount: 0,
    reportsWithMismatches: 0,
  },
  charts: {
    volumeByRange: {
      "7d": [],
      "30d": [],
      "90d": [],
    },
    transactionStatus: [],
    walletStatus: [],
    operationsWeekly: [],
    kpiSparklines: {
      volume: [],
      wallets: [],
      risk: [],
    },
  },
};

export function getAdminDashboard(): AdminDashboardData {
  return initialAdminDashboard;
}

export function createDemoAdminDashboard(): AdminDashboardData {
 const now=new Date().toISOString();
 return {...initialAdminDashboard,totalWallets:4,activeWallets:3,frozenWallets:1,totalTransactions:3,pendingTransactions:1,failedTransactions:1,totalRiskFlags:2,criticalRiskFlags:1,unresolvedReconciliationMismatches:1,
 systemHealth:{status:"HEALTHY",uptime:"Local demo",lastCheck:now},
 volumeToday:[{label:"Gross volume today",amount:"GMD 18,750",change:"Sample activity"}],
 wallets:[{id:1,owner:"Fatou Jallow",email:"fatou@example.invalid",status:"ACTIVE",balance:"12500.00",currency:"GMD",kycLevel:"VERIFIED"},{id:2,owner:"Bakau Market",email:"shop@example.invalid",status:"ACTIVE",balance:"48500.00",currency:"GMD",kycLevel:"VERIFIED"},{id:3,owner:"Demo Agent",email:"agent@example.invalid",status:"ACTIVE",balance:"20000.00",currency:"GMD",kycLevel:"VERIFIED"},{id:4,owner:"Sample Review",email:"review@example.invalid",status:"SUSPENDED",balance:"7000.00",currency:"GMD",kycLevel:"PENDING"}],
 transactions:[{id:101,reference:"DEMO-PAY-101",amount:"GMD 250.00",status:"COMPLETED",sourceWalletId:1,destinationWalletId:2,createdAt:now},{id:102,reference:"DEMO-PAY-102",amount:"GMD 5000.00",status:"PENDING",sourceWalletId:4,destinationWalletId:2,createdAt:now},{id:103,reference:"DEMO-PAY-103",amount:"GMD 150.00",status:"FAILED",sourceWalletId:1,destinationWalletId:2,createdAt:now}],
 webhooks:[{id:201,eventType:"payment.completed",status:"PROCESSED",provider:"Sample provider",receivedAt:now,payloadRef:"DEMO-PAY-101"},{id:202,eventType:"payment.failed",status:"FAILED",provider:"Sample provider",receivedAt:now,payloadRef:"DEMO-PAY-103"}],
 riskFlags:[{id:301,walletId:4,transactionId:102,level:"CRITICAL",rule:"Sample velocity rule",description:"Sample rapid payment attempts",resolved:false,createdAt:now},{id:302,walletId:1,level:"MEDIUM",rule:"Sample amount rule",description:"Sample amount review",resolved:false,createdAt:now}],
 reconciliationReports:[{id:401,reconciliationType:"WALLET_LEDGER",status:"COMPLETED",startedAt:now,completedAt:now,totalRecordsChecked:3,mismatchesFound:1,mismatchesResolved:0,summary:"Sample balance mismatch",triggeredBy:"Demo Admin",automated:false}],
 reconciliationMismatches:[{id:501,reportId:401,mismatchType:"BALANCE",entityId:4,entityType:"WALLET",expectedValue:"7000.00",actualValue:"6950.00",variance:"50.00",description:"Sample ledger balance mismatch",resolved:false,createdAt:now}],
 reconciliationStatus:{unresolvedMismatchCount:1,reportsWithMismatches:1},
 auditLogs:[{id:"demo-audit-1",actionType:"FREEZE_WALLET",entityType:"WALLET",entityId:4,actor:"Demo Admin",changeDescription:"Sample review hold",success:true,timestamp:now}],
 charts:{volumeByRange:{"7d":[{label:"Mon",value:12},{label:"Tue",value:9},{label:"Wed",value:18},{label:"Thu",value:14},{label:"Fri",value:21},{label:"Sat",value:15},{label:"Sun",value:19}],"30d":[{label:"Week 1",value:52},{label:"Week 2",value:71},{label:"Week 3",value:64},{label:"Week 4",value:86}],"90d":[{label:"Month 1",value:210},{label:"Month 2",value:273},{label:"Month 3",value:310}]},walletStatus:[{label:"Active",value:3,color:"#f59e0b"},{label:"Frozen",value:1,color:"#ef4444"}],transactionStatus:[{label:"Completed",value:1,color:"#10b981"},{label:"Pending",value:1,color:"#f59e0b"},{label:"Failed",value:1,color:"#ef4444"}],operationsWeekly:[{label:"Freezes",value:1},{label:"Reconciliations",value:1}],kpiSparklines:{volume:[12,9,18,14,21,15,19],wallets:[1,2,2,3,4],risk:[1,3,2,2]}}
 };
}
