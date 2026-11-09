export type BillAiParseType = "支出" | "收入" | "转账";

export interface BillAiParseResponse {
    type: BillAiParseType;
    billCategoryId: number | null;
    billCategoryName: string | null;
    inAssetId: number | null;
    inAssetName: string | null;
    outAssetId: number | null;
    outAssetName: string | null;
    amount: number | null;
    transferFee: number | null;
    billTime: string | null;
    remark: string | null;
}
