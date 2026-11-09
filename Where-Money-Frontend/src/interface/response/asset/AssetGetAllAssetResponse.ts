import {Asset} from "@/interface/schemas";

export interface AssetGetAllAssetResponse {
    assetList: Asset[];
    netAsset: number;
    totalAsset: number;
    totalLiabilities: number;
}
