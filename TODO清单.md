# 学习养成计划 — 一周实践 TODO

> **课程**：软件系统实践（小学期） | **2人** | **周期：7天**
> **技术栈**：Vue 3 + Spring Boot + MyBatis + MySQL

---

## 一周时间规划

```
周一          周二          周三          周四          周五          周六         周日
环境搭建  →  后端开发  →  后端+前端  →  前端开发  →  联调测试  →  文档报告  →  答辩准备
```

| 天数 | 阶段 | 目标 |
|------|------|------|
| Day 1 | 🔧 环境搭建 | 项目初始化、数据库建表、Git就绪 |
| Day 2 | ⚙️ 后端开发 | 用户认证、任务CRUD API |
| Day 3 | ⚙️+🖥️ 后端收尾+前端起步 | 打卡API、前端框架搭建 |
| Day 4 | 🖥️ 前端开发 | 页面开发、图表 |
| Day 5 | 🔗 联调测试 | 前后端打通、修Bug |
| Day 6 | 📝 文档 | 实践报告、整理提交 |
| Day 7 | 🎤 答辩 | PPT、演示准备 |

---

## Day 1 — 环境搭建

| ID | 任务 | 负责人 | 状态 |
|----|------|--------|------|
| 1.1 | Spring Boot 项目初始化（Maven） | A | ⬜ |
| 1.2 | 数据库建表（DDL，4张表） | A | ⬜ |
| 1.3 | MyBatis 配置 + 数据源 | A | ⬜ |
| 1.4 | Vue 3 项目初始化（Vite） | B | ⬜ |
| 1.5 | Element Plus + Axios + Router 引入 | B | ⬜ |
| 1.6 | Git 仓库初始化 + .gitignore | 共同 | ⬜ |

## Day 2 — 后端核心开发

| ID | 任务 | 说明 | 负责人 | 状态 |
|----|------|------|--------|------|
| 2.1 | 统一返回格式 Result<T> | {code, msg, data} | A | ⬜ |
| 2.2 | 用户注册 API | POST /api/user/register (BCrypt) | A | ⬜ |
| 2.3 | 用户登录 API | POST /api/user/login → JWT | A | ⬜ |
| 2.4 | JWT 拦截器 | Token 校验 | A | ⬜ |
| 2.5 | 任务创建 API | POST /api/tasks | A | ⬜ |
| 2.6 | 任务列表/详情 API | GET /api/tasks, GET /api/tasks/{id} | A | ⬜ |
| 2.7 | 任务编辑/删除 API | PUT/DELETE /api/tasks/{id} | A | ⬜ |

## Day 3 — 后端收尾 + 前端起步

| ID | 任务 | 说明 | 负责人 | 状态 |
|----|------|------|--------|------|
| 3.1 | 打卡 API | POST /api/checkin/{taskId} | A | ⬜ |
| 3.2 | 打卡记录 API | GET /api/checkin/history | A | ⬜ |
| 3.3 | 统计 API | GET /api/statistics (完成率/打卡天数) | A | ⬜ |
| 3.4 | CORS 跨域配置 | 允许前端请求 | A | ⬜ |
| 3.5 | 登录/注册页面 | 表单+验证+跳转 | B | ⬜ |
| 3.6 | 路由配置 + 布局框架 | Vue Router + Element Plus 布局 | B | ⬜ |

## Day 4 — 前端页面开发

| ID | 任务 | 说明 | 负责人 | 状态 |
|----|------|------|--------|------|
| 4.1 | 任务列表页 | 列表展示 + 优先级排序 + 筛选 | B | ⬜ |
| 4.2 | 新建/编辑任务弹窗 | 表单 + 日期选择 + 优先级选择 | B | ⬜ |
| 4.3 | 打卡功能 | 任务卡片打卡按钮 + 状态更新 | B | ⬜ |
| 4.4 | 打卡记录页 | 历史打卡列表 | B | ⬜ |
| 4.5 | 统计页 | 完成率进度条 + 打卡天数 | A/B | ⬜ |
| 4.6 | 提醒页面 | 提醒列表（简单展示） | B | ⬜ |

## Day 5 — 联调测试

| ID | 任务 | 说明 | 负责人 | 状态 |
|----|------|------|--------|------|
| 5.1 | Axios Token 注入 | 请求拦截器加 JWT | B | ⬜ |
| 5.2 | 前后端联调 | 全流程走通 | 共同 | ⬜ |
| 5.3 | Bug 修复 | 边测边改 | 共同 | ⬜ |
| 5.4 | 定时提醒（可选） | @Scheduled 扫描到期任务 | A | ⬜ |

## Day 6 — 文档

| ID | 任务 | 说明 | 负责人 | 状态 |
|----|------|------|--------|------|
| 6.1 | 需求规格说明书 | ✅ 已完成 | — | ✅ |
| 6.2 | 系统设计文档（简要） | 架构图、接口文档 | A | ⬜ |
| 6.3 | 实践报告 | 过程总结、分工、问题与收获 | 共同 | ⬜ |
| 6.4 | 代码整理 | 注释、README | 共同 | ⬜ |

## Day 7 — 答辩准备

| ID | 任务 | 说明 | 负责人 | 状态 |
|----|------|------|--------|------|
| 7.1 | 答辩 PPT | 背景/需求/设计/演示/总结 | B | ⬜ |
| 7.2 | 系统演示录制/截图 | 核心功能展示 | 共同 | ⬜ |
| 7.3 | 最终检查 | 代码能跑通、文档完整 | 共同 | ⬜ |

---

## 人员分工

| | A（后端偏重） | B（前端偏重） |
|------|-------------|-------------|
| **Day 1-3** | Spring Boot + API + 数据库 | Vue 项目搭建 + 页面开发 |
| **Day 4-5** | 联调协助 + 修Bug + 定时任务 | 页面完成 + 数据对接 |
| **Day 6-7** | 设计文档 + 代码整理 | 实践报告 + 答辩PPT |

---

## API 接口清单（后端要做的）

| 方法 | 路径 | 说明 | JWT |
|------|------|------|-----|
| POST | /api/user/register | 注册 | ❌ |
| POST | /api/user/login | 登录，返回Token | ❌ |
| GET | /api/tasks | 任务列表（支持?status=&priority=） | ✅ |
| POST | /api/tasks | 创建任务 | ✅ |
| GET | /api/tasks/{id} | 任务详情 | ✅ |
| PUT | /api/tasks/{id} | 编辑任务 | ✅ |
| DELETE | /api/tasks/{id} | 删除任务 | ✅ |
| POST | /api/checkin/{taskId} | 打卡 | ✅ |
| GET | /api/checkin/history | 打卡记录 | ✅ |
| GET | /api/statistics | 统计数据 | ✅ |
| GET | /api/reminders | 提醒列表 | ✅ |

---

## 数据库表（4张）

| 表 | 核心字段 | 
|----|---------|
| user | id, username, password, nickname, created_at |
| task | id, user_id, title, description, priority, deadline, status, created_at |
| check_in | id, user_id, task_id, check_time |
| reminder | id, user_id, task_id, message, remind_time, is_read |

---

> **原则**：先跑通核心流程（注册→建任务→打卡→看统计），再锦上添花。一周结束能完整演示就是胜利。
