<template>
    <div class="assistant-page">
        <div class="heading">
            <div><h1>AI 记账助手</h1><p>说清一笔收支，检查草稿后保存。数字由账单计算，AI 帮你理解消费。</p></div>
            <n-select v-model:value="bookId" :options="bookOptions" :disabled="busy" placeholder="请选择账本" class="book-select" />
        </div>
        <n-alert v-if="pageError" type="error" closable @close="pageError = ''">{{ pageError }}</n-alert>
        <div v-if="loading" class="loading"><n-spin /></div>
        <n-empty v-else-if="!bookOptions.length" description="请先在账本管理中创建一个账本" />
        <div v-else class="assistant-grid">
            <section>
                <n-card title="一句话记账" class="panel">
                    <n-radio-group v-model:value="type" :disabled="busy">
                        <n-radio-button v-for="item in types" :key="item" :value="item">{{ item }}</n-radio-button>
                    </n-radio-group>
                    <n-input v-model:value="text" type="textarea" :maxlength="1000" show-count
                        :disabled="busy" :autosize="{ minRows: 4, maxRows: 8 }" :placeholder="example" class="text-input" />
                    <div class="example">例如：{{ example }}</div>
                    <div class="actions"><n-button :disabled="busy || !contextReady" @click="manualDraft">手动填写</n-button><n-button type="primary" :loading="parsing" :disabled="busy || !text.trim() || !contextReady" @click="parse">生成账单草稿</n-button></div>
                    <p class="hint">{{ assets.length }} 个可用账户 · {{ categories.length }} 个{{ type }}分类。请明确账户；一次描述一笔交易。</p>
                </n-card>
                <n-card v-if="draft" title="确认账单草稿" class="panel">
                    <n-alert type="info">尚未保存。请核对金额、账户、分类和时间；模糊时间是 AI 推测值。</n-alert>
                    <n-form label-placement="top" class="draft-form" :disabled="busy">
                        <div class="fields">
                            <n-form-item label="金额（元）"><n-input v-model:value="draft.amount" placeholder="0.00" /></n-form-item>
                            <n-form-item v-if="type === '转账'" label="手续费（从转账金额中扣除）"><n-input v-model:value="draft.transferFee" /></n-form-item>
                            <n-form-item v-if="type !== '收入'" :label="type === '转账' ? '转出账户' : '付款账户'">
                                <n-select v-model:value="draft.outAssetId" :options="assetOptions" />
                            </n-form-item>
                            <n-form-item v-if="type !== '支出'" :label="type === '转账' ? '转入账户' : '收款账户'">
                                <n-select v-model:value="draft.inAssetId" :options="assetOptions" />
                            </n-form-item>
                            <n-form-item v-if="type !== '转账'" label="消费 / 收入分类"><n-select v-model:value="draft.billCategoryId" :options="categoryOptions" /></n-form-item>
                            <n-form-item label="账单时间（北京时间）"><input v-model="localBillTime" type="datetime-local" step="1" :disabled="busy" class="native-input" /></n-form-item>
                        </div>
                        <n-form-item label="备注"><n-input v-model:value="draft.remark" :maxlength="255" /></n-form-item>
                    </n-form>
                    <div class="actions">
                        <n-button :disabled="busy" @click="draft = null">放弃草稿</n-button>
                        <n-button type="primary" :loading="saving" :disabled="busy" @click="save">确认并保存</n-button>
                    </div>
                </n-card>
                <n-alert v-if="savedMessage" type="success">{{ savedMessage }}</n-alert>
                <n-card title="月度收支总结" class="panel">
                    <div class="report-actions"><input v-model="month" type="month" :max="currentMonth" :disabled="busy" class="native-input" />
                        <n-button type="primary" secondary :loading="reporting" :disabled="busy || !bookId" @click="generateReport">生成月报</n-button></div>
                    <p class="hint">按自然月统计。净支出 = 支出 − 当月到账退款 + 转账手续费；转账本金不计入收支。</p>
                    <template v-if="report">
                        <n-alert v-if="report.warning" type="warning">{{ report.warning }}</n-alert>
                        <div class="totals">
                            <div>收入<strong>¥{{ money(report.facts.current.income) }}</strong></div>
                            <div>净支出<strong>¥{{ money(report.facts.current.netExpense) }}</strong></div>
                            <div>结余<strong>¥{{ money(report.facts.current.balance) }}</strong></div>
                        </div>
                        <p class="hint">上月：收入 ¥{{ money(report.facts.previous.income) }} / 净支出 ¥{{ money(report.facts.previous.netExpense) }} · 本月 {{ report.facts.current.entries }} 笔流水</p>
                        <div class="source">{{ report.source === 'AI' ? 'AI 解读' : report.source === 'EMPTY' ? '暂无账单' : '系统基础统计' }}{{ report.cached ? ' · 使用缓存' : '' }}</div>
                        <p class="overview">{{ report.narrative.overview }}</p>
                        <ul><li v-for="(item, i) in report.narrative.observations" :key="`o${i}`">{{ item }}</li></ul>
                        <ul><li v-for="(item, i) in report.narrative.suggestions" :key="`s${i}`">{{ item }}</li></ul>
                        <div v-if="report.facts.topCategories.length" class="category-list"><span>主要消费分类（支出原额）</span>
                            <div v-for="item in report.facts.topCategories" :key="item.category"><span>{{ item.category }}</span><strong>¥{{ money(item.amount) }}</strong></div>
                        </div>
                        <p class="hint">生成于 {{ report.generatedAt }}。AI 文字仅供参考，请以账单统计为准。</p>
                    </template>
                    <n-empty v-else description="选择月份，查看真实收支统计与 AI 解读" />
                </n-card>
            </section>
            <aside><n-card title="预算提醒" class="panel">
                <div class="budget-heading"><span class="hint">{{ budgetPeriod }}</span><n-button text :disabled="busy" @click="loadBudget">刷新</n-button></div>
                <n-alert v-if="budgetError" type="warning">{{ budgetError }}</n-alert>
                <div v-for="(item, i) in budgetItems" :key="i" class="budget-item">
                    <div class="budget-heading"><strong>{{ item.name }}</strong><n-tag size="small" :type="budgetColor(item.level)">{{ budgetLabel(item.level) }}</n-tag></div>
                    <p>{{ item.message }}</p>
                    <n-progress v-if="item.limit !== null && item.limit > 0" type="line" :percentage="Math.min(100, Math.round(item.used / item.limit * 100))" :show-indicator="false" :status="item.level === 'EXCEEDED' ? 'error' : 'default'" />
                    <p class="hint">已用 ¥{{ money(item.used) }} / {{ item.limit === null ? '未设置上限' : `预算 ¥${money(item.limit)}` }}</p>
                </div>
                <p class="hint">使用账本的当前预算周期，达到 80% 提醒，用完或超支时预警。保存账单后自动刷新。</p>
            </n-card></aside>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { getAllBookApi } from "@/apis";
import { aiGet, aiPost, type BillType, type BudgetItem, type Draft, type Report } from "@/apis/ai";
import { useStore } from "@/stores/store";

const store = useStore();
const bookId = ref<number | null>(null);
const bookOptions = ref<{ label: string; value: number }[]>([]);
const types: BillType[] = ["支出", "收入", "转账"];
const type = ref<BillType>("支出");
const text = ref("");
const loading = ref(true);
const parsing = ref(false);
const saving = ref(false);
const reporting = ref(false);
const busy = computed(() => parsing.value || saving.value || reporting.value);
const pageError = ref("");
const savedMessage = ref("");
const contextReady = ref(false);
const assets = ref<any[]>([]);
const categories = ref<any[]>([]);
const assetOptions = computed(() => assets.value.map(a => ({ label: a.assetName, value: a.id })));
const categoryOptions = computed(() => categories.value.map(c => ({ label: c.billCategoryName, value: c.id })));
const draft = ref<Draft | null>(null);
const localBillTime = ref("");
const currentMonth = new Intl.DateTimeFormat("sv-SE", { timeZone: "Asia/Shanghai", year: "numeric", month: "2-digit" }).format(new Date());
const month = ref(currentMonth);
const report = ref<Report | null>(null);
const budgetItems = ref<BudgetItem[]>([]);
const budgetPeriod = ref("");
const budgetError = ref("");
let generation = 0;
const example = computed(() => type.value === "支出" ? "昨晚用支付宝吃火锅花了128元"
    : type.value === "收入" ? "今天工资5000元到账招商银行卡" : "今天从招商银行卡转1000元到支付宝，手续费2元");
const money = (value: number) => Number(value ?? 0).toFixed(2);
const errorText = (e: any) => e?.message || "请求失败，请稍后重试";
const budgetColor = (level: string): "error" | "warning" | "success" | "default" =>
    level === "EXCEEDED" ? "error" : ["WARNING", "REACHED"].includes(level) ? "warning" : level === "NORMAL" ? "success" : "default";
const budgetLabel = (level: string) => ({ EXCEEDED: "已超支", REACHED: "已用完", WARNING: "接近上限", NORMAL: "正常", UNSET: "未设置" }[level] || level);

async function loadBudget() {
    if (!bookId.value) return;
    const selected = bookId.value;
    budgetError.value = "";
    try {
        const data = await aiGet("budget-alerts", { bookId: selected });
        if (bookId.value !== selected) return;
        budgetItems.value = data.items;
        budgetPeriod.value = `${data.periodStart} 至 ${data.periodEnd}（不含）`;
    } catch (e) { if (bookId.value === selected) budgetError.value = errorText(e); }
}

watch([bookId, type], async () => {
    const version = ++generation;
    draft.value = null; savedMessage.value = ""; contextReady.value = false;
    assets.value = []; categories.value = []; pageError.value = "";
    if (!bookId.value) return;
    store.bookId = bookId.value;
    try {
        const data = await aiGet("context", { bookId: bookId.value, type: type.value });
        if (version !== generation) return;
        assets.value = data.assets; categories.value = data.categories;
        contextReady.value = true;
        if (!assets.value.length) pageError.value = "请先到资产页面添加付款或收款账户。";
    } catch (e) { if (version === generation) pageError.value = errorText(e); }
});
watch(bookId, () => { report.value = null; budgetItems.value = []; loadBudget(); });
watch(() => store.bookId, value => { if (value && !busy.value && bookOptions.value.some(b => b.value === value)) bookId.value = value; });
watch(month, () => { report.value = null; });

async function parse() {
    if (busy.value || !bookId.value) return;
    parsing.value = true; pageError.value = ""; savedMessage.value = ""; draft.value = null;
    try {
        const data = await aiPost("parse", { bookId: bookId.value, type: type.value, text: text.value.trim() });
        localBillTime.value = data.billTime.replace(" ", "T");
        draft.value = { requestId: crypto.randomUUID(), bookId: bookId.value, type: type.value,
            amount: String(data.amount), transferFee: type.value === "转账" ? String(data.transferFee ?? 0) : null,
            billTime: data.billTime, remark: data.remark ?? "", outAssetId: data.outAssetId,
            inAssetId: data.inAssetId, billCategoryId: data.billCategoryId };
    } catch (e) { pageError.value = errorText(e); }
    finally { parsing.value = false; }
}

function manualDraft() {
    if (!bookId.value) return;
    const now = new Intl.DateTimeFormat("sv-SE", { timeZone: "Asia/Shanghai", year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit", second: "2-digit" }).format(new Date());
    localBillTime.value = now.replace(" ", "T");
    draft.value = { requestId: crypto.randomUUID(), bookId: bookId.value, type: type.value,
        amount: "", transferFee: type.value === "转账" ? "0" : null, billTime: now, remark: "",
        inAssetId: null, outAssetId: null, billCategoryId: null };
    pageError.value = ""; savedMessage.value = "";
}

async function save() {
    if (busy.value || !draft.value) return;
    const amountPattern = /^(?:0|[1-9]\d{0,7})(?:\.\d{1,2})?$/;
    if (!amountPattern.test(draft.value.amount) || Number(draft.value.amount) <= 0) {
        pageError.value = "请输入大于0、最多两位小数的金额"; return;
    }
    if (!localBillTime.value) { pageError.value = "请选择账单时间"; return; }
    saving.value = true; pageError.value = "";
    draft.value.billTime = localBillTime.value.replace("T", " ");
    if (draft.value.billTime.length === 16) draft.value.billTime += ":00";
    try {
        await aiPost("confirm", draft.value);
        savedMessage.value = "账单已保存，账户余额和预算已更新。可到账单明细查看。";
        draft.value = null; text.value = ""; report.value = null;
        await loadBudget();
    } catch (e) { pageError.value = errorText(e); }
    finally { saving.value = false; }
}

async function generateReport() {
    if (busy.value || !bookId.value) return;
    reporting.value = true; pageError.value = "";
    try { report.value = await aiPost("monthly-report", { bookId: bookId.value, month: month.value }); }
    catch (e) { pageError.value = errorText(e); }
    finally { reporting.value = false; }
}

onMounted(async () => {
    try {
        const data = await getAllBookApi();
        bookOptions.value = data.bookList.map((b: any) => ({ label: b.title, value: b.id }));
        bookId.value = bookOptions.value.some(b => b.value === store.bookId) ? store.bookId : bookOptions.value[0]?.value ?? null;
    } catch (e) { pageError.value = errorText(e); }
    finally { loading.value = false; }
});
</script>

<style scoped>
.assistant-page { max-width: 1280px; margin: auto; }
.heading { display: flex; justify-content: space-between; align-items: center; gap: 24px; margin-bottom: 22px; }
h1 { font-size: 25px; font-weight: 650; margin: 0 0 8px; }
.heading p, .hint, .example { color: #858993; font-size: 13px; line-height: 1.7; }
.book-select { width: 220px; flex-shrink: 0; }
.assistant-grid { display: grid; grid-template-columns: minmax(0, 1fr) 320px; gap: 22px; margin-top: 18px; }
.panel { border-radius: 14px; margin-bottom: 20px; }
.text-input { margin-top: 18px; }
.example { margin: 10px 0; }
.actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 15px; }
.draft-form { margin-top: 18px; }
.fields { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 18px; }
.native-input { width: 100%; padding: 7px 12px; border: 1px solid #9996; border-radius: 4px; background: transparent; color: inherit; color-scheme: light dark; }
.report-actions { display: flex; align-items: center; gap: 14px; }
.report-actions input { width: auto; }
.totals { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin: 22px 0 15px; }
.totals strong { display: block; font-size: 22px; margin-top: 6px; font-variant-numeric: tabular-nums; }
.source { margin-top: 20px; color: #18a058; font-weight: 600; }
.overview { line-height: 1.9; }
ul { padding-left: 20px; line-height: 1.9; list-style: disc; }
.budget-heading, .category-list div { display: flex; justify-content: space-between; gap: 10px; align-items: center; }
.budget-item { padding: 18px 0; border-bottom: 1px solid #9993; }
.budget-item p { margin: 9px 0; }
.category-list { margin-top: 22px; }
.category-list div { padding: 10px 0; border-bottom: 1px solid #9992; }
.loading { padding: 50px; text-align: center; }
@media (max-width: 900px) { .assistant-grid { grid-template-columns: 1fr; } .heading { align-items: flex-start; flex-direction: column; } .book-select { width: 100%; } }
@media (max-width: 480px) { .fields { grid-template-columns: 1fr; } .totals strong { font-size: 17px; } }
</style>
