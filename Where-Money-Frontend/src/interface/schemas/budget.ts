export interface Budget {
    id: number;
    bookId: number;
    billCategoryId: number;
    used: number;
    limit: number;
    times: number;

    [key: string]: number;
}
