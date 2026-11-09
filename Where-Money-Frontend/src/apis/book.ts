import http from "@/utils/http";

function addBookApi(params: any): any {
    return http.post("/book/book", params);
}

function getBookApi(params: { id: number }): any {
    return http.get("/book/book", params);
}

function getAllBookApi(): any {
    return http.get("/book/all", null);
}

// 获取账本所有账单
function getAllBillCategoryApi(params: { bookId: number, type: "支出" | "收入" }): any {
    return http.get("/book/bill-category", params);
}

// 获取账本月结余
function getBalanceMonthApi(params: { bookId: number, month: string }): any {
    return http.get("/book/balance-month", params);
}

// 获取账本月收入
function getIncomeMonthApi(params: { bookId: number, month: string }): any {
    return http.get("book/income-month", params);
}

// 获取账本月支出
function getPayMonthApi(params: { bookId: number, month: string }): any {
    return http.get("/book/pay-month", params);
}

function deleteBillCategoryApi(params: { id: number }): any {
    return http.delete("/book/bill-category", params);
}

function addBillCategoryApi(params: {
    bookId: number,
    billCategoryName: string,
    svg: string,
    type: "支出" | "收入"
}): any {
    return http.post("/book/bill-category", params);
}

function updateBillCategoryApi(params: {
    id: number,
    billCategoryName: string,
    svg: string,
    type: "支出" | "收入",
    bookId: number
}): any {
    console.log(params)
    return http.put("/book/bill-category", params);
}

function deleteBookApi(params: { id: number }): any {
    return http.delete("/book/book", params);
}

export {
    addBookApi,
    getBookApi,
    getAllBookApi,
    getAllBillCategoryApi,
    getBalanceMonthApi,
    getIncomeMonthApi,
    getPayMonthApi,
    deleteBillCategoryApi,
    addBillCategoryApi,
    updateBillCategoryApi,
    deleteBookApi
};