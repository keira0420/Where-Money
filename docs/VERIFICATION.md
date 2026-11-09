# 本地验收记录与接口

验收日期：2026-10-19。源目录：`D:/Where-Money`。

## 已执行

- Java21 Maven打包；Spring Boot本地profile启动，`/actuator/health`返回UP。
- MySQL8.0.31独立数据目录、端口3307；Redis本机实例端口6380。
- 新版vue-tsc类型检查通过；Vite生产构建通过。仍有图表依赖导致的大体积chunk警告，不影响编译但可继续优化。
- HTTP冒烟脚本验证注册、登录、账户创建、预算、确认保存、重复提交不重复扣款、预算80%提醒、负数拒绝、越权拒绝及未登录拒绝。
- JUnit5集成测试使用真实MySQL/Redis，仅外部付费模型网关用Mock。覆盖JSON错误、编造账户、草稿不落库、幂等并发、不同账单并发、回滚、缓存提交后失效、预算、月报缓存及降级、收入/转账手续费/退款口径。
- 浏览器实际登录，检查首页统计、AI草稿编辑、账户/分类选择、确认保存及预算变化。

最终测试数量以`Where-Money-Backend/target/surefire-reports/*.txt`为准。不要把Mock模型测试当作真实大模型准确率评测。
本轮测试共19个：12个真实MySQL/Redis集成测试与7个单元测试，0失败、0错误。

## 尚未完成的外部验证

1. 真实通义千问接口返回HTTP400、错误类型`Arrearage`，表示账号服务额度不足。真实解析与真实AI月报尚不能宣称跑通。需要用户提供本地有效配置或恢复额度后运行`smoke.ps1 -LiveAi`。
2. GitHub尚未连接且未指定用户目标仓库，未推送。原前端remote指向上游作者仓库，不能擅自推送到该地址。
3. 本机Docker Engine未启动，Dockerfile/Compose和GitHub Actions已准备，未在容器或远端CI中执行。

## AI接口

全部需要`Authorization: Bearer <token>`。保持原项目响应包装`{code,message,data}`。

| 方法 | 路径 | 参数 |
| --- | --- | --- |
| GET | `/api/ai/context` | 查询参数bookId、type |
| POST | `/api/ai/parse` | JSON：bookId、type、text |
| POST | `/api/ai/confirm` | JSON：下列完整草稿 |
| GET | `/api/ai/budget-alerts` | 查询参数bookId |
| POST | `/api/ai/monthly-report` | JSON：bookId、month（yyyy-MM） |

```json
{
  "requestId": "a-new-uuid-for-this-draft",
  "bookId": 1,
  "type": "支出",
  "amount": "128.00",
  "transferFee": null,
  "billTime": "2026-10-19 12:00:00",
  "remark": "午餐",
  "outAssetId": 1,
  "inAssetId": null,
  "billCategoryId": 1
}
```

编号需使用真实UUID；重复提交保留原编号。业务码401为身份失效、403为资源权限、409为并发修改冲突、422为参数或解析业务错误、429为限流、500为服务异常。

## 面试现场演示顺序

1. 登录一个本地演示账号，展示账本、两个账户和余额。
2. 有可用模型时输入“一笔支付宝餐饮支出”，展示真实解析草稿；无额度时明确说明限制，改用手动草稿展示后端流程。
3. 修改金额并确认，到账单页面查看记录和余额变化。
4. 使用相同提交编号重试，证明只记一次。
5. 展示分类预算80%与超支提醒。
6. 生成月报，检查真实金额；模型不可用时明确显示系统基础统计。
7. 展示并发与回滚测试，而不是只展示成功页面。

## 发布检查

打包时排除`.runtime`、node_modules、target、dist、本地密钥与旧Git元数据。保留原始项目来源说明。源码中的历史示例密钥和JWT已替换为占位数据；已有真实密钥如曾出现在历史仓库中，仍建议在服务商侧轮换，删除当前文件不能抹除历史泄露。
