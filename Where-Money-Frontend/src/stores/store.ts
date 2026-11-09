import {defineStore} from "pinia";

export const useStore = defineStore("main", {
    state: () => {
        return {
            selectedBillCategoryId: 0,
            bookId: Number(),
            currentBill: Object(),
            currentBillType: String()
        };
    },
    getters: {},
    actions: {}
});