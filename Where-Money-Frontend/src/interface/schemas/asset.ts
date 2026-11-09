export interface Asset {
    assetName: string;
    balance: number;
    billDate: number;
    id: number;
    inTotal: boolean;
    quota: number;
    repayDate: number;
    type: "信用卡" | "充值" | "投资理财" | "资金";
    userId: number;
    svg: string;

    [key: string]: any;
}
