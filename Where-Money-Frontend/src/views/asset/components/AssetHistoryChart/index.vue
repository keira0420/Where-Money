<template>
    <n-card :bordered="false" class="rounded-16px shadow-sm h-250px">
        <template #header>
            <n-space align="center">
                <Icon height="24px" icon="carbon:chart-line" width="24px"/>
                <span class="font-bold text-xl">资产历史</span>
            </n-space>
        </template>
        <template #header-extra>
            <div class="flex items-center space-x-2 text-sm">
                <span>30天</span>
                <n-switch v-model:value="longRangeMode">
                </n-switch>
                <span>365天</span>
            </div>
        </template>

        <template #default>
            <div ref="lineChartAreaRef" class="h-[100%] flex content-center items-center justify-center">
                <n-spin v-if="statisticList.length === 0" size="large"/>
            </div>
        </template>
    </n-card>
</template>

<script lang="ts" setup>
// vue
import {computed, defineEmits, defineProps, PropType, ref, watch} from "vue";
// components
import {Icon} from "@iconify/vue";
import {Line} from "@antv/g2plot";
// .ts
import {AssetDayStatistic} from "@/interface";
import {formattedCurrencyNoSymbol, getThemeColor} from "@/utils";
import {useThemeStore} from "@/store";


const props = defineProps({
    statisticList: {
        type: Array as PropType<Array<AssetDayStatistic>>,
        required: true,
        default: [] as Array<AssetDayStatistic>
    },
    mode: {
        type: String as PropType<"30d" | "365d">,
        required: false,
        default: "30d"
    }
});
const emit = defineEmits<{
    (e: "update:mode", value: "30d" | "365d"): void
}>();

const longRangeMode = computed<boolean>({
    get: () => props.mode === "365d",
    set: (value: boolean) => emit("update:mode", value ? "365d" : "30d")
});

const lineChart = ref<Line>();
const lineChartAreaRef = ref<HTMLElement>();
const key = "__THEME_COLOR__";
const theme = useThemeStore();
let color: string | null = getThemeColor();
watch(
    () => theme.themeColor,
    newValue => {
        color = newValue;
        lineChart.value?.destroy();
        initAndPlotLineChart();
    },
    {immediate: true}
);

function initAndPlotLineChart() {
    if (lineChartAreaRef.value === undefined) {
        return;
    }
    const isXl = window.matchMedia("(min-width: 1280px)").matches;

    const fullFmt = new Intl.NumberFormat("zh-CN", {
        style: "decimal",
        minimumFractionDigits: 0,
        maximumFractionDigits: 2
    });

    const compactFmt = new Intl.NumberFormat("zh-CN", {
        notation: "compact",          // 关键：紧凑表示
        compactDisplay: "short",      // zh-CN 下会是 万/亿
        maximumFractionDigits: 0
    });
    lineChart.value = new Line(lineChartAreaRef.value as any, {
        color: color ? color : "#2092C6",
        data: props.statisticList,
        xField: "time",
        yField: "total",
        xAxis: {
            type: "timeCat",
            label: {
                rotate: -Math.PI / 8,
                offset: 20,
                style: {fontSize: 14}
            }
        },
        yAxis: {
            grid: null,
            min: null,
            max: null,
            tickCount: 4,
            label: {
                style: {fontSize: isXl ? 14 : 12},
                formatter: (text: string) => {
                    const v = Number(text);
                    return isXl ? fullFmt.format(v) : compactFmt.format(v);
                }
            },
            line: {
                "style": {
                    "lineWidth": 1,
                    "stroke": "#BFBFBF"
                }
            },
            tickLine: {
                "style": {
                    "lineWidth": 1,
                    "stroke": "#BFBFBF"
                },
                "alignTick": true,
                "length": 4
            },
            subTickLine: null,
            animate: true
        },
        lineStyle: {
            strokeOpacity: 1,
            lineWidth: 3
        },
        animation: {
            appear: {
                animation: "wave-in",
                duration: 1500
            }
        },
        tooltip: {
            showCrosshairs: true,
            crosshairs: {type: "xy"},
            formatter: (datum: { total: number }) => ({name: "净资产", value: formattedCurrencyNoSymbol(datum.total)}),
            domStyles: {
                "g2-tooltip": {fontSize: "16px"}
            }
        }
    } as any);
    lineChart.value?.render();
}

watch(() => props.statisticList,
    (newVal) => {
        if (!!lineChart.value) {
            lineChart.value?.changeData(newVal as any);
        } else {
            initAndPlotLineChart();
        }
    },
    {deep: true}
);
</script>

<style lang="scss" scoped>
</style>
