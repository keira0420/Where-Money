import http from "@/utils/http";

function addAsset(params: {
    assetName: string, balance?: number, billDate?: number | null, repayDate?: number | null, quota?: number | null,
    inTotal?: boolean
}): any {
    return http.post("asset/asset", params);
}

function getAllAsset(): any {
    return http.get("asset/all", null);
}

function getAssetApi(params: { id: number }): any {
    return http.get("asset/asset", params);
}

function getDayStatistic(params: { startTime: string, endTime: string }): any {
    return http.get("asset/day-statistic", params);
}

function updateAsset(params: { assetId: number, [key: string]: any }): any {
    return http.patch("asset/asset", params);
}

export {addAsset, getAllAsset, getAssetApi, getDayStatistic, updateAsset};
