# Where Money Frontend

一个基于 Vue 3 + Vite + TypeScript 的个人记账前端项目，支持登录注册、账本管理、账单记录、资产统计、预算管理和主题定制。

## 功能概览

- 用户系统：登录、注册、修改用户名/密码
- 账本与账单：按日/月查看账单，支持支出/收入/转账/退款
- 资产管理：资产总览、净资产统计、近 30 天资产变化
- 预算管理：按账本与分类设置预算并追踪使用情况
- 统计分析：月收支卡片、分类占比图、日维度分布图
- 界面体验：暗黑模式、主题色切换、响应式布局

## 技术栈

- `Vue 3`、`TypeScript`、`Vite 5`
- `Pinia`、`Vue Router`
- `Naive UI`
- `Windi CSS` + `Tailwind CSS`
- `ECharts` / `G2Plot`
- `Axios`

## 环境要求

- `Node.js >= 18`
- `npm >= 9`

## 快速开始

```bash
npm install
npm run dev
```

默认开发端口：`13140`（见 `vite.config.mts`）。

## 环境变量

项目使用 Vite 环境变量，主要配置在 `.env.development`、`.env.production`。

| 变量名 | 说明 | 示例 |
| --- | --- | --- |
| `VITE_APP_NAME` | 应用名称 | `Where Money` |
| `VITE_APP_TITLE` | 浏览器标题 | `Where Money` |
| `VITE_APP_INTERFACE_URL` | 后端接口基础地址 | `http://localhost:5050/api/` |
| `VITE_APP_BASE_URL` | 路由基础路径 | `` |
| `VITE_STORAGE_PREFIX` | 本地存储前缀 | `` |
| `VITE_IS_VERCEL` | Vercel 部署标识（`1` 时使用 hash 路由） | `1` |
| `VITE_HTTP_ENV` | 构建环境标识（脚本内注入） | `test` / `prod` |

## 可用脚本

- `npm run dev`：本地开发
- `npm run build`：生产构建
- `npm run build:test`：测试环境构建（先类型检查）
- `npm run build:vercel`：Vercel 构建（自动切换 hash history）
- `npm run preview`：本地预览构建产物（端口 `5050`）
- `npm run typecheck`：TS 类型检查
- `npm run serve`：以 production mode 启动 Vite 开发服务

## 项目结构

```text
.
├─ build/                 # Vite 插件与构建配置
├─ deploy/                # 部署脚本
├─ public/
│  ├─ logo.png
│  └─ resource/           # 首屏 loading 资源
├─ src/
│  ├─ apis/               # 接口封装
│  ├─ components/         # 通用组件
│  ├─ layouts/            # 布局系统
│  ├─ router/             # 路由与守卫
│  ├─ settings/           # 主题与颜色配置
│  ├─ store/              # Pinia 状态管理
│  ├─ styles/             # 全局样式
│  └─ views/              # 页面（home/bill/asset/budget/about 等）
├─ vite.config.mts
└─ windi.config.ts
```

## 后端对接说明

- 前端请求基地址由 `VITE_APP_INTERFACE_URL` 控制
- 默认通过 `Authorization: Bearer <token>` 传递登录态
- 账单相关接口中，除 `/bill/bill` 外，`post/patch` 会转为 `application/x-www-form-urlencoded`

## 部署说明

- 常规部署：执行 `npm run build` 后发布 `dist/`
- Vercel：使用 `npm run build:vercel`
- 仓库内提供了 Windows PowerShell 脚本：`deploy/deply.ps1`（含本地绝对路径，使用前请按你的环境修改）

## 致谢

项目工程结构参考并基于 Soybean Admin 模板演进。
