<template>
    <n-card v-cloak :segmented="true" class="rounded-[16px] shadow-sm">
        <template #header>
            <div
                class="flex flex-col gap-2 xl:flex-row xl:items-center xl:justify-between lg:flex-row lg:items-center lg:justify-between">
                <span class="text-xl font-bold min-w-0 break-words">{{ book.title }}</span>
                <n-button class="self-start xl:self-auto lg:self-auto" text
                          @click="showAddModal = true; store.selectedBillCategoryId = -1;">
                    <icon height="24px" icon="fluent:add-24-filled"/>
                    <span class="text-base ml-2">新增预算</span>
                </n-button>
            </div>
        </template>

        <template #default>
            <n-empty v-if="budgetList && budgetList.length === 0">
                <span class="text-lg">暂无预算</span>
            </n-empty>

            <template v-else-if="budgetList != undefined">
                <n-card :bordered="false" :segmented="true" size="small">
                    <!-- 账本总预算 -->
                    <template #header>
                        <div
                            class="book-budget-info flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between lg:flex-row lg:items-center lg:justify-between">
                            <!-- 左侧 -->
                            <div class="flex flex-col min-w-0">
                                <span class="text-lg font-bold">总预算</span>
                                <span class="text-xs break-words">总额: {{
                                        formattedCurrencyNoSymbol(book.totalBudget)
                                    }}</span>
                            </div>
                            <!-- 右侧[进度条、修改]切换 -->
                            <div
                                class="flex items-end justify-end self-end xl:self-auto xl:items-center xl:justify-end lg:self-auto lg:items-center lg:justify-end">
                                <div v-if="bookBudgetEdit" class="flex flex-col gap-2">
                                    <n-input-group>
                                        <n-input-group-label size="small">总额</n-input-group-label>
                                        <n-input-number v-model:value="bookTotalBudget" :min="bookBudgetTotalMin"
                                                        :show-button="false"
                                                        :style="{ width: '100px', textAlign: 'center' }"
                                                        size="small">
                                            <template #prefix>￥</template>
                                        </n-input-number>
                                        <n-button text @click="submitBookBudgetChange">
                                            <icon class="text-blue-600" height="32" icon="bi:check"></icon>
                                        </n-button>
                                    </n-input-group>
                                    <n-input-group>
                                        <n-input-group-label size="small">已用</n-input-group-label>
                                        <n-input-number v-model:value="bookUsedBudget" :disabled="true"
                                                        :min="bookBudgetUsedMin"
                                                        :show-button="false"
                                                        :style="{ width: '100px', textAlign: 'center' }"
                                                        size="small">
                                            <template #prefix>￥</template>
                                        </n-input-number>
                                        <n-button text @click="cancelBookBudgetInput">
                                            <icon class="text-red-600" height="32" icon="bi:x"></icon>
                                        </n-button>
                                    </n-input-group>
                                </div>

                                <div v-else class="flex-y-center flex-shrink-0 gap-2">
                                    <n-button text @click="showBookBudgetInput">
                                        <div class="mr-2 h-40px flex items-end">
                                            <icon height="24" horizontalAlign="bottom" icon="carbon:edit"></icon>
                                        </div>
                                    </n-button>
                                    <n-progress :gap-offset-degree="180"
                                                :percentage="book.usedBudget > book.totalBudget ? 100 : (1 - book.usedBudget / book.totalBudget) * 100"
                                                :status="book.usedBudget > book.totalBudget ? 'error' : 'success'"
                                                type="circle">
                                        <div class="text-center">
                                            <p class="text-xs">{{
                                                    book.totalBudget >= book.usedBudget ? "可用" : "超支"
                                                }}</p>
                                            <p class="text-xs">
                                                {{ compactFormatter.format(book.totalBudget - book.usedBudget) }}
                                            </p>
                                        </div>
                                    </n-progress>
                                </div>
                            </div>
                        </div>
                    </template>
                    <!-- 分类预算 -->
                    <template #default>
                        <n-list>
                            <n-list-item v-for="(budget, idx) in budgetList" :key="budget.id"
                                         class="hover:bg-[#f6f6f6] dark:hover:bg-[#333]"
                                         @click="showBudgetInfoModal(budget)"
                            >
                                <div
                                    class="budget-info flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between px-2 lg:flex-row lg:items-center lg:justify-between">
                                    <!-- 左侧 icon和名字、额度信息 -->
                                    <div class="flex-y-center min-w-0">
                                        <icon :height="iconWidth"
                                              :icon="mapBudgetToCategory.get(budget.id)?.svg as any" :width="iconWidth"
                                              class="text-primary hover:text-primary-hover mr-3 flex-shrink-0"/>

                                        <div class="flex flex-col min-w-0">
                                            <span class="text-base break-words">
                                                {{ mapBudgetToCategory.get(budget.id)?.billCategoryName }}
                                            </span>
                                            <span class="text-xs break-words">
                                                总额: {{
                                                    formattedCurrencyNoSymbol(budget.limit)
                                                }} | {{ budget.times.toFixed(0) }} 笔支出
                                            </span>
                                        </div>
                                    </div>
                                    <!-- 右侧进度条 -->
                                    <div class="flex-shrink-0 self-end xl:self-auto lg:self-auto">
                                        <n-progress :gap-offset-degree="180"
                                                    :percentage="budgetRemainList![idx] < 0 ? 100 : (1 - budget.used / budget.limit) * 100"
                                                    :status="budget.used > budget.limit ? 'error' : 'success'"
                                                    type="circle">
                                            <div class="text-center">
                                                <p class="text-xs">
                                                    {{ budgetRemainList![idx] >= 0 ? "可用" : "超支" }}
                                                </p>
                                                <p class="text-xs">
                                                    {{ compactFormatter.format(Math.abs(budgetRemainList![idx])) }}
                                                </p>
                                            </div>
                                        </n-progress>
                                    </div>
                                </div>
                            </n-list-item>
                        </n-list>
                    </template>
                </n-card>
            </template>

            <add-budget-modal v-model:show-modal="showAddModal" :disabled-categories="categoryInUse" v-bind:book="book"
                              @manual-update-book="emit('manualUpdateBook')"/>

            <budget-info-modal v-model:budget="budgetTemp as any" v-model:show-modal="showInfoModal"
                               :category="categoryTemp" @manual-update-book="emit('manualUpdateBook')"/>
        </template>
    </n-card>
</template>

<script lang="ts" setup>

import {computed, defineEmits, defineProps, onMounted, PropType, ref, watch} from "vue";
import {useMessage} from "naive-ui";
import {BillCategory, Book, BookAllBillCategoryResponse, Budget, BudgetGetBudgetByBookResponse} from "@/interface";
import {getAllBillCategoryApi, getBudgetsByBook, setBookBudget} from "@/apis";
import {formattedCurrencyNoSymbol} from "@/utils";
import {useStore} from "@/stores/store";

import {Icon} from "@iconify/vue";
import {AddBudgetModal, BudgetInfoModal} from "./components";

const props = defineProps({
    book: {
        type: Object as PropType<Book>,
        required: true
    }
});

const store = useStore();

const iconWidth = ref(24);
const budgetList = ref<Budget[]>();
const billCategoryList = ref([] as BillCategory[]);
const mapBudgetToCategory = ref(new Map<Number, BillCategory>());
const categoryInUse = ref(new Set<Number>());

function getBudgetList(id: number) {
    getBudgetsByBook({bookId: id}).then((res: BudgetGetBudgetByBookResponse) => {
        budgetList.value = res.budgetList;
        getAllBillCategoryApi({bookId: id, type: "支出"}).then((res: BookAllBillCategoryResponse) => {
            billCategoryList.value = res.billCategoryList;
            budgetList.value?.forEach((budget) => {
                const billCategory = billCategoryList.value.find((billCategory) => billCategory.id === budget.billCategoryId);
                mapBudgetToCategory.value.set(budget.id, billCategory!);
                categoryInUse.value.add(budget.billCategoryId);
            });
        }).catch((err: Error) => {
            console.log(err);
        });
    }).catch((err: Error) => {
        console.log(err);
    });
}

const budgetRemainList = computed(() => (
    budgetList.value?.map((budget) => (budget.limit - budget.used))
));

onMounted(() => {
    getBudgetList(props.book?.id as any);
});

watch(() => props.book,
    (newBook) => getBudgetList(newBook?.id as any),
    {deep: true, immediate: true}
);

const bookTotalBudget = ref(0);
const bookUsedBudget = ref(0);
const bookBudgetTotalMin = computed(() => {
    let sum: number | undefined = 0;
    for (const budget of budgetList.value ?? []) {
        sum += budget.limit;
    }
    return sum;
});
const bookBudgetUsedMin = computed(() => {
    let sum: number | undefined = 0;
    for (const budget of budgetList.value ?? []) {
        sum += budget.used;
    }
    return sum;
});
const bookBudgetEdit = ref(false);

function showBookBudgetInput() {
    bookTotalBudget.value = props.book?.totalBudget as any;
    bookUsedBudget.value = props.book?.usedBudget as any;
    bookBudgetEdit.value = true;
}

function cancelBookBudgetInput() {
    bookBudgetEdit.value = false;
}

const message = useMessage();

function submitBookBudgetChange() {
    const params = {
        bookId: props.book?.id as any,
        totalBudget: bookTotalBudget.value,
        usedBudget: null
    };
    setBookBudget(params).then(() => {
        bookBudgetEdit.value = false;
        emit("manualUpdateBook");
        getBudgetList(props.book?.id as any);
        message.success("修改成功！");
    }).catch((err: Error) => {
        console.log(err);
    });
}

const showAddModal = ref(false);
const emit = defineEmits(["manualUpdateBook"]);

const showInfoModal = ref(false);
const budgetTemp = ref<Budget>({
    id: 0,
    bookId: 0,
    billCategoryId: 0,
    used: 0,
    limit: 0,
    times: 0
});

const categoryTemp = ref<BillCategory>({
    id: 0,
    bookId: 0,
    billCategoryName: "",
    svg: "",
    type: "支出"
});

function showBudgetInfoModal(budget: Budget) {
    budgetTemp.value = budget;
    categoryTemp.value = mapBudgetToCategory.value.get(budget.id)!;
    showInfoModal.value = true;
}

const compactFormatter = new Intl.NumberFormat("en-US", {
    notation: "compact",
    maximumFractionDigits: 2,
    maximumSignificantDigits: 1
});

</script>

<style lang="scss" scoped>
$--small-progress-size: 56px;
$--medium-progress-size: 64px;

.book-budget-info {
    :deep(.n-progress) {
        width: $--medium-progress-size;
        height: $--medium-progress-size;
    }
}

.budget-info {
    :deep(.n-progress) {
        width: $--small-progress-size;
        height: $--small-progress-size;
    }
}

:deep(.n-card__content) {
    .n-card-header {
        padding-top: 0;
    }

    .n-card__content {
        padding: 0;
    }

    .n-list {
        margin: 0;

        .n-list-item {
            border-radius: 8px;

            -webkit-transition-duration: 0.2s;
            transition-duration: 0.2s;

            -webkit-transition-property: background-color;
            transition-property: background-color;

            &:hover svg {
                -webkit-transition-duration: 0.2s;
                transition-duration: 0.2s;

                -webkit-transition-property: color;
                transition-property: color;

                color: var(--primary-color-hover);
            }
        }
    }

    .n-divider {
        margin-top: 0;
    }
}


</style>
