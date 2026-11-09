export interface BillCategoryStatisticTimeResponse {
    payStatistic: Array<{ category: string, amount: number, percent: string }>;
    incomeStatistic: Array<{ category: string, amount: number, percent: string }>;
}