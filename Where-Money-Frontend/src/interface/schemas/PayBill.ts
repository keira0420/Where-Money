import {BaseBill} from "@/interface";

export interface PayBill extends BaseBill {
    payAssetId: number;
    billCategoryId: number;
    refunded: boolean;
}