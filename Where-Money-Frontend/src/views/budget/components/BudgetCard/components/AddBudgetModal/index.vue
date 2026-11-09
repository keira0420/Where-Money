<template>
    <n-modal
        :auto-focus="false"
        :show="showModal"
        class="w-9/10 xl:w-992px lg:w-1/2"
        display-directive="show"
        preset="card"
        @close="closeModal"
    >
        <template #header>
            <span class="text-lg font-bold">添加预算</span>
        </template>

        <template #default>
            <n-scrollbar class="h-50 xl:h-auto lg:h-auto">
                <div class="grid grid-cols-6 gap-1 xl:grid-cols-10 xl:gap-4 lg:grid-cols-8 lg:gap-2">
                    <div v-for="(category, index) in billCategoryList"
                         :key="category.id"
                         :class="[
                             store.selectedBillCategoryId == category.id ? 'bg-primary-active' : '',
                             disabledCategories.has(category.id)
                                 ? 'opacity-40 cursor-not-allowed pointer-events-none'
                                 : 'cursor-pointer'
                         ]"
                         class="space-y-2 text-center py-2 rounded xl:rounded-lg xl:p-2 lg:rounded-md lg:p-2"
                         @click="selectCategory(category.id)">
                        <div>
                            <Icon :icon="category.svg"
                                  class="text-primary h-5 w-5 xl:h-8 xl:w-8 m-auto lg:h-6 lg:w-6"></Icon>
                        </div>
                        <div class="text-xs xl:text-sm lg:text-sm">
                            {{ category.billCategoryName }}
                        </div>
                    </div>
                </div>
            </n-scrollbar>

            <n-hr/>

            <div class="flex-y-center justify-between">
                <div class="flex-y-center xl:w-full lg:w-full">
                    <span class="text-base xl:w-1/2 lg:w-1/2">预算总额</span>
                    <n-input-number v-model:value="limitInput"
                                    :show-button="false"
                                    class="ml-2 xl:w-1/2 w-2/3 lg:w-1/2"
                                    placeholder="请输入预算"
                    >
                        <template #prefix>￥</template>
                    </n-input-number>
                </div>
                <n-button class="w-14 xl:w-1/4 ml-2 lg:w-1/4" size="medium" type="primary" @click="submitNewBudget">
                    确定
                </n-button>

            </div>

        </template>
    </n-modal>

</template>

<script lang="ts" setup>

import {defineEmits, defineProps, onMounted, PropType, ref} from "vue";
import {addBudget, getAllBillCategoryApi, setBookBudget} from "@/apis";
import {BillCategory, Book, BookAllBillCategoryResponse} from "@/interface";
import {useStore} from "@/stores/store";
import {Icon} from "@iconify/vue";
import {useMessage} from "naive-ui";

const props = defineProps({
    showModal: {
        type: Boolean,
        required: true
    },
    book: {
        type: Object as PropType<Book>,
        required: true
    },
    disabledCategories: {
        type: Object as PropType<Set<Number>>,
        default: {} as Set<Number>
    }
});

const message = useMessage();
const emit = defineEmits(["update:showModal", "manualUpdateBook"]);

function closeModal() {
    emit("update:showModal", false);
}

const billCategoryList = ref<BillCategory[]>();

function getBillCategories() {
    getAllBillCategoryApi({bookId: props.book.id, type: "支出"}).then((res: BookAllBillCategoryResponse) => {
        billCategoryList.value = res.billCategoryList;
    }).catch((err: Error) => {
        console.error(err);
    });
}

onMounted(() => {
    getBillCategories();
});

const store = useStore();

function selectCategory(id: number) {
    store.selectedBillCategoryId = id;
}

const limitInput = ref<number | null>(null);

function submitNewBudget() {
    if (store.selectedBillCategoryId === -1) {
        message.error("请选择类别！");
        return;
    }
    if (limitInput.value === null) {
        message.error("请输入预算总额！");
        return;
    }
    setBookBudget({bookId: props.book.id, totalBudget: props.book.totalBudget + limitInput.value!}).then(() => {
        addBudget({
            bookId: props.book.id,
            billCategoryId: store.selectedBillCategoryId,
            limit: limitInput.value!
        }).then(() => {
            store.selectedBillCategoryId = -1;
            emit("manualUpdateBook", null);
            message.success("添加成功");
            closeModal();
            limitInput.value == null;
        }).catch((err: Error) => {
            console.error(err);
        });
    }).catch((err: Error) => {
        console.error(err);
    });
}

</script>

<style lang="scss" scoped>
</style>
