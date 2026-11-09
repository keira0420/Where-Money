export interface BillDayStatisticTimeResponse {
    payStatistic: Array<{ day: string, amount: number }>;
    incomeStatistic: Array<{ day: string, amount: number }>;
}