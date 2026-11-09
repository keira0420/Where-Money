<template>
    <div>
        <div class="space-y-5 lg:flex lg:gap-5 lg:space-y-0 xl:flex xl:space-x-5 xl:gap-0">
            <div class="space-y-5 lg:w-1/2 xl:contents xl:space-y-0 xl:space-x-5">
                <div class="xl:w-7/20 xl:order-1 space-y-5">
                    <div class="xl:flex xl:space-x-3 space-y-3 xl:space-y-0 lg:flex lg:space-x-3 lg:space-y-0">
                        <div class="xl:w-1/2 lg:w-1/2">
                            <MonthStatistic type="balance" v-bind:amount="balanceMonth">
                            </MonthStatistic>
                        </div>
                        <div class="xl:w-1/2 space-y-3 lg:w-1/2">
                            <MonthStatistic type="pay" v-bind:amount="payMonth">
                            </MonthStatistic>
                            <MonthStatistic type="income" v-bind:amount="incomeMonth">
                            </MonthStatistic>
                        </div>
                    </div>
                    <n-card class="rounded-xl xl:h-125">
                        <template #default>
                            <n-config-provider :date-locale="dateZhCNSingleWeekday">
                                <n-calendar class="xl:h-105 h-85" @panel-change="changeMonth">
                                    <template #default="{ year, month, date }">
                                        <n-popover
                                            :disabled="getCellStat(year, month, date).pay === 0 && getCellStat(year, month, date).income === 0"
                                            :on-clickoutside="closeDayPopover"
                                            :show="activePopoverKey === getCellStat(year, month, date).key"
                                            :show-arrow="false"
                                            placement="bottom"
                                            trigger="manual"
                                        >
                                            <template #trigger>
                                                <div class="w-full pt-1" v-on:click="openDayPopover(year, month, date)">
                                                    <!-- 手机：只显示点 -->
                                                    <div class="flex items-center gap-1 xl:hidden">
                                                    <span
                                                        v-if="getCellStat(year, month, date).pay > 0"
                                                        class="h-1.5 w-1.5 rounded-full bg-red-500"
                                                    />
                                                        <span
                                                            v-if="getCellStat(year, month, date).income > 0"
                                                            class="h-1.5 w-1.5 rounded-full bg-green-500"
                                                        />
                                                    </div>

                                                    <!-- 电脑：显示金额 -->
                                                    <div class="hidden xl:block space-y-0.5">
                                                        <div
                                                            v-if="getCellStat(year, month, date).pay > 0"
                                                            class="text-red-500 text-xs whitespace-nowrap"
                                                        >
                                                            -{{ fmtCalendarAmount(getCellStat(year, month, date).pay) }}
                                                        </div>
                                                        <div
                                                            v-if="getCellStat(year, month, date).income > 0"
                                                            class="text-green-500 text-xs whitespace-nowrap"
                                                        >
                                                            +{{
                                                                fmtCalendarAmount(getCellStat(year, month, date).income)
                                                            }}
                                                        </div>
                                                    </div>
                                                </div>
                                            </template>

                                            <div class="text-sm leading-6">
                                                <div class="text-xs text-gray-500">
                                                    {{ getCellStat(year, month, date).key }}
                                                </div>
                                                <div class="text-red-500">
                                                    支出：-{{ fmtCalendarAmount(getCellStat(year, month, date).pay) }}
                                                </div>
                                                <div class="text-green-600">
                                                    收入：+{{ fmtCalendarAmount(getCellStat(year, month, date).income) }}
                                                </div>
                                                <div>
                                                    净额：{{
                                                        (getCellStat(year, month, date).income - getCellStat(year, month, date).pay).toFixed(2)
                                                    }}
                                                </div>
                                            </div>
                                        </n-popover>
                                    </template>
                                </n-calendar>
                            </n-config-provider>
                        </template>
                    </n-card>
                </div>
                <div class="xl:w-3/10 xl:order-3 space-y-5">
                    <n-card class="rounded-xl">
                        <template #header>
                            <div>分类统计</div>
                        </template>
                        <template #header-extra>
                            <div class="flex items-center space-x-2 text-sm">
                                <span>月</span>
                                <n-switch v-model:value="pieYearMode">
                                </n-switch>
                                <span>年</span>
                            </div>
                        </template>
                        <template #default>
                            <n-tabs v-model:value="pieSwitch" :animated="true" type="line" @update:value="refreshPie">
                                <n-tab-pane name="支出" tab="支出">
                                    <div ref="payCateRef" class="h-80">
                                    </div>
                                </n-tab-pane>
                                <n-tab-pane name="收入" tab="收入">
                                    <div ref="incomeCateRef" class="h-80">
                                    </div>
                                </n-tab-pane>
                            </n-tabs>
                        </template>
                    </n-card>
                    <n-card class="rounded-xl">
                        <n-scrollbar class="max-h-120">
                            <n-data-table :columns="cateColumns" :data="cateData">
                            </n-data-table>
                        </n-scrollbar>
                    </n-card>
                </div>
            </div>
            <div class="lg:w-1/2 xl:w-7/20 xl:order-2 space-y-5">
                <div>
                    <n-card class="rounded-xl">
                        <template #header>
                            <div>收支分布</div>
                        </template>
                        <template #default>
                            <n-tabs v-model:value="lineSwitch" :animated="true" type="line" @update:value="refreshLine">
                                <n-tab-pane name="支出" tab="支出">
                                    <div ref="payDayRef" class="h-80">
                                    </div>
                                </n-tab-pane>
                                <n-tab-pane name="收入" tab="收入">
                                    <div ref="incomeDayRef" class="h-80">
                                    </div>
                                </n-tab-pane>
                            </n-tabs>
                        </template>
                    </n-card>
                </div>
                <n-card class="rounded-xl w-full">
                    <template #default>
                        <div class="flex space-x-3">
                            <n-statistic class="w-1/2">
                                <template #label>
                                    日均支出
                                </template>
                                <template #prefix>
                                    ￥
                                </template>
                                <template #default>
                                    {{ dayPayAverage }}
                                </template>
                            </n-statistic>
                            <n-statistic class="w-1/2">
                                <template #label>
                                    日均收入
                                </template>
                                <template #prefix>
                                    ￥
                                </template>
                                <template #default>
                                    {{ dayIncomeAverage }}
                                </template>
                            </n-statistic>
                        </div>
                    </template>
                </n-card>
                <n-card class="rounded-xl">
                    <n-scrollbar class="max-h-120">
                        <n-data-table :columns="dayColumns" :data="dayData">
                        </n-data-table>
                    </n-scrollbar>
                </n-card>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import {computed, ComputedRef, h, nextTick, onMounted, Ref, ref, watch} from "vue";
import {getBalanceMonthApi, getIncomeMonthApi, getPayMonthApi} from "@/apis/book";
import {useStore} from "@/stores/store";
import {dateToString, now} from "@/utils/dateComputer";
import {Icon} from "@iconify/vue";
import {dateZhCN, type NDateLocale} from "naive-ui";
import {
    BillCategory,
    BillDayStatisticTimeResponse,
    BillMaxMinTimeResponse,
    BookBalanceMonthResponse,
    BookIncomeMonthResponse,
    BookPayMonthResponse
} from "@/interface";
import {MonthStatistic} from "@/views/components";
import * as echarts from "echarts";
import {
    getBillCategoryApi,
    getCategoryStatisticTimeApi,
    getDayStatisticTimeApi,
    getMaxMinIncomeTimeApi,
    getMaxMinPayTimeApi
} from "@/apis";
import {BillCategoryStatisticTimeResponse} from "@/interface/response/bill/BillCategoryStatisticTimeResponse";
import {useThemeStore} from "@/store";

const dateZhCNSingleWeekday: NDateLocale = {
    ...dateZhCN,
    locale: {
        ...dateZhCN.locale,
        localize: {
            ...dateZhCN.locale.localize,
            day: (n: any, options: any) =>
                dateZhCN.locale.localize?.day(n, {...options, width: "narrow"})
        }
    } as any
};

const themeStore = useThemeStore();
const store = useStore();
let bookId = ref<number>(0);

interface DayStatisticTime extends BillDayStatisticTimeResponse {
}

interface CategoryStatisticTime extends BillCategoryStatisticTimeResponse {
}

let dayStatisticTime: Ref<DayStatisticTime> = ref({payStatistic: [], incomeStatistic: []});
let categoryStatisticTime: Ref<CategoryStatisticTime> = ref({payStatistic: [], incomeStatistic: []});
let lineSwitch: Ref<"支出" | "收入"> = ref("支出");
let pieSwitch: Ref<"支出" | "收入"> = ref("支出");
let maxPay: Ref<number> = ref(0);
let minPay: Ref<number> = ref(0);
let maxIncome: Ref<number> = ref(0);
let minIncome: Ref<number> = ref(0);
let pieYearMode: Ref<boolean> = ref(false);

function getData() {
    getBalanceMonth();
    getIncomeMonth();
    getPayMonth();
    getDayStatisticTimeApi({
        bookId: bookId.value,
        startTime: startTime.value,
        endTime: endTime.value
    }).then((response: BillDayStatisticTimeResponse) => {
        dayStatisticTime.value = response;
        getMaxMinPayTimeApi({
            bookId: bookId.value,
            startTime: startTime.value,
            endTime: endTime.value
        }).then((response: BillMaxMinTimeResponse) => {
            maxPay.value = response.max.amount;
            minPay.value = response.min.amount;
            getMaxMinIncomeTimeApi({
                bookId: bookId.value,
                startTime: startTime.value,
                endTime: endTime.value
            }).then((response: BillMaxMinTimeResponse) => {
                maxIncome.value = response.max.amount;
                minIncome.value = response.min.amount;
                nextTick(() => {
                    initLineChart(lineSwitch.value);
                });
            }).catch(() => {
            });
        }).catch(() => {
        });
    }).catch(() => {
    });
    getCategoryStatisticData();
}

let billCategory: Ref<Array<BillCategory>> = ref([]);

onMounted(() => {
    bookId.value = store.bookId;
    if (bookId.value !== 0) {
        getData();
        getBillCategoryApi({bookId: bookId.value}).then((res: any) => {
            billCategory.value = res;
        }).catch(() => {
        });
    }
});
watch(() => store.bookId, (newValue: number) => {
    bookId.value = newValue;
    if (bookId.value !== 0) {
        getData();
        getBillCategoryApi({bookId: bookId.value}).then((res: any) => {
            billCategory.value = res;
        }).catch(() => {
        });
    }
});
let activeYear: Ref<number> = ref(now().getFullYear());
let activeMonth: Ref<number> = ref(now().getMonth());
let days: ComputedRef<Array<number>> = computed(() => {
    let days: Array<number> = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
    if (activeYear.value % 4 === 0 && activeYear.value % 100 !== 0 || activeYear.value % 400 === 0) {
        days[1] = 29;
    }
    return days;
});
let startTime: ComputedRef<string> = computed(() => {
    return dateToString(new Date(activeYear.value, activeMonth.value, 1, 0, 0, 0));
});
let endTime: ComputedRef<string> = computed(() => {
    return dateToString(new Date(activeYear.value, activeMonth.value, days.value[activeMonth.value], 23, 59, 59));
});
let categoryStartTime: ComputedRef<string> = computed(() => {
    if (pieYearMode.value) {
        return dateToString(new Date(activeYear.value, 0, 1, 0, 0, 0));
    }
    return startTime.value;
});
let categoryEndTime: ComputedRef<string> = computed(() => {
    if (pieYearMode.value) {
        return dateToString(new Date(activeYear.value, 11, 31, 23, 59, 59));
    }
    return endTime.value;
});
let balanceMonth: Ref<number> = ref(0);

function getBalanceMonth(): void {
    getBalanceMonthApi({
        bookId: bookId.value,
        month: startTime.value
    }).then((bookBalanceMonthResponse: BookBalanceMonthResponse) => {
        balanceMonth.value = bookBalanceMonthResponse.amount;
    });
}

let incomeMonth: Ref<number> = ref(0);

function getIncomeMonth(): void {
    getIncomeMonthApi({bookId: bookId.value, month: startTime.value}).then((response: BookIncomeMonthResponse) => {
        incomeMonth.value = response.amount;
    });
}

let payMonth: Ref<number> = ref(0);

function getPayMonth(): void {
    getPayMonthApi({bookId: bookId.value, month: startTime.value}).then((response: BookPayMonthResponse) => {
        payMonth.value = response.amount;
    });
}

let payDayRef: Ref<HTMLElement | undefined> = ref();
let incomeDayRef: Ref<HTMLElement | undefined> = ref();

function initLineChart(type: "支出" | "收入"): void {
    const dom = (type === "支出" ? payDayRef.value : incomeDayRef.value) as HTMLElement | undefined;
    if (!dom) {
        return;
    }
    const lineChart = echarts.getInstanceByDom(dom) ?? echarts.init(dom);
    lineChart.clear();
    let xData: Array<string> = [];
    for (let i = 1; i <= days.value[activeMonth.value]; i++) {
        xData.push(i.toString());
    }
    let lineData: Array<number> = [];
    let maxNum = 0;
    let minNum = 0;
    (type === "支出" ? dayStatisticTime.value.payStatistic : dayStatisticTime.value.incomeStatistic).forEach((item: {
        day: string,
        amount: number
    }) => {
        lineData.push(item.amount);
        if (item.amount > maxNum) {
            maxNum = item.amount;
        }
        if (item.amount < minNum) {
            minNum = item.amount;
        }
    });
    let option = {
        xAxis: {
            type: "category",
            data: xData
        },
        yAxis: {
            type: "value",
            name: `最大一笔：${type === "支出" ? maxPay.value : maxIncome.value} 最小一笔：${type === "支出" ? minPay.value : minIncome.value}`,
            nameGap: 20,
            nameTextStyle: {
                padding: [0, 0, 0, 120],
            },
        },
        tooltip: {
            show: true
        },
        series: [
            {
                data: lineData,
                type: "bar",
                color: type === "支出" ? "red" : "green"
            }
        ]
    };
    lineChart.setOption(option, true);
    if (!charts.includes(lineChart)) {
        charts.push(lineChart);
    }
}

function refreshLine(value: string): void {
    nextTick(() => {
        nextTick(() => {
            initLineChart(value === "支出" ? "支出" : "收入");
        });
    });
}

function fmtCalendarAmount(amount: number): string {
    const x = Math.abs(amount);
    if (x >= 10000) {
        return (Math.round((amount / 10000) * 10) / 10) + "万";
    }
    return String(amount);
}

// ------------------ Calendar cell popover (点击单元格弹出) ------------------
type DayAmount = { day: string; amount: number };

const dayStatMap = computed(() => {
    const map = new Map<string, { pay: number; income: number }>();
    for (const it of dayStatisticTime.value.payStatistic as DayAmount[]) {
        map.set(it.day, {pay: it.amount, income: 0});
    }
    for (const it of dayStatisticTime.value.incomeStatistic as DayAmount[]) {
        const prev = map.get(it.day) ?? {pay: 0, income: 0};
        prev.income = it.amount;
        map.set(it.day, prev);
    }
    return map;
});

const activePopoverKey = ref<string | null>(null);

function getCellStat(year: number, month: number, day: number) {
    const key = dateToString(new Date(year, month - 1, day));
    const v = dayStatMap.value.get(key) ?? {pay: 0, income: 0};
    return {key, pay: v.pay ?? 0, income: v.income ?? 0};
}

function openDayPopover(year: number, month: number, day: number) {
    const {key, pay, income} = getCellStat(year, month, day);
    if (pay === 0 && income === 0) {
        activePopoverKey.value = null;
        return;
    }
    activePopoverKey.value = key;
}

function closeDayPopover() {
    activePopoverKey.value = null;
}

function changeMonth(info: { year: number, month: number }): void {
    activeYear.value = info.year;
    activeMonth.value = info.month - 1;
    activePopoverKey.value = null;
    getData();
}

let payCateRef: Ref<HTMLElement | undefined> = ref();
let incomeCateRef: Ref<HTMLElement | undefined> = ref();

function getCategoryStatisticData(): void {
    getCategoryStatisticTimeApi({
        bookId: bookId.value,
        startTime: categoryStartTime.value,
        endTime: categoryEndTime.value
    }).then((response: BillCategoryStatisticTimeResponse) => {
        categoryStatisticTime.value = response;
        nextTick(() => {
            initPieChart(pieSwitch.value);
        });
    }).catch(() => {
    });
}

function initPieChart(type: "支出" | "收入"): void {
    if ((type === "支出" ? payCateRef.value : incomeCateRef.value) === undefined) {
        return;
    }
    let pieChart = echarts.init((type === "支出" ? payCateRef.value : incomeCateRef.value) as HTMLElement);
    let pieData: Array<{ name: string, value: number }> = [];
    let legendData: Array<string> = [];
    (type === "支出" ? categoryStatisticTime.value.payStatistic : categoryStatisticTime.value.incomeStatistic)
        .forEach((item: { category: string, amount: number, percent: string }, index: number) => {
            pieData.push({
                name: item.category,
                value: item.amount
            });
            if (index < 5) {
                legendData.push(item.category);
            }
        });
    const option = {
        tooltip: {
            trigger: "item"
        },
        title: {
            text: `前${legendData.length}项`,
            right: 40,
            top: 30,
            textStyle: {
                color: "rgb(129,135,138)",
                fontSize: 12
            }
        },
        legend: {
            type: "scroll",
            orient: "vertical",
            right: "0%",
            top: "18%",
            textStyle: {
                color: themeStore.darkMode ? "rgb(213, 213, 214)" : "rgb(31, 34, 37)"
            },
            data: legendData
        },
        series: [
            {
                name: `${type}类型分布`,
                type: "pie",
                radius: ["40%", "80%"],
                left: "-10%",
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 10,
                    borderColor: themeStore.darkMode ? "rgb(24,24,28)" : "rgb(255,255,255)",
                    borderWidth: 1
                },
                label: {
                    show: false,
                    position: "center"
                },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: "28",
                        fontWeight: "bold"
                    }
                },
                labelLine: {
                    show: false
                },
                data: pieData
            }
        ]
    };
    pieChart.setOption(option);
    charts.push(pieChart);
}

let charts: Array<any> = [];
window.onresize = function () {
    for (let i = 0; i < charts.length; i++) {
        charts[i].resize();
    }
};

watch(() => themeStore.darkMode, () => {
    initPieChart(pieSwitch.value);
});
watch(() => pieYearMode.value, () => {
    if (bookId.value !== 0) {
        getCategoryStatisticData();
    }
});

function refreshPie(value: string): void {
    nextTick(() => {
        nextTick(() => {
            initPieChart(value === "支出" ? "支出" : "收入");
        });
    });
}

let dayPayAverage: ComputedRef<number> = computed(() => {
    let nowDate: Date = now();
    let count: number = nowDate.getMonth() == activeMonth.value ? nowDate.getDate() : days.value[activeMonth.value];
    return Math.round(payMonth.value / count * 100) / 100;
});
let dayIncomeAverage: ComputedRef<number> = computed(() => {
    let nowDate: Date = now();
    let count: number = nowDate.getMonth() == activeMonth.value ? nowDate.getDate() : days.value[activeMonth.value];
    return Math.round(incomeMonth.value / count * 100) / 100;
});

class dayItem {
    title: string;
    key: string;
    fixed: string | null;
    width: number | null;
    align: string;
    render: Function | null;
    sorter: string;

    constructor(title: string) {
        this.title = title;
        this.key = title;
        this.fixed = null;
        this.width = null;
        this.align = "left";
        switch (title) {
            case "日期":
                this.render = (row: any) => {
                    return h(
                        "p",
                        {class: "p-0"},
                        {default: () => row[this.key]}
                    );
                };
                break;
            case "支出":
                this.render = (row: any) => {
                    return h(
                        "p",
                        {class: "text-red-500 p-0"},
                        {default: () => row[this.key] >= 10000 ? Math.round(row[this.key] / 1000) / 10 + "万" : row[this.key]}
                    );
                };
                break;
            case "收入":
                this.render = (row: any) => {
                    return h(
                        "p",
                        {class: "text-green-500 p-0"},
                        {default: () => row[this.key] >= 10000 ? Math.round(row[this.key] / 1000) / 10 + "万" : row[this.key]}
                    );
                };
                break;
            default:
                this.render = null;
                break;
        }
        this.sorter = "default";
    }
}

const dayColumns: import("naive-ui").DataTableColumns = [
    new dayItem("日期"),
    new dayItem("支出"),
    new dayItem("收入")
].map(item => ({ title: item.title, key: item.key, sorter: "default" as const,
    render: (row: any) => item.render ? item.render(row) : row[item.key] }));
const dayData: ComputedRef = computed(() => {
    if (dayStatisticTime.value.payStatistic.length === 0 && dayStatisticTime.value.incomeStatistic.length === 0) {
        return [];
    }
    let data: Array<{ 日期: string, 支出: number, 收入: number }> = [];
    let nowDate: Date = now();
    let count: number = nowDate.getMonth() == activeMonth.value ? nowDate.getDate() : days.value[activeMonth.value];
    for (let i = 0; i < (count < dayStatisticTime.value.payStatistic.length ? count : dayStatisticTime.value.payStatistic.length); i++) {
        data.push({
            日期: dayStatisticTime.value.payStatistic[i].day.split(" ")[0],
            支出: dayStatisticTime.value.payStatistic[i].amount,
            收入: dayStatisticTime.value.incomeStatistic[i]?.amount
        });
    }
    return data;
});

class cateItem {
    title: string;
    key: string;
    fixed: string | null;
    width: number | null;
    align: string;
    render: Function | null;
    sorter: string | Function;

    constructor(title: string) {
        this.title = title;
        this.key = title;
        this.fixed = null;
        this.width = null;
        this.align = "left";
        switch (title) {
            case "类别":
                this.render = (row: any) => {
                    return h(
                        "p",
                        {class: "p-0 flex space-x-2"},
                        {
                            default: () => [
                                h(
                                    Icon,
                                    {
                                        icon: row.svg,
                                        class: "my-auto text-primary"
                                    }
                                ),
                                h(
                                    "p",
                                    {class: "my-auto"},
                                    {default: () => row[this.key]}
                                )
                            ]
                        }
                    );
                };
                this.sorter = "default";
                break;
            case "支出":
                this.render = (row: any) => {
                    return h(
                        "p",
                        {class: "text-red-500 p-0"},
                        {default: () => row.amount >= 10000 ? Math.round(row.amount / 1000) / 10 + "万" : row.amount}
                    );
                };
                this.sorter = (a: { amount: number; }, b: { amount: number; }) => {
                    return b.amount - a.amount;
                };
                break;
            case "收入":
                this.render = (row: any) => {
                    return h(
                        "p",
                        {class: "text-green-500 p-0"},
                        {default: () => row.amount >= 10000 ? Math.round(row.amount / 1000) / 10 + "万" : row.amount}
                    );
                };
                this.sorter = (a: { amount: number; }, b: { amount: number; }) => {
                    return b.amount - a.amount;
                };
                break;
            default:
                this.render = null;
                this.sorter = "default";
                break;
        }
    }
}

const cateColumns: ComputedRef<import("naive-ui").DataTableColumns> = computed(() => {
    return [
        new cateItem("类别"),
        new cateItem(pieSwitch.value)
    ].map(item => ({ title: item.title, key: item.key,
        sorter: typeof item.sorter === "function" ? (a: any, b: any) => (item.sorter as Function)(a, b) : "default" as const,
        render: (row: any) => item.render ? item.render(row) : row[item.key] }));
});
const cateData: ComputedRef = computed(() => {
    let data: Array<{ 类别: string, amount: number, svg: string }> = [];
    let rawData: Array<{ category: string, amount: number, percent: string }>
        = (pieSwitch.value === "支出" ? categoryStatisticTime.value.payStatistic : categoryStatisticTime.value.incomeStatistic);
    for (let i = 0; i < rawData.length; i++) {
        let svg: string | undefined = billCategory.value.find((item: BillCategory) => item.billCategoryName === rawData[i].category)?.svg;
        data.push({
            类别: rawData[i].category,
            amount: rawData[i].amount,
            svg: svg ? svg : ""
        });
    }
    return data;
});
</script>
<style scoped>
</style>
