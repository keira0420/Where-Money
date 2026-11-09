<template>
    <div :style="{ minHeight: `calc(100vh - ${minHeight}px` }">
        <div class="grid grid-cols-1 gap-4 lg:grid-cols-2 xl:grid-cols-3">
            <div
                v-for="(book, index) in bookList"
                :key="book.id"
            >
                <div class="w-full mx-auto">
                    <budget-card :book="book" @manual-update-book="getBooks"></budget-card>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>

import {computed, onMounted, ref} from "vue";

import {getAllBookApi} from "@/apis";
import {Book, BookGetAllBookResponse} from "@/interface";
import {useThemeStore} from "@/store";

import {BudgetCard} from "./components";

const theme = useThemeStore();
const minHeight = computed(() => (theme.tab.height + theme.header.height + theme.footer.height + 32));

const bookList = ref<Book[]>();

function getBooks() {
    getAllBookApi().then((res: BookGetAllBookResponse) => {
        bookList.value = res.bookList;
    }).catch((err: Error) => {
        console.error(err);
    });
}

onMounted(() => {
    getBooks();
});

</script>

<style lang="scss" scoped>
</style>
