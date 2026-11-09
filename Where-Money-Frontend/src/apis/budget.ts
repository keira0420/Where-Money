import http from "@/utils/http";

export function addBudget(params: { bookId: number, billCategoryId: number, limit: number }): any {
    return http.post("budget/budget", params);
}

export function getBudgetsByBook(params: { bookId: number }): any {
    return http.get("budget/budget/all", params);
}

export function setBookBudget(params: { bookId: number, totalBudget?: number, usedBudget?: number | null }): any {
    return http.patch("budget/budget/total", params);
}

export function deleteBudget(params: { budgetId: number }): any {
    return http.delete("budget/budget", params);
}

export function updateBudget(params: {
    budgetId: number,
    billCategoryId?: number,
    limit?: number,
    amount?: number | null,
    times?: number | null
}): any {
    return http.patch("budget/budget", params);
}
