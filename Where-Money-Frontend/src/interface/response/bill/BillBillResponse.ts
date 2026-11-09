export interface BillBillResponse {
    amount: number;
    billTime: string;
    payAsset?: string;
    billCategory?: string;
    refunded?: boolean;
    incomeAsset?: string;
    inAsset?: string;
    outAsset?: string;
    transferFee?: number;
    payBillId?: number;
    refundAsset?: string;
    id: number;
    remark: string,
    type: "支出" | "收入" | "转账" | "退款";
}