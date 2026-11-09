<template>
    <div>
        <div
            class="flex flex-col space-y-5 xl:flex-row xl:space-y-0 xl:space-x-5 lg:flex-row lg:space-y-0 lg:space-x-5">
            <div class="w-full space-y-5 xl:w-7/20">
                <div
                    class="flex flex-col space-y-3 xl:flex-row xl:space-y-0 xl:space-x-3 lg:flex-row lg:space-y-0 lg:space-x-3">
                    <div class="w-full xl:w-1/2 lg:w-1/2">
                        <MonthStatistic type="balance" v-bind:amount="balanceMonth">
                        </MonthStatistic>
                    </div>
                    <div class="w-full space-y-3 xl:w-1/2 lg:w-1/2">
                        <MonthStatistic type="pay" v-bind:amount="payMonth">
                        </MonthStatistic>
                        <MonthStatistic type="income" v-bind:amount="incomeMonth">
                        </MonthStatistic>
                    </div>
                </div>
                <div>
                    <n-card class="rounded-xl xl:h-125">
                        <template #default>
                            <n-config-provider :date-locale="dateZhCNSingleWeekday">
                                <n-calendar class="xl:h-105 lg:h-85 h-85" @update:value="changeDay"
                                            @panel-change="changeMonth">
                                    <template #header="{ year, month }">
                                        <div class="cursor-pointer" v-on:click="getAllBillMonth()">
                                            {{ year + " " + numberToCharMonth(month) }}
                                        </div>
                                    </template>
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
                                                        >
                                                        </span>
                                                        <span
                                                            v-if="getCellStat(year, month, date).income > 0"
                                                            class="h-1.5 w-1.5 rounded-full bg-green-500"
                                                        >
                                                        </span>
                                                    </div>

                                                    <!-- 电脑：显示金额 -->
                                                    <div class="hidden xl:block space-y-0.5">
                                                        <div
                                                            v-if="getCellStat(year, month, date).pay > 0"
                                                            class="text-red-500 text-xs whitespace-nowrap"
                                                        >
                                                            -{{ fmtDesktop(getCellStat(year, month, date).pay) }}
                                                        </div>
                                                        <div
                                                            v-if="getCellStat(year, month, date).income > 0"
                                                            class="text-green-500 text-xs whitespace-nowrap"
                                                        >
                                                            +{{ fmtDesktop(getCellStat(year, month, date).income) }}
                                                        </div>
                                                    </div>
                                                </div>
                                            </template>

                                            <!-- 弹出内容：你要更丰富也在这里加 -->
                                            <div class="text-sm leading-6">
                                                <div class="text-xs text-gray-500">
                                                    {{ getCellStat(year, month, date).key }}
                                                </div>
                                                <div class="text-red-500">
                                                    支出：-{{ fmtDesktop(getCellStat(year, month, date).pay) }}
                                                </div>
                                                <div class="text-green-600">
                                                    收入：+{{ fmtDesktop(getCellStat(year, month, date).income) }}
                                                </div>
                                                <div>
                                                    净额：{{
                                                        fmtDesktop(getCellStat(year, month, date).income - getCellStat(year, month, date).pay)
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
            </div>
            <div class="w-full xl:w-3/10">
                <div>
                    <n-scrollbar class="xl:h-185 lg:h-150">
                        <DayBillList v-for="item in billDayList" class="mb-3" v-bind:bill-list="item[1]"
                                     v-bind:day="item[0]">
                        </DayBillList>
                    </n-scrollbar>
                </div>
            </div>
            <div class="hidden xl:block xl:w-7/20 space-y-5">
                <n-card v-if="currentBill.id != undefined" class="rounded-xl">
                    <template #header>
                        <div class="flex space-x-3">
                            <div>
                                账单详情
                            </div>
                            <Icon class="h-5 w-5 my-auto" v-bind:class="iconColorMap[currentBill.type]"
                                  v-bind:icon="iconMap[currentBill.type]"/>
                        </div>
                    </template>
                    <template #header-extra>
                        <div class="flex space-x-2">
                            <div class="flex space-x-2 cursor-pointer" v-bind:class="{'text-primary':mouseOnEdit}"
                                 v-on:click="editingChange"
                                 v-on:mouseenter="mouseOnEditChange(true)" v-on:mouseleave="mouseOnEditChange(false)">
                                <div class="text-base">
                                    {{ editing ? "完成" : "编辑" }}
                                </div>
                                <Icon class="my-auto h-5 w-5" icon="fluent:edit-48-regular">
                                </Icon>
                            </div>
                            <div v-if="currentBill.type==='支出'" class="flex space-x-2 cursor-pointer"
                                 v-bind:class="{'text-primary':mouseOnRefund}"
                                 v-on:click="refundChange"
                                 v-on:mouseenter="mouseOnRefundChange(true)" v-on:mouseleave="mouseOnRefundChange(false)">
                                <div class="text-base">
                                    退款
                                </div>
                                <Icon class="my-auto h-5 w-5" icon="ph:arrows-down-up">
                                </Icon>
                                <n-modal v-model:show="showRefundModal" class="w-1/7 xl:block hidden">
                                    <n-card>
                                        <template #header>
                                            <div class="s-title s-underline text-lg">提示</div>
                                        </template>
                                        <template #default>
                                            <div class="text-center space-y-8">
                                                <div class="mx-auto text-lg">确认退款？</div>
                                                <div class="space-y-2 text-left">
                                                    <div class="text-sm text-gray-500">退款金额</div>
                                                    <n-input-number v-model:value="refundAmount" :min="0"
                                                                    :precision="2" class="w-full">
                                                        <template #prefix>￥</template>
                                                    </n-input-number>
                                                </div>
                                                <div class="flex space-x-2 justify-end">
                                                    <n-button v-on:click="refundBill">确定</n-button>
                                                    <n-button v-on:click="showRefundModal = false">取消</n-button>
                                                </div>
                                            </div>
                                        </template>
                                    </n-card>
                                </n-modal>
                            </div>
                            <div class="flex space-x-2 cursor-pointer" v-bind:class="{'text-primary':mouseOnDelete}"
                                 v-on:click="deleteChange"
                                 v-on:mouseenter="mouseOnDeleteChange(true)" v-on:mouseleave="mouseOnDeleteChange(false)">
                                <div class="text-base">
                                    删除
                                </div>
                                <Icon class="my-auto h-5 w-5" icon="material-symbols:delete-forever">
                                </Icon>
                                <n-modal v-model:show="showDeleteModal" class="hidden xl:block w-1/7">
                                    <n-card>
                                        <template #header>
                                            <div class="s-title s-underline text-lg">提示</div>
                                        </template>
                                        <template #default>
                                            <div class="text-center space-y-8">
                                                <div class="mx-auto text-lg">确认删除？</div>
                                                <div class="flex space-x-2 justify-end">
                                                    <n-button v-on:click="deleteBill">确定</n-button>
                                                    <n-button v-on:click="showDeleteModal=false">取消</n-button>
                                                </div>
                                            </div>
                                        </template>
                                    </n-card>
                                </n-modal>
                            </div>
                        </div>
                    </template>
                    <template #default>
                        <n-scrollbar class="xl:max-h-164">
                            <div class="space-y-3">
                                <div class="space-y-2">
                                    <div class="text-lg my-auto">
                                        金额
                                    </div>
                                    <div class="h-10 flex items-center">
                                        <n-input-number v-if="editing" v-model:value="currentBill.amount">
                                            <template #prefix>
                                                ￥
                                            </template>
                                        </n-input-number>
                                        <div v-if="!editing" class="text-15px">
                                            ￥{{ currentBill.amount }}
                                        </div>
                                    </div>
                                </div>
                                <div v-if="currentBill.type === '支出' || currentBill.type === '收入'"
                                     class="space-y-2">
                                    <div class="text-lg my-auto">
                                        类别
                                    </div>
                                    <div class="h-10 flex items-center">
                                        <n-select v-if="editing" v-model:value="currentBill.billCategory as any"
                                                  class="w-1/2"
                                                  size="large" v-bind:options="billCategoryList"
                                                  v-bind:render-label="cateSelectorRender as any">
                                        </n-select>
                                        <div v-if="!editing" class="flex space-x-2 items-center">
                                            <Icon class="h-5 w-5 text-primary"
                                                  v-bind:icon="billCategoryList.find(item => item.billCategoryName === currentBill.billCategory)?.svg as any">
                                            </Icon>
                                            <div class="text-15px">
                                                {{ currentBill.billCategory }}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div v-if="currentBill.type==='支出'" class="space-y-2">
                                    <div class="text-lg my-auto">
                                        支出账户
                                    </div>
                                    <div class="h-10 flex items-center">
                                        <n-select v-if="editing" v-model:value="currentBill.payAsset as any"
                                                  class="w-1/2"
                                                  size="large" v-bind:options="assetList"
                                                  v-bind:render-label="assetSelectorRender as any">
                                        </n-select>
                                        <div v-if="!editing" class="flex space-x-2 items-center">
                                            <Icon class="h-5 w-5 text-primary"
                                                  v-bind:icon="assetList.find(item => item.assetName === currentBill.payAsset)?.svg as any">
                                            </Icon>
                                            <div class="text-15px">
                                                {{ currentBill.payAsset }}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div v-if="currentBill.type==='收入'" class="space-y-2">
                                    <div class="text-lg my-auto">
                                        收入账户
                                    </div>
                                    <div class="h-10 flex items-center">
                                        <n-select v-if="editing" v-model:value="currentBill.incomeAsset as any"
                                                  class="w-1/2"
                                                  size="large" v-bind:options="assetList"
                                                  v-bind:render-label="assetSelectorRender as any">
                                        </n-select>
                                        <div v-if="!editing" class="flex space-x-2 items-center">
                                            <Icon class="h-5 w-5 text-primary"
                                                  v-bind:icon="assetList.find(item => item.assetName === currentBill.incomeAsset)?.svg as any">
                                            </Icon>
                                            <div class="text-15px">
                                                {{ currentBill.incomeAsset }}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div v-if="currentBill.type==='转账'" class="space-x-2 flex">
                                    <div class="w-1/2 space-y-2">
                                        <div class="text-lg my-auto">
                                            转出账户
                                        </div>
                                        <div class="h-10 flex items-center">
                                            <n-select v-if="editing" v-model:value="currentBill.outAsset as any"
                                                      size="large"
                                                      v-bind:options="assetList" v-bind:render-label="assetSelectorRender as any">
                                            </n-select>
                                            <div v-if="!editing" class="flex space-x-2 items-center">
                                                <Icon class="h-5 w-5 text-primary"
                                                      v-bind:icon="assetList.find(item => item.assetName === currentBill.outAsset)?.svg as any">
                                                </Icon>
                                                <div class="text-15px">
                                                    {{ currentBill.outAsset }}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="w-1/2 space-y-2">
                                        <div class="text-lg my-auto">
                                            转入账户
                                        </div>
                                        <div class="h-10 flex items-center">
                                            <n-select v-if="editing" v-model:value="currentBill.inAsset as any"
                                                      size="large"
                                                      v-bind:options="assetList" v-bind:render-label="assetSelectorRender as any">
                                            </n-select>
                                            <div v-if="!editing" class="flex space-x-2 items-center">
                                                <Icon class="h-5 w-5 text-primary"
                                                      v-bind:icon="assetList.find(item => item.assetName === currentBill.inAsset)?.svg as any">
                                                </Icon>
                                                <div class="text-15px">
                                                    {{ currentBill.inAsset }}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div v-if="currentBill.type==='退款'" class="space-y-2">
                                    <div class="text-lg my-auto">
                                        退款账户
                                    </div>
                                    <div class="h-10 flex items-center">
                                        <n-select v-if="editing" v-model:value="currentBill.refundAsset as any"
                                                  class="w-1/2"
                                                  size="large" v-bind:options="assetList"
                                                  v-bind:render-label="assetSelectorRender as any">
                                        </n-select>
                                        <div v-if="!editing" class="flex space-x-2 items-center">
                                            <Icon class="h-5 w-5 text-primary"
                                                  v-bind:icon="assetList.find(item => item.assetName === currentBill.refundAsset)?.svg as any">
                                            </Icon>
                                            <div class="text-15px">
                                                {{ currentBill.refundAsset }}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div v-if="currentBill.type==='转账'" class="space-y-2">
                                    <div class="text-lg my-auto">
                                        手续费
                                    </div>
                                    <div class="h-10 flex items-center">
                                        <n-input-number v-if="editing" v-model:value="currentBill.transferFee as any">
                                            <template #prefix>
                                                ￥
                                            </template>
                                        </n-input-number>
                                        <div v-if="!editing" class="text-15px">
                                            ￥{{ currentBill.transferFee }}
                                        </div>
                                    </div>
                                </div>
                                <div class="space-y-2">
                                    <div class="text-lg my-auto">
                                        时间
                                    </div>
                                    <div class="h-10 flex items-center">
                                        <n-date-picker v-if="editing" v-model:value="editTime" :input-readonly="true"
                                                       :time-picker-props="timePickerProps" :update-value-on-close="true"
                                                       class="w-1/2"
                                                       type="datetime">
                                        </n-date-picker>
                                        <div v-if="!editing" class="text-15px">
                                            {{ currentBill.billTime }}
                                        </div>
                                    </div>
                                </div>
                                <div class="space-y-2">
                                    <div class="text-lg my-auto">
                                        备注
                                    </div>
                                    <div class="h-10 flex items-center">
                                        <n-input v-if="editing" v-model:value="currentBill.remark" class="w-1/2"
                                                 size="large">
                                        </n-input>
                                        <div v-if="!editing" class="text-15px">
                                            {{ currentBill.remark }}
                                        </div>
                                    </div>
                                </div>
                                <div class="space-y-2">
                                    <div class="text-lg my-auto">
                                        图片
                                    </div>
                                    <div>
                                        <div v-if="editing" class="flex space-x-2">
                                            <img v-if="imageSrc !== EMPTY_IMAGE_SRC"
                                                 alt="图片"
                                                 class="rounded-xl h-24" v-bind:src="imageSrc"
                                                 v-on:error="handleImageLoadError" v-on:load="handleImageLoadSuccess"/>
                                            <n-button v-if="imageSrc !== EMPTY_IMAGE_SRC" class="my-auto"
                                                      v-on:click="deleteImage">
                                                <template #default>
                                                    删除图片
                                                </template>
                                            </n-button>
                                            <n-upload :custom-request="customRequest" class="w-1/5"
                                                      list-type="image-card" :max="1" v-on:change="changePicture"
                                                      v-on:before-upload="beforeUpload">
                                                <template #default>
                                                    <div>
                                                        图片
                                                    </div>
                                                </template>
                                            </n-upload>
                                            <div class="my-auto">
                                                {{ fileName }}
                                            </div>
                                        </div>
                                        <img v-if="imageSrc !== EMPTY_IMAGE_SRC && !editing"
                                             alt="图片"
                                             class="rounded-xl" v-bind:src="imageSrc"
                                             v-on:error="handleImageLoadError" v-on:load="handleImageLoadSuccess"/>
                                        <div v-if="imageSrc !== EMPTY_IMAGE_SRC && !editing && (imageCompatibilityWarning || imageLoadFailed)"
                                             class="mt-2 text-xs text-orange-500">
                                            {{ imageLoadFailed ? "当前浏览器无法预览此图片格式（可能是 HEIC/HEIF），可改用 Safari 打开。" : imageCompatibilityWarning }}
                                        </div>
                                        <n-empty v-if="imageSrc === EMPTY_IMAGE_SRC && !editing" class="w-1/2">
                                            <template #default>
                                                什么也没有
                                            </template>
                                        </n-empty>
                                    </div>
                                </div>
                            </div>
                        </n-scrollbar>
                    </template>
                </n-card>
            </div>
        </div>
        <!-- 手机端：账单详情弹窗 -->
        <n-modal
            v-model:show="showBillDetailModal"
            :mask-closable="true"
            class="xl:hidden"
        >
            <n-card
                v-if="currentBill.id != undefined"
                :bordered="false"
                class="w-[92vw] max-w-[92vw] rounded-xl lg:w-1/2"
            >
                <template #header>
                    <div class="flex space-x-3">
                        <div>账单详情</div>
                        <Icon
                            class="h-5 w-5 my-auto"
                            v-bind:class="iconColorMap[currentBill.type]"
                            v-bind:icon="iconMap[currentBill.type]"
                        />
                    </div>
                </template>

                <template #header-extra>
                    <!-- 手机：两行两列；电脑：一行 -->
                    <div
                        class="grid grid-cols-2 gap-2 xl:flex xl:gap-0 xl:space-x-3 xl:items-center lg:flex lg:gap-0 lg:space-x-3 lg:items-center">
                        <!-- 编辑 -->
                        <div
                            class="flex items-center justify-center gap-1 rounded-lg px-2 py-1 cursor-pointer select-none border border-gray-200 text-sm xl:text-base xl:border-0"
                            v-bind:class="{'text-primary': mouseOnEdit}"
                            v-on:click="editingChange"
                            v-on:mouseenter="mouseOnEditChange(true)"
                            v-on:mouseleave="mouseOnEditChange(false)"
                        >
                            <Icon class="h-5 w-5" icon="fluent:edit-48-regular"/>
                            <div>{{ editing ? "完成" : "编辑" }}</div>
                        </div>

                        <!-- 退款（仅支出） -->
                        <div
                            v-if="currentBill.type === '支出'"
                            class="flex items-center justify-center gap-1 rounded-lg px-2 py-1 cursor-pointer select-none border border-gray-200 text-sm xl:text-base xl:border-0"
                            v-bind:class="{'text-primary': mouseOnRefund}"
                            v-on:click="refundChange"
                            v-on:mouseenter="mouseOnRefundChange(true)"
                            v-on:mouseleave="mouseOnRefundChange(false)"
                        >
                            <Icon class="h-5 w-5" icon="ph:arrows-down-up"/>
                            <div>退款</div>

                            <!-- 建议：弹窗宽度手机大一点 -->
                            <n-modal v-model:show="showRefundModal" class="w-5/6 xl:w-1/7 block xl:hidden">
                                <n-card>
                                    <template #header>
                                        <div class="s-title s-underline text-lg">提示</div>
                                    </template>
                                    <template #default>
                                        <div class="text-center space-y-8">
                                            <div class="mx-auto text-lg">确认退款？</div>
                                            <div class="space-y-2 text-left">
                                                <div class="text-sm text-gray-500">退款金额</div>
                                                <n-input-number v-model:value="refundAmount" :min="0"
                                                                :precision="2" class="w-full">
                                                    <template #prefix>￥</template>
                                                </n-input-number>
                                            </div>
                                            <div class="flex space-x-2 justify-end">
                                                <n-button v-on:click="refundBill">确定</n-button>
                                                <n-button v-on:click="showRefundModal = false">取消</n-button>
                                            </div>
                                        </div>
                                    </template>
                                </n-card>
                            </n-modal>
                        </div>

                        <!-- 删除 -->
                        <div
                            class="flex items-center justify-center gap-1 rounded-lg px-2 py-1 cursor-pointer select-none border border-gray-200 text-sm xl:text-base xl:border-0"
                            v-bind:class="{'text-primary': mouseOnDelete}"
                            v-on:click="deleteChange"
                            v-on:mouseenter="mouseOnDeleteChange(true)"
                            v-on:mouseleave="mouseOnDeleteChange(false)"
                        >
                            <Icon class="h-5 w-5" icon="material-symbols:delete-forever"/>
                            <div>删除</div>

                            <n-modal v-model:show="showDeleteModal" class="w-5/6 xl:w-1/7">
                                <n-card>
                                    <template #header>
                                        <div class="s-title s-underline text-lg">提示</div>
                                    </template>
                                    <template #default>
                                        <div class="text-center space-y-8">
                                            <div class="mx-auto text-lg">确认删除？</div>
                                            <div class="flex space-x-2 justify-end">
                                                <n-button v-on:click="deleteBill">确定</n-button>
                                                <n-button v-on:click="showDeleteModal=false">取消</n-button>
                                            </div>
                                        </div>
                                    </template>
                                </n-card>
                            </n-modal>
                        </div>

                        <!-- 关闭：如果没有“退款”，让它占满一行更好看 -->
                        <div
                            class="flex items-center justify-center gap-1 rounded-lg px-2 py-1 cursor-pointer select-none border border-gray-200 text-sm xl:text-base xl:border-0"
                            v-bind:class="currentBill.type === '支出' ? '' : 'col-span-2 xl:col-span-1'"
                            v-on:click="closeBillDetailModal"
                        >
                            <Icon class="h-5 w-5" icon="material-symbols:close"/>
                            <div>关闭</div>
                        </div>
                    </div>
                </template>


                <!-- 内容区：把你桌面端 n-card 里 <template #default> 的 n-scrollbar 整段复制过来即可 -->
                <template #default>
                    <n-scrollbar class="max-h-[70vh]">
                        <!-- 复制你原来的详情内容（金额/类别/账户/时间/备注/图片 ...） -->
                        <!-- 注意：你原来是 xl:max-h-164，这里改成 max-h-[70vh] 更适合手机 -->
                        <div class="space-y-3">
                            <div class="space-y-2">
                                <div class="text-lg my-auto">
                                    金额
                                </div>
                                <div class="h-10 flex items-center">
                                    <n-input-number v-if="editing" v-model:value="currentBill.amount">
                                        <template #prefix>
                                            ￥
                                        </template>
                                    </n-input-number>
                                    <div v-if="!editing" class="text-15px">
                                        ￥{{ currentBill.amount }}
                                    </div>
                                </div>
                            </div>
                            <div v-if="currentBill.type === '支出' || currentBill.type === '收入'"
                                 class="space-y-2">
                                <div class="text-lg my-auto">
                                    类别
                                </div>
                                <div class="h-10 flex items-center">
                                    <n-select v-if="editing" v-model:value="currentBill.billCategory as any"
                                              class="w-1/2"
                                              size="large" v-bind:options="billCategoryList"
                                              v-bind:render-label="cateSelectorRender as any">
                                    </n-select>
                                    <div v-if="!editing" class="flex space-x-2 items-center">
                                        <Icon class="h-5 w-5 text-primary"
                                              v-bind:icon="billCategoryList.find(item => item.billCategoryName === currentBill.billCategory)?.svg as any">
                                        </Icon>
                                        <div class="text-15px">
                                            {{ currentBill.billCategory }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div v-if="currentBill.type==='支出'" class="space-y-2">
                                <div class="text-lg my-auto">
                                    支出账户
                                </div>
                                <div class="h-10 flex items-center">
                                    <n-select v-if="editing" v-model:value="currentBill.payAsset as any"
                                              class="w-1/2"
                                              size="large" v-bind:options="assetList"
                                              v-bind:render-label="assetSelectorRender as any">
                                    </n-select>
                                    <div v-if="!editing" class="flex space-x-2 items-center">
                                        <Icon class="h-5 w-5 text-primary"
                                              v-bind:icon="assetList.find(item => item.assetName === currentBill.payAsset)?.svg as any">
                                        </Icon>
                                        <div class="text-15px">
                                            {{ currentBill.payAsset }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div v-if="currentBill.type==='收入'" class="space-y-2">
                                <div class="text-lg my-auto">
                                    收入账户
                                </div>
                                <div class="h-10 flex items-center">
                                    <n-select v-if="editing" v-model:value="currentBill.incomeAsset as any"
                                              class="w-1/2"
                                              size="large" v-bind:options="assetList"
                                              v-bind:render-label="assetSelectorRender as any">
                                    </n-select>
                                    <div v-if="!editing" class="flex space-x-2 items-center">
                                        <Icon class="h-5 w-5 text-primary"
                                              v-bind:icon="assetList.find(item => item.assetName === currentBill.incomeAsset)?.svg as any">
                                        </Icon>
                                        <div class="text-15px">
                                            {{ currentBill.incomeAsset }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div v-if="currentBill.type==='转账'" class="space-x-2 flex">
                                <div class="w-1/2 space-y-2">
                                    <div class="text-lg my-auto">
                                        转出账户
                                    </div>
                                    <div class="h-10 flex items-center">
                                        <n-select v-if="editing" v-model:value="currentBill.outAsset as any"
                                                  size="large"
                                                  v-bind:options="assetList" v-bind:render-label="assetSelectorRender as any">
                                        </n-select>
                                        <div v-if="!editing" class="flex space-x-2 items-center">
                                            <Icon class="h-5 w-5 text-primary"
                                                  v-bind:icon="assetList.find(item => item.assetName === currentBill.outAsset)?.svg as any">
                                            </Icon>
                                            <div class="text-15px">
                                                {{ currentBill.outAsset }}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="w-1/2 space-y-2">
                                    <div class="text-lg my-auto">
                                        转入账户
                                    </div>
                                    <div class="h-10 flex items-center">
                                        <n-select v-if="editing" v-model:value="currentBill.inAsset as any"
                                                  size="large"
                                                  v-bind:options="assetList" v-bind:render-label="assetSelectorRender as any">
                                        </n-select>
                                        <div v-if="!editing" class="flex space-x-2 items-center">
                                            <Icon class="h-5 w-5 text-primary"
                                                  v-bind:icon="assetList.find(item => item.assetName === currentBill.inAsset)?.svg as any">
                                            </Icon>
                                            <div class="text-15px">
                                                {{ currentBill.inAsset }}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div v-if="currentBill.type==='退款'" class="space-y-2">
                                <div class="text-lg my-auto">
                                    退款账户
                                </div>
                                <div class="h-10 flex items-center">
                                    <n-select v-if="editing" v-model:value="currentBill.refundAsset as any"
                                              class="w-1/2"
                                              size="large" v-bind:options="assetList"
                                              v-bind:render-label="assetSelectorRender as any">
                                    </n-select>
                                    <div v-if="!editing" class="flex space-x-2 items-center">
                                        <Icon class="h-5 w-5 text-primary"
                                              v-bind:icon="assetList.find(item => item.assetName === currentBill.refundAsset)?.svg as any">
                                        </Icon>
                                        <div class="text-15px">
                                            {{ currentBill.refundAsset }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div v-if="currentBill.type==='转账'" class="space-y-2">
                                <div class="text-lg my-auto">
                                    手续费
                                </div>
                                <div class="h-10 flex items-center">
                                    <n-input-number v-if="editing" v-model:value="currentBill.transferFee as any">
                                        <template #prefix>
                                            ￥
                                        </template>
                                    </n-input-number>
                                    <div v-if="!editing" class="text-15px">
                                        ￥{{ currentBill.transferFee }}
                                    </div>
                                </div>
                            </div>
                            <div class="space-y-2">
                                <div class="text-lg my-auto">
                                    时间
                                </div>
                                <div class="h-10 flex items-center">
                                    <n-date-picker v-if="editing" v-model:value="editTime" :input-readonly="true"
                                                   :time-picker-props="timePickerProps" :update-value-on-close="true"
                                                   class="w-1/2"
                                                   type="datetime">
                                    </n-date-picker>
                                    <div v-if="!editing" class="text-15px">
                                        {{ currentBill.billTime }}
                                    </div>
                                </div>
                            </div>
                            <div class="space-y-2">
                                <div class="text-lg my-auto">
                                    备注
                                </div>
                                <div class="h-10 flex items-center">
                                    <n-input v-if="editing" v-model:value="currentBill.remark" class="w-1/2"
                                             size="large">
                                    </n-input>
                                    <div v-if="!editing" class="text-15px">
                                        {{ currentBill.remark }}
                                    </div>
                                </div>
                            </div>
                            <div class="space-y-2">
                                <div class="text-lg my-auto">
                                    图片
                                </div>
                                <div>
                                    <div v-if="editing" class="flex space-x-2">
                                        <img v-if="imageSrc !== EMPTY_IMAGE_SRC"
                                             alt="图片"
                                             class="rounded-xl h-24" v-bind:src="imageSrc"
                                             v-on:error="handleImageLoadError" v-on:load="handleImageLoadSuccess"/>
                                        <n-button v-if="imageSrc !== EMPTY_IMAGE_SRC" class="my-auto"
                                                  v-on:click="deleteImage">
                                            <template #default>
                                                删除图片
                                            </template>
                                        </n-button>
                                        <n-upload :custom-request="customRequest" class="w-1/5"
                                                  list-type="image-card" :max="1" v-on:change="changePicture"
                                                  v-on:before-upload="beforeUpload">
                                            <template #default>
                                                <div>
                                                    图片
                                                </div>
                                            </template>
                                        </n-upload>
                                        <div class="my-auto">
                                            {{ fileName }}
                                        </div>
                                    </div>
                                    <img v-if="imageSrc !== EMPTY_IMAGE_SRC && !editing"
                                         alt="图片"
                                         class="rounded-xl" v-bind:src="imageSrc"
                                         v-on:error="handleImageLoadError" v-on:load="handleImageLoadSuccess"/>
                                    <div v-if="imageSrc !== EMPTY_IMAGE_SRC && !editing && (imageCompatibilityWarning || imageLoadFailed)"
                                         class="mt-2 text-xs text-orange-500">
                                        {{ imageLoadFailed ? "当前浏览器无法预览此图片格式（可能是 HEIC/HEIF），可改用 Safari 打开。" : imageCompatibilityWarning }}
                                    </div>
                                    <n-empty v-if="imageSrc === EMPTY_IMAGE_SRC && !editing" class="w-1/2">
                                        <template #default>
                                            什么也没有
                                        </template>
                                    </n-empty>
                                </div>
                            </div>
                        </div>
                    </n-scrollbar>
                </template>
            </n-card>
        </n-modal>
    </div>
</template>
<script lang="ts" setup>
import {computed, ComputedRef, h, onBeforeUnmount, onMounted, Ref, ref, VNodeChild, watch} from "vue";
import {
    addBillApi,
    changeBillApi,
    deleteBillApi,
    deleteBillImageApi,
    getAllAsset,
    getAllBillTimeApi,
    getBalanceMonthApi,
    getBillCategoryApi,
    getBillImageApi,
    getDayStatisticTimeApi,
    getIncomeMonthApi,
    getPayMonthApi
} from "@/apis";
import {useStore} from "@/stores/store";
import {dateToString, intToString, now, stringToInt} from "@/utils/dateComputer";
import {
    Asset,
    AssetGetAllAssetResponse,
    BillAllBillTimeResponse,
    BillBillResponse,
    BillCategory,
    BillDayStatisticTimeResponse,
    BookBalanceMonthResponse,
    BookIncomeMonthResponse,
    BookPayMonthResponse
} from "@/interface";
import {DayBillList} from "./components";
import {MonthStatistic} from "@/views/components";
import {Icon} from "@iconify/vue";
// calendar-date-locale.ts
import {dateZhCN, type NDateLocale, TimePickerProps, UploadCustomRequestOptions, UploadFileInfo} from "naive-ui";

const dateZhCNSingleWeekday: NDateLocale = {
    ...dateZhCN,
    locale: {
        ...dateZhCN.locale,
        localize: {
            ...dateZhCN.locale.localize,
            // 关键：把 'EEE' 需要的 weekday，强制用 narrow 输出
            day: (n: any, options: any) =>
                dateZhCN.locale.localize?.day(n, {...options, width: 'narrow'})
        }
    } as any
}
let timePickerProps: TimePickerProps = {inputReadonly: true};
const store = useStore();
const showBillDetailModal = ref(false);

const isDesktop = ref(false);
let mql: MediaQueryList | null = null;

function updateIsDesktop() {
    // Tailwind xl 默认 1280px
    isDesktop.value = window.matchMedia('(min-width: 1280px)').matches;
}

function closeBillDetailModal() {
    showBillDetailModal.value = false;
    editing.value = false; // 关弹窗顺便退出编辑态，避免下次打开还是编辑态
}

let bookId = ref<number>(0);

interface DayStatisticTime extends BillDayStatisticTimeResponse {
}

let dayStatisticTime: Ref<DayStatisticTime> = ref({payStatistic: [], incomeStatistic: []});

interface BillCategoryShow extends BillCategory {
    [key: string]: any;
    value: string;
    label: string;
}

interface AssetShow extends Asset {
    value: string;
    label: string;
}

let billCategoryList: Ref<Array<BillCategoryShow>> = ref([]);
let assetList: Ref<Array<AssetShow>> = ref([]);
const iconMap = {
    "支出": "material-symbols:arrow-upward",
    "收入": "material-symbols:arrow-downward",
    "转账": "ph:arrows-clockwise",
    "退款": "ph:arrows-down-up"
};
const iconColorMap = {
    "支出": "text-red-500",
    "收入": "text-green-500",
    "转账": "text-blue-500",
    "退款": "text-blue-500"
};

function getData(refresh: boolean = true) {
    getBalanceMonth();
    getIncomeMonth();
    getPayMonth();
    getAllBillMonth();
    getDayStatisticTimeApi({
        bookId: bookId.value,
        startTime: startTime.value,
        endTime: endTime.value
    }).then((response: BillDayStatisticTimeResponse) => {
        dayStatisticTime.value = response;
    }).catch(() => {
    });
    getBillCategoryApi({
        bookId: bookId.value
    }).then((response: any) => {
        billCategoryList.value = response;
        billCategoryList.value.forEach((item: BillCategoryShow) => {
            item.value = item.billCategoryName;
            item.label = item.billCategoryName;
        });
    }).catch(() => {
    });
    getAllAsset().then((response: any) => {
        assetList.value = response.assetList;
        assetList.value.forEach((item: AssetShow) => {
            item.value = item.assetName;
            item.label = item.assetName;
        });
    }).catch(() => {
    });
    if (refresh) {
        store.currentBill = Object();
        imageSrc.value = EMPTY_IMAGE_SRC;
        imageLoadFailed.value = false;
        imageCompatibilityWarning.value = "";
        editTime.value = 0;
    }
    editing.value = false;
}

onMounted(() => {
    bookId.value = store.bookId;
    if (bookId.value !== 0) {
        getData();
    }
    // 新增：判定是否桌面 + 监听尺寸变化
    updateIsDesktop();
    mql = window.matchMedia('(min-width: 1280px)');
    mql.addEventListener('change', updateIsDesktop);
});
onBeforeUnmount(() => {
    mql?.removeEventListener('change', updateIsDesktop);
});
watch(() => store.bookId, (newValue: number) => {
    bookId.value = newValue;
    if (bookId.value !== 0) {
        getData();
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

function fmtMobile(amount: number): string {
    const x = Math.abs(amount);
    if (x >= 10000) return (Math.round((amount / 10000) * 10) / 10) + "万";
    return String(Math.round(amount)); // 手机别带小数，长度立刻下降
}


function fmtDesktop(amount: number): string {
    const x = Math.abs(amount);
    if (x >= 10000) return (Math.round((amount / 10000) * 10) / 10) + "万";
    // 桌面保留两位小数
    return amount.toFixed(2);
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
    // 没有数据就不弹（同时 popover 也会 disabled）
    if (pay === 0 && income === 0) {
        activePopoverKey.value = null;
        return;
    }
    activePopoverKey.value = key;
}

function closeDayPopover() {
    activePopoverKey.value = null;
}

function calendarContent(year: number, month: number, day: number): string {
    const key = dateToString(new Date(year, month - 1, day));

    const pay = dayStatisticTime.value.payStatistic.find(
        (i: { day: string; amount: number }) => i.day === key
    )?.amount ?? 0;

    const income = dayStatisticTime.value.incomeStatistic.find(
        (i: { day: string; amount: number }) => i.day === key
    )?.amount ?? 0;

    if (pay === 0 && income === 0) return "";

    // 手机：只显示点（绝不显示金额文本）
    // 桌面：显示金额文本（必要时可 truncate）
    return `
    <div class="mt-1">
      <div class="flex items-center gap-1 xl:hidden">
        ${pay > 0 ? `<span class="h-1.5 w-1.5 rounded-full bg-red-500"></span>` : ``}
        ${income > 0 ? `<span class="h-1.5 w-1.5 rounded-full bg-green-500"></span>` : ``}
      </div>

      <div class="hidden xl:block space-y-0.5">
        ${pay > 0 ? `<div class="text-red-500 text-xs whitespace-nowrap">${"-" + fmtDesktop(pay)}</div>` : ``}
        ${income > 0 ? `<div class="text-green-500 text-xs whitespace-nowrap">${"+" + fmtDesktop(income)}</div>` : ``}
      </div>
    </div>
  `;
}

function changeDay(timestamp: number, info: { year: number, month: number, date: number }): void {
    let newStartTime = String(info.year) + "-" + String(info.month) + "-" + String(info.date) + " 00:00:00";
    let newEndTime = String(info.year) + "-" + String(info.month) + "-" + String(info.date) + " 23:59:59";
    getAllBillTimeApi({
        bookId: bookId.value,
        startTime: newStartTime,
        endTime: newEndTime
    }).then((response: BillAllBillTimeResponse) => {
        billList.value = response.billList;
    });
}

function changeMonth(info: { year: number, month: number }): void {
    activeYear.value = info.year;
    activeMonth.value = info.month - 1;
    activePopoverKey.value = null;
    getData();
}

interface BillShow extends BillBillResponse {
}

let billList: Ref<Array<BillShow>> = ref([]);

function getAllBillMonth(): void {
    getAllBillTimeApi({
        bookId: bookId.value,
        startTime: startTime.value,
        endTime: endTime.value
    }).then((response: BillAllBillTimeResponse) => {
        billList.value = response.billList;
    });
}

let billDayList: ComputedRef<Map<string, Array<BillShow>>> = computed(() => {
    let dayMap: Map<string, Array<BillShow>> = new Map();
    billList.value.forEach((item: BillShow) => {
        let day: string = item.billTime.substring(0, 10);
        if (dayMap.has(day)) {
            dayMap.get(day)?.push(item);
        } else {
            dayMap.set(day, [item]);
        }
    });
    return dayMap;
});
let currentBill: Ref<BillShow> = ref({} as BillShow);
const EMPTY_IMAGE_SRC = "data:image;base64,null";
let imageSrc: Ref<string> = ref(EMPTY_IMAGE_SRC);
let imageLoadFailed: Ref<boolean> = ref(false);
let imageCompatibilityWarning: Ref<string> = ref("");

interface BillImageResponse {
    contentType?: string;
    image?: string;
}

function isHeifLike(contentType: string): boolean {
    const lower = contentType.toLowerCase();
    return lower.includes("image/heic") || lower.includes("image/heif");
}

function isSafariBrowser(): boolean {
    const ua = navigator.userAgent.toLowerCase();
    return ua.includes("safari") && !ua.includes("chrome") && !ua.includes("android") && !ua.includes("edg");
}

function updateImageCompatibilityWarning(contentType: string): void {
    imageCompatibilityWarning.value = "";
    if (isHeifLike(contentType) && !isSafariBrowser()) {
        imageCompatibilityWarning.value = "当前浏览器可能不支持 HEIC/HEIF 预览，可改用 Safari 或上传 JPEG/AVIF。";
    }
}

function setImageFromResponse(response: any): void {
    imageLoadFailed.value = false;
    imageCompatibilityWarning.value = "";
    if (!response) {
        imageSrc.value = EMPTY_IMAGE_SRC;
        return;
    }
    // backward compatibility: older API only returned base64 string
    if (typeof response === "string") {
        imageSrc.value = "data:image;base64," + response;
        return;
    }
    const imageResponse: BillImageResponse = response as BillImageResponse;
    if (!imageResponse.image) {
        imageSrc.value = EMPTY_IMAGE_SRC;
        return;
    }
    const contentType = imageResponse.contentType ?? "";
    updateImageCompatibilityWarning(contentType);
    const mime = contentType.startsWith("image/") ? contentType : "image";
    imageSrc.value = `data:${mime};base64,${imageResponse.image}`;
}

function handleImageLoadSuccess(): void {
    imageLoadFailed.value = false;
}

function handleImageLoadError(): void {
    imageLoadFailed.value = true;
}

watch(() => store.currentBill, (newValue: BillShow) => {
    currentBill.value.amount = newValue.amount;
    currentBill.value.billTime = newValue.billTime;
    currentBill.value.payAsset = newValue.payAsset;
    currentBill.value.billCategory = newValue.billCategory;
    currentBill.value.incomeAsset = newValue.incomeAsset;
    currentBill.value.inAsset = newValue.inAsset;
    currentBill.value.outAsset = newValue.outAsset;
    currentBill.value.transferFee = newValue.transferFee;
    currentBill.value.payBillId = newValue.payBillId;
    currentBill.value.refundAsset = newValue.refundAsset;
    currentBill.value.id = newValue.id;
    currentBill.value.remark = newValue.remark;
    currentBill.value.refunded = newValue.refunded;
    currentBill.value.type = newValue.type;
    editing.value = false;
    editTime.value = stringToInt(newValue.billTime);
    imageSrc.value = EMPTY_IMAGE_SRC;
    if (!isNaN(currentBill.value.id)) {
        getBillImageApi({billId: currentBill.value.id, type: currentBill.value.type}).then((response: any) => {
            setImageFromResponse(response);
        });
    }
    if (!isDesktop.value && newValue?.id != undefined && !isNaN(newValue.id as any)) {
        showBillDetailModal.value = true;
    }
});
watch(isDesktop, (v) => {
    if (v) showBillDetailModal.value = false;
});
let mouseOnEdit: Ref<boolean> = ref(false);
let mouseOnRefund: Ref<boolean> = ref(false);

function mouseOnEditChange(value: boolean): void {
    mouseOnEdit.value = value;
}

function mouseOnRefundChange(value: boolean): void {
    mouseOnRefund.value = value;
}

let showRefundModal: Ref<boolean> = ref(false);
let refundAmount: Ref<number | null> = ref(null);

function refundChange() {
    showRefundModal.value = !showRefundModal.value;
    if (showRefundModal.value) {
        refundAmount.value = currentBill.value.amount;
    }
}

function refundBill() {
    if (currentBill.value.type !== "支出") {
        return;
    }
    if (refundAmount.value === null || refundAmount.value <= 0) {
        window.$message.warning("请输入有效退款金额");
        return;
    }
    const confirmedRefundAmount = refundAmount.value;
    getAllAsset().then((response: AssetGetAllAssetResponse) => {
        let inAsset: number = 0;
        response.assetList.forEach((item) => {
            if (item.assetName === currentBill.value.payAsset) {
                inAsset = item.id;
            }
        });
        getBillCategoryApi({bookId: bookId.value}).then((subResponse: any) => {
            let billCategoryId: number = 0;
            subResponse.forEach((item: { billCategoryName: string | undefined; id: number; }) => {
                if (item.billCategoryName === currentBill.value.billCategory) {
                    billCategoryId = item.id;
                }
            });
            let formData: FormData = new FormData();
            formData.append("bookId", bookId.value.toString());
            formData.append("inAssetId", inAsset.toString());
            formData.append("payBillId", currentBill.value.id.toString());
            formData.append("type", "退款");
            formData.append("amount", confirmedRefundAmount.toString());
            formData.append("time", dateToString(now()));
            formData.append("remark", "退款");
            formData.append("billCategoryId", billCategoryId.toString());
            addBillApi(formData).then(() => {
                window.$message.success("退款成功");
                getData();
                refundChange();
            }).catch(() => {
            });
        }).catch(() => {
        });
    }).catch(() => {
    });

}

let editing: Ref<boolean> = ref(false);

function editingChange(): void {
    if (editing.value) {
        let changed: boolean = false;
        let formData: FormData = new FormData();
        formData.append("id", currentBill.value.id as any);
        switch (currentBill.value.type) {
            case "支出":
                if (currentBill.value.payAsset !== store.currentBill.payAsset) {
                    console.log(currentBill.value.payAsset);
                    console.log(assetList.value);
                    formData.append("outAssetId", assetList.value.find((item: Asset) => item.assetName === currentBill.value.payAsset)?.id as any);
                    changed = true;
                }
                if (currentBill.value.billCategory !== store.currentBill.billCategory) {
                    formData.append("billCategoryId",
                        billCategoryList.value.find((item: BillCategory) => item.billCategoryName === currentBill.value.billCategory)?.id as any);
                    changed = true;
                }
                if (currentBill.value.amount !== store.currentBill.amount) {
                    formData.append("amount", currentBill.value.amount as any);
                    changed = true;
                }
                if (intToString(editTime.value) !== store.currentBill.billTime) {
                    formData.append("billTime", intToString(editTime.value) as any);
                    changed = true;
                }
                if (currentBill.value.remark !== store.currentBill.remark) {
                    formData.append("remark", currentBill.value.remark as any);
                    changed = true;
                }
                if (picture !== undefined) {
                    formData.append("file", picture as File);
                    changed = true;
                }
                formData.append("type", "支出");
                break;
            case "收入":
                if (currentBill.value.incomeAsset !== store.currentBill.incomeAsset) {
                    formData.append("inAssetId", assetList.value.find((item: Asset) => item.assetName === currentBill.value.incomeAsset)?.id as any);
                    changed = true;
                }
                if (currentBill.value.billCategory !== store.currentBill.billCategory) {
                    formData.append("billCategoryId",
                        billCategoryList.value.find((item: BillCategory) => item.billCategoryName === currentBill.value.billCategory)?.id as any);
                    changed = true;
                }
                if (currentBill.value.amount !== store.currentBill.amount) {
                    formData.append("amount", currentBill.value.amount as any);
                    changed = true;
                }
                if (intToString(editTime.value) !== store.currentBill.billTime) {
                    formData.append("billTime", intToString(editTime.value) as any);
                    changed = true;
                }
                if (currentBill.value.remark !== store.currentBill.remark) {
                    formData.append("remark", currentBill.value.remark as any);
                    changed = true;
                }
                if (picture !== undefined) {
                    formData.append("file", picture as File);
                    changed = true;
                }
                formData.append("type", "收入");
                break;
            case "转账":
                if (currentBill.value.inAsset !== store.currentBill.incomeAsset) {
                    formData.append("inAssetId", assetList.value.find((item: Asset) => item.assetName === currentBill.value.inAsset)?.id as any);
                    changed = true;
                }
                if (currentBill.value.outAsset !== store.currentBill.payAsset) {
                    formData.append("outAssetId", assetList.value.find((item: Asset) => item.assetName === currentBill.value.outAsset)?.id as any);
                    changed = true;
                }
                if (currentBill.value.amount !== store.currentBill.amount) {
                    formData.append("amount", currentBill.value.amount as any);
                    changed = true;
                }
                if (currentBill.value.transferFee !== store.currentBill.transferFee) {
                    formData.append("transferFee", currentBill.value.transferFee as any);
                    changed = true;
                }
                if (intToString(editTime.value) !== store.currentBill.billTime) {
                    formData.append("billTime", intToString(editTime.value) as any);
                    changed = true;
                }
                if (currentBill.value.remark !== store.currentBill.remark) {
                    formData.append("remark", currentBill.value.remark as any);
                    changed = true;
                }
                if (picture !== undefined) {
                    formData.append("file", picture as File);
                    changed = true;
                }
                formData.append("type", "转账");
                break;
            case "退款":
                if (currentBill.value.payBillId !== store.currentBill.payBillId) {
                    formData.append("payBillId", currentBill.value.payBillId as any);
                    changed = true;
                }
                if (currentBill.value.refundAsset !== store.currentBill.refundAsset) {
                    formData.append("inAssetId", assetList.value.find((item: Asset) => item.assetName === currentBill.value.refundAsset)?.id as any);
                    changed = true;
                }
                if (currentBill.value.amount !== store.currentBill.amount) {
                    formData.append("amount", currentBill.value.amount as any);
                    changed = true;
                }
                if (intToString(editTime.value) !== store.currentBill.billTime) {
                    formData.append("billTime", intToString(editTime.value) as any);
                    changed = true;
                }
                if (currentBill.value.remark !== store.currentBill.remark) {
                    formData.append("remark", currentBill.value.remark as any);
                    changed = true;
                }
                if (picture !== undefined) {
                    formData.append("file", picture as File);
                    changed = true;
                }
                formData.append("type", "退款");
                break;
        }
        if (changed) {
            changeBillApi(formData).then(() => {
                window.$message.success("修改成功");
                getData(false);
                getBillImageApi({
                    billId: currentBill.value.id,
                    type: currentBill.value.type
                }).then((response: any) => {
                    setImageFromResponse(response);
                });
            }).catch(() => {
            });
        }
    }
    editing.value = !editing.value;
}

function cateSelectorRender(option: BillCategoryShow): VNodeChild {
    return h(
        "div",
        {
            class: "flex space-x-2"
        },
        {
            default: () => [
                h(
                    Icon,
                    {
                        class: "h-5 w-5 text-primary",
                        icon: option.svg
                    }
                ),
                h(
                    "div",
                    {},
                    {
                        default: () => option.label
                    }
                )
            ]
        });
}

function assetSelectorRender(option: AssetShow): VNodeChild {
    return h(
        "div",
        {
            class: "flex space-x-2"
        },
        {
            default: () => [
                h(
                    Icon,
                    {
                        class: "h-5 w-5 text-primary",
                        icon: option.svg
                    }
                ),
                h(
                    "div",
                    {},
                    {
                        default: () => option.label
                    }
                )
            ]
        });
}

let editTime: Ref<number> = ref(0);
let picture: File | undefined;
let fileName: Ref<string> = ref("");
const customRequest = (file: UploadCustomRequestOptions) => {
    picture = file.file.file as File;
    fileName.value = file.file.name;
};

function changePicture() {
    fileName.value = "";
}

declare const window: Window & { $message: any; URL: any };

function beforeUpload(data: { file: UploadFileInfo, fileList: UploadFileInfo[] }) {
    const fileType = (data.file.file?.type ?? "").toLowerCase();
    if (!fileType.startsWith("image")) {
        window.$message.error("请上传图片");
        return false;
    }
    if (isHeifLike(fileType) && !isSafariBrowser()) {
        window.$message.warning("当前浏览器可能无法预览 HEIC/HEIF，建议上传 JPEG/AVIF 或使用 Safari 查看。");
    }
    return true;
}

let mouseOnDelete: Ref<boolean> = ref(false);

function mouseOnDeleteChange(value: boolean): void {
    mouseOnDelete.value = value;
}

let showDeleteModal: Ref<boolean> = ref(false);

function deleteChange() {
    showDeleteModal.value = !showDeleteModal.value;
}

function deleteBill() {
    deleteBillApi({
        id: currentBill.value.id,
        type: currentBill.value.type
    }).then(() => {
        window.$message.success("删除成功");
        getData();
        deleteChange();
    }).catch(() => {
    });
}

function deleteImage(): void {
    deleteBillImageApi({
        id: currentBill.value.id,
        type: currentBill.value.type
    }).then(() => {
        window.$message.success("删除成功");
        getData(false);
    }).catch(() => {
    });
}

function numberToCharMonth(numberMonth: number): string {
    let charList: Array<string> = ["一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"];
    return charList[numberMonth - 1] + "月";
}
</script>
<style scoped>
</style>
