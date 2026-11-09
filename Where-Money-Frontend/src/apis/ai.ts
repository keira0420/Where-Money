import request from "@/utils/request";

export type BillType = "支出" | "收入" | "转账";
export interface Draft {
    requestId: string;
    bookId: number;
    type: BillType;
    amount: string;
    transferFee: string | null;
    billTime: string;
    remark: string;
    inAssetId: number | null;
    outAssetId: number | null;
    billCategoryId: number | null;
}
export interface BudgetItem {
    name: string; limit: number | null; used: number; remaining: number | null;
    level: string; message: string;
}
export interface Totals {
    income: number; grossExpense: number; refund: number; transferFee: number;
    entries: number; netExpense: number; balance: number;
}
export interface Report {
    facts: { month: string; current: Totals; previous: Totals; topCategories: { category: string; amount: number }[] };
    narrative: { overview: string; observations: string[]; suggestions: string[] };
    source: "AI" | "RULES" | "EMPTY";
    cached: boolean; warning: string | null; generatedAt: string;
}
export function aiPost(url: string, data: unknown): Promise<any> {
    return request({ method: "post", url: `/ai/${url}`, data,
        headers: { "Content-Type": "application/json" }, skipErrorMessage: true } as any);
}
export function aiGet(url: string, params: unknown): Promise<any> {
    return request({ method: "get", url: `/ai/${url}`, params, skipErrorMessage: true } as any);
}
