export interface BillCategory {
    id: number;
    bookId: number;
    billCategoryName: string;
    svg: string;
    type: "支出" | "收入";
}
