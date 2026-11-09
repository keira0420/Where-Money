export interface AssetDayStatistic {
    total: number;
    time: string;
}

export interface AssetDayStatisticTimeResponse {
    dayStatistic: Array<AssetDayStatistic>;
}
