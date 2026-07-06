# 学习养成计划 — 一周实践 TODO

> **课程**：软件系统实践（小学期） | **2人** | **周期：7天**
> **原则**：覆盖全部需求点，但每个功能用最简单的方式实现

---

## 一周时间规划

```
周一            周二            周三           周四            周五          周六          周日
环境搭建   →   后端开发    →   前端开发   →   前端+联调   →   文档报告   →   收尾   →   答辩
```

---

## Day 1 — 环境搭建

| ID | 任务 | 负责人 | 状态 |
|----|------|--------|------|
| 1.1 | Spring Boot 项目初始化（Maven + 依赖） | A | ⬜ |
| 1.2 | 数据库建表（user / task / check_in / recommendation 4张表 + 预置推荐数据） | A | ⬜ |
| 1.3 | MyBatis 配置 + 数据源 | A | ⬜ |
| 1.4 | Vue 3 项目初始化（Vite + Element Plus + ECharts + Axios + Router） | B | ⬜ |

---

## Day 2 — 后端开发（全部 API）

| ID | 任务 | 接口 | 负责人 | 状态 |
|----|------|------|--------|------|
| 2.1 | 统一返回格式 | Result<T> {code, msg, data} | A | ⬜ |
| 2.2 | 用户注册 | POST /api/user/register | A | ⬜ |
| 2.3 | 用户登录 | POST /api/user/login → JWT | A | ⬜ |
| 2.4 | JWT 拦截器 | Token 校验 | A | ⬜ |
| 2.5 | 任务 CRUD | GET/POST/PUT/DELETE /api/tasks | A | ⬜ |
| 2.6 | 打卡 | POST /api/checkin/{taskId} | A | ⬜ |
| 2.7 | 打卡记录 + 日历数据 | GET /api/checkin/history + /api/checkin/calendar | A | ⬜ |
| 2.8 | 统计数据 | GET /api/statistics（完成率/每日统计/趋势） | A | ⬜ |
| 2.9 | 推荐模板 | GET /api/recommendations | A | ⬜ |
| 2.10 | 到期提醒 | GET /api/tasks/upcoming（临近截止+高优任务） | A | ⬜ |
| 2.11 | CORS 配置 | 允许前端跨域 | A | ⬜ |

**Day 2 目标**：Postman 调通全部 11 个接口。

---

## Day 3 — 前端页面开发（核心页面）

| ID | 任务 | 说明 | 负责人 | 状态 |
|----|------|------|--------|------|
| 3.1 | Axios 封装 | 请求拦截器注入 Token，统一错误处理 | B | ⬜ |
| 3.2 | 登录/注册页 | 表单验证 → 存储 Token → 跳转主页 | B | ⬜ |
| 3.3 | 主页布局 | Element Plus 布局框架 + 侧边导航 | B | ⬜ |
| 3.4 | 任务列表页 | 列表 + 优先级标签 + 状态筛选 + 打卡按钮 | B | ⬜ |
| 3.5 | 新建/编辑任务弹窗 | 表单 + 日期选择 + 推荐模板卡片（点击自动填充） | B | ⬜ |
| 3.6 | 打卡记录页 | 历史打卡列表 | B | ⬜ |

---

## Day 4 — 前端数据可视化（图表 + 日历）

| ID | 任务 | 说明 | 负责人 | 状态 |
|----|------|------|--------|------|
| 4.1 | 统计页面 — 进度条 | Element Plus el-progress，完成率 | B | ⬜ |
| 4.2 | 统计页面 — 统计表 | el-table，每日任务添加数/完成数 | B | ⬜ |
| 4.3 | 统计页面 — 打卡日历 | ECharts Calendar 热力图，直接套官方示例 | B | ⬜ |
| 4.4 | 统计页面 — 折线图 | ECharts Line，每日完成率趋势 | B | ⬜ |
| 4.5 | 提醒面板 | 任务列表顶部 "即将到期" 区域，调用 /api/tasks/upcoming | B | ⬜ |

**ECharts 实现技巧**：每个图表去[官方示例](https://echarts.apache.org/examples/)复制代码，替换数据源即可，不用从头写。

---

## Day 5 — 联调 + 测试

| ID | 任务 | 说明 | 负责人 | 状态 |
|----|------|------|--------|------|
| 5.1 | 前后端全流程联调 | 注册→登录→推荐模板建任务→打卡→统计→日历→提醒 | 共同 | ⬜ |
| 5.2 | 修 Bug | — | 共同 | ⬜ |
| 5.3 | 界面微调 | 间距/配色/响应式 | B | ⬜ |

---

## Day 6 — 文档

| ID | 任务 | 说明 | 负责人 | 状态 |
|----|------|------|--------|------|
| 6.1 | ✅ 需求规格说明书 | 已完成 | — | ✅ |
| 6.2 | 系统设计文档（简要） | 架构图 + API 文档 + 数据库设计说明 | A | ⬜ |
| 6.3 | 实践报告 | 过程总结、分工、遇到的问题与收获 | 共同 | ⬜ |
| 6.4 | README | 项目说明 + 运行方式 | A | ⬜ |

---

## Day 7 — 答辩

| ID | 任务 | 说明 | 负责人 | 状态 |
|----|------|------|--------|------|
| 7.1 | 答辩 PPT | 背景→需求→设计→实现→演示→总结 | B | ⬜ |
| 7.2 | 演示准备 | 截图/录屏核心流程 | 共同 | ⬜ |
| 7.3 | 最终检查 | 代码能跑通、文档完整、Git 提交清晰 | 共同 | ⬜ |

---

## 功能覆盖对照（确保不丢分）

| 原始需求 | 实现方式 | 简化策略 |
|---------|---------|---------|
| 用户自定义任务 | 任务 CRUD + 属性表单 | — |
| 系统推荐任务 | 4条预置模板，卡片点击自动填充 | 不做独立页面，放弹窗里 |
| 任务属性设置 | 标题/描述/截止时间/优先级/复习提醒 | — |
| 任务提醒 | 任务列表顶部"即将到期"区域 | 不做定时推送，查询筛选即可 |
| 打卡功能 | 打卡按钮 + 时间记录 + 状态更新 | — |
| 打卡日历 | ECharts Calendar 热力图 | 套官方示例 |
| 进度条 | Element Plus el-progress | — |
| 统计表 | el-table 每日添加数/完成数 | — |
| 任务分析 | ECharts 折线图（每日完成率趋势） | 套官方示例 |

---

## 数据库（4张表）

| 表 | 字段 | 预置数据 |
|----|------|---------|
| user | id, username, password, nickname, created_at | — |
| task | id, user_id, title, description, priority, deadline, need_review, status, created_at | — |
| check_in | id, user_id, task_id, check_time | — |
| recommendation | id, title, description, priority, category | 4条 |

---

## API 清单（11个）

| 方法 | 路径 | 说明 | JWT |
|------|------|------|-----|
| POST | /api/user/register | 注册 | ❌ |
| POST | /api/user/login | 登录，返回Token | ❌ |
| GET | /api/tasks | 任务列表（?status=&priority=） | ✅ |
| POST | /api/tasks | 创建任务 | ✅ |
| PUT | /api/tasks/{id} | 编辑任务 | ✅ |
| DELETE | /api/tasks/{id} | 删除任务 | ✅ |
| GET | /api/recommendations | 推荐模板列表 | ✅ |
| POST | /api/checkin/{taskId} | 打卡 | ✅ |
| GET | /api/checkin/history | 打卡记录 | ✅ |
| GET | /api/checkin/calendar | 日历数据（?year=&month=） | ✅ |
| GET | /api/statistics | 统计数据（完成率/每日统计/趋势） | ✅ |
| GET | /api/tasks/upcoming | 即将到期任务（提醒用） | ✅ |

---

## 人员分工

| | A（后端） | B（前端） |
|------|----------|----------|
| **Day 1** | Spring Boot + 建表 + MyBatis | Vue + 组件库搭建 |
| **Day 2** | 全部 12 个 API | 辅助测试接口 + 准备 ECharts 示例 |
| **Day 3** | 修后端 Bug + 协助联调 | 登录/注册/任务列表/打卡页面 |
| **Day 4** | 协助 ECharts 数据接口调试 | 统计图表 + 打卡日历 + 提醒面板 |
| **Day 5** | 联调 + 修Bug | 联调 + 界面微调 |
| **Day 6** | 设计文档 + README | 实践报告 |
| **Day 7** | 演示准备 | PPT |

---

> **心态**：每个需求点都覆盖到，但用最简单的方式实现。ECharts 套官方示例改数据源，推荐模板硬编码4条，提醒用查询不用定时任务。一周结束，12个验收点全部能演示就是满分。
