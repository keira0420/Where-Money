<template>
    <div :style="{ minHeight: `calc(100vh - ${minHeight}px)` }" class="w-full flex flex-col gap-4 xl:gap-4 lg:gap-4">
        <!-- 上面两张卡：手机竖排；xl 横排 9/15 -->
        <div class="grid grid-cols-1 gap-4 xl:grid-cols-24 xl:gap-4 lg:grid-cols-24 lg:gap-4">
            <div class="min-w-0 xl:col-span-9 lg:col-span-9">
                <asset-summarization
                    v-model:debt="debt"
                    v-model:net="net"
                    v-model:total="total"
                    class="h-auto xl:h-[250px]"
                />
            </div>

            <div class="min-w-0 xl:col-span-15 lg:col-span-15">
                <asset-history-chart
                    v-model:mode="historyMode"
                    :statistic-list="dayStatisticList"
                    class="h-[250px]"
                />
            </div>
        </div>

        <!-- 明细：保持一列，避免溢出 -->
        <div class="min-w-0">
            <asset-detail-list
                v-model:asset-list="assetList"
                @asset-list-changed="pullAllAsset"
            />
        </div>
    </div>
</template>

<script lang="ts" setup>

import {computed, onMounted, ref, Ref, watch} from "vue";
import {getAllAsset, getDayStatistic} from "@/apis";
import {Asset, AssetDayStatistic, AssetDayStatisticTimeResponse, AssetGetAllAssetResponse} from "@/interface";
import {AssetDetailList, AssetHistoryChart, AssetSummarization} from "./components";
import {useThemeStore} from "@/store";
import {dateToString} from "@/utils/dateComputer";

const theme = useThemeStore();
const minHeight = computed(() => (theme.tab.height + theme.header.height + theme.footer.height + 32));

// 第一张卡片：资产总览
const total = ref(0);
const debt = ref(0);
const net = ref(0);
const assetList = ref([] as Asset[]);

/**
 * @description 获取所有资产
 */
function pullAllAsset() {
    getAllAsset().then((data: AssetGetAllAssetResponse) => {
        assetList.value = data.assetList;
        total.value = data.totalAsset;
        debt.value = data.totalLiabilities;
        net.value = data.netAsset;
    }).catch((error: any) => {
        console.log(error);
    });
}

watch(assetList, (newValue: Asset[]) => {
    total.value = newValue.reduce((acc: number, cur: Asset) => {
        if (!cur.inTotal) return acc;
        return cur.balance < 0 ? acc : acc + cur.balance;
    }, 0);
    debt.value = newValue.reduce((acc: number, cur: Asset) => {
        if (!cur.inTotal) return acc;
        return cur.balance > 0 ? acc : acc + cur.balance;
    }, 0);
    net.value = total.value + debt.value;
}, {deep: true});

// 第二张卡片：资产变化曲线
const dayStatisticList: Ref<AssetDayStatistic[]> = ref([]);
const historyMode: Ref<"30d" | "365d"> = ref("30d");

/**
 * @description 获取资产变化
 */
function pullAssetDayStatistic() {
    const days = historyMode.value === "365d" ? 365 : 30;
    const endDate = new Date();
    endDate.setHours(23, 59, 59, 0);
    const startDate = new Date(endDate);
    startDate.setDate(startDate.getDate() - (days - 1));
    startDate.setHours(0, 0, 0, 0);
    const startTime = dateToString(startDate);
    const endTime = dateToString(endDate);
    getDayStatistic({
        startTime,
        endTime
    }).then((res: AssetDayStatisticTimeResponse) => {
        dayStatisticList.value = res.dayStatistic;
    }).catch((error: any) => {
        console.log(error);
    });
}

watch(() => historyMode.value, () => {
    pullAssetDayStatistic();
});

onMounted(() => {
    pullAllAsset();
    pullAssetDayStatistic();
});

</script>

<style lang="scss" scoped>
</style>
