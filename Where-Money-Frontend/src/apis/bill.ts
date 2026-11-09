import http from "@/utils/http";
import request from "@/utils/request";
import {BillAiParseResponse} from "@/interface";

function addBillApi(params: any): any {
    return http.post("/bill/bill", params);
}

function getBillApi(params: any): any {
    return http.get("/bill/bill", params);
}

function changeBillApi(params: any): any {
    return http.patch("/bill/bill", params);
}

function deleteBillApi(params: { id: number, type: string }): any {
    return http.delete("/bill/bill", params);
}

function deleteBillImageApi(params: { id: number, type: string }): any {
    return http.delete("/bill/image", params);
}

// 获取指定账本的所有账单
function getAllBillInBookApi(params: { bookId: number }): any {
    return http.get("/bill/all", params);
}

function getAllBillTimeApi(params: { bookId: number, startTime: string, endTime: string }): any {
    return http.get("/bill/all/time", params);
}

// 分日统计指定账本所有账单
function getDayStatisticTimeApi(params: { bookId: number, startTime: string, endTime: string }): any {
    return http.get("/bill/statistic/day", params);
}

// 获取指定账本下的所有账单分类
function getBillCategoryApi(params: { bookId: number }): any {
    return http.get("/bill/category", params);
}

//分类统计指定账本指定日期内的账单
function getCategoryStatisticTimeApi(params: { bookId: Number, startTime: string, endTime: string }): any {
    return http.get("/bill/statistic/category", params);
}

function getMaxMinPayTimeApi(params: { bookId: Number, startTime: string, endTime: string }): any {
    return http.get("/bill/statistic/pay", params);
}

function getMaxMinIncomeTimeApi(params: { bookId: Number, startTime: string, endTime: string }): any {
    return http.get("/bill/statistic/income", params);
}

function getBillImageApi(params: { billId: number, type: string }): any {
    return http.get("/bill/image", params);
}

function aiParseBillApi(params: { bookId: number, type: "支出" | "收入" | "转账", text: string }): Promise<BillAiParseResponse> {
    return request({
        method: "post",
        url: "/bill/ai/parse",
        params,
        skipErrorMessage: true
    } as any);
}

export {
    addBillApi,
    getBillApi,
    changeBillApi,
    deleteBillApi,
    deleteBillImageApi,
    getAllBillInBookApi,
    getAllBillTimeApi,
    getDayStatisticTimeApi,
    getBillCategoryApi,
    getCategoryStatisticTimeApi,
    getMaxMinPayTimeApi,
    getMaxMinIncomeTimeApi,
    getBillImageApi,
    aiParseBillApi,
};
