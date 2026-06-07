# 通用智能客服AI系统 - 架构设计文档

> **文档版本**: v2.0  
> **适用范围**: 通用电商/零售/金融/教育/医疗/SaaS 等行业智能客服场景  
> **最后更新**: 2026-06-07

---

## 目录

1. [项目概述](#1-项目概述)
2. [需求分析](#2-需求分析)
3. [系统架构](#3-系统架构)
4. [技术选型](#4-技术选型)
5. [后端模块设计](#5-后端模块设计)
6. [前端模块设计](#6-前端模块设计)
7. [数据库设计](#7-数据库设计)
8. [API接口设计](#8-api接口设计)
9. [知识库设计](#9-知识库设计)
10. [AI Agent设计](#10-ai-agent设计)
11. [Docker容器化部署](#11-docker容器化部署)
12. [运维监控](#12-运维监控)
13. [实施计划](#13-实施计划)
14. [附录](#14-附录)

---

## 1. 项目概述

### 1.1 项目定位

通用智能客服AI系统是一套**开箱即用、行业通用**的企业级智能客服解决方案。系统以AI大模型为核心，融合RAG（检索增强生成）技术，支持多渠道接入、智能问答、自动化工单、数据分析等完整客服闭环，帮助企业快速搭建智能化客服体系。

### 1.2 核心能力

| 能力域 | 说明 |
|--------|------|
| **智能对话** | 基于LLM的多轮对话，支持上下文理解、意图识别、情感分析 |
| **知识库RAG** | 企业私有知识库接入，文档上传自动向量化，精准检索回答 |
| **多渠道接入** | 统一消息网关，支持Web、H5、API、第三方平台（企微/钉钉/飞书等） |
| **客诉工单** | AI自动创建工单、智能分类分级、流转跟踪、SLA管理 |
| **人工协作** | 智能转人工、坐席工作台、会话接管、内部协作 |
| **数据分析** | 实时运营看板、热点分析、满意度统计、趋势报表 |
| **系统管理** | 多租户隔离、RBAC权限、Prompt管理、配置中心、操作审计 |

### 1.3 适用场景

```
电商零售: 订单查询、退换货、物流跟踪、商品咨询、促销活动
金融保险: 保单查询、理赔进度、产品咨询、风险提示
教育培训: 课程咨询、报名流程、学习进度、资料下载
医疗健康: 预约挂号、报告查询、用药指导、健康科普
SaaS服务: 功能咨询、故障排查、升级指导、API文档
政务民生: 办事指南、进度查询、政策解读、投诉建议
```

---

## 2. 需求分析

### 2.1 用户角色

| 角色 | 说明 | 核心需求 |
|------|------|----------|
| **C端用户** | 终端消费者/客户 | 快速获得问题解答，查询订单/业务进度，提交投诉建议 |
| **客服坐席** | 一线客服人员 | 工作台统一处理会话，工单协作，知识库辅助，快捷回复 |
| **客服主管** | 客服团队管理者 | 坐席监控、质检评估、数据报表、排班管理 |
| **知识库管理员** | 业务专家 | 知识库维护、文档管理、FAQ编辑、检索效果评估 |
| **系统管理员** | IT运维/管理员 | 租户管理、权限配置、系统参数、模型配置、渠道对接 |

### 2.2 核心业务流程

```
┌──────────────────────────────────────────────────────────────────────┐
│                      客服业务全流程闭环                                │
│                                                                      │
│  C端用户发起咨询                                                      │
│       │                                                              │
│       ▼                                                              │
│  ┌──────────┐     ┌──────────┐     ┌──────────┐                     │
│  │ 渠道接入  │────▶│ 意图识别  │────▶│ AI应答   │                     │
│  │(Web/API) │     │(LLM分类) │     │(RAG+LLM)│                     │
│  └──────────┘     └──────────┘     └────┬─────┘                     │
│                                         │                            │
│                          ┌──────────────┼──────────────┐             │
│                          ▼              ▼              ▼             │
│                     ┌────────┐   ┌──────────┐   ┌──────────┐        │
│                     │直接回复 │   │创建工单  │   │ 转人工   │        │
│                     └───┬────┘   └────┬─────┘   └────┬─────┘        │
│                         │             │              │               │
│                         ▼             ▼              ▼               │
│                    ┌────────┐   ┌──────────┐   ┌──────────┐         │
│                    │用户评价 │   │坐席处理  │   │坐席接管  │         │
│                    └───┬────┘   └────┬─────┘   └────┬─────┘         │
│                        │             │              │               │
│                        └─────────────┼──────────────┘               │
│                                      ▼                              │
│                              ┌──────────────┐                       │
│                              │ 会话关闭/归档 │                       │
│                              └──────┬───────┘                       │
│                                     ▼                               │
│                              ┌──────────────┐                       │
│                              │  数据分析    │                       │
│                              │ (报表/看板)  │                       │
│                              └──────────────┘                       │
└──────────────────────────────────────────────────────────────────────┘
```

### 2.3 功能模块全景图

```
通用智能客服AI系统
├── 智能对话引擎
│   ├── 多轮对话管理
│   ├── 意图识别（LLM分类）
│   ├── 情感分析（正/中/负）
│   ├── 流式响应（SSE）
│   ├── 上下文记忆（摘要压缩）
│   └── 快捷回复/推荐追问
├── 知识库管理（RAG）
│   ├── 文档上传/解析（PDF/Word/Excel/TXT/图片）
│   ├── 智能切片（滑动窗口+语义边界）
│   ├── 向量化存储（Embedding+ES）
│   ├── 混合检索（向量+关键词+RRF融合）
│   ├── 手动录入/编辑
│   ├── 分类管理（树形结构）
│   └── 检索效果评估
├── 渠道接入
│   ├── Web嵌入（浮窗/全屏）
│   ├── H5页面
│   ├── REST API（供第三方调用）
│   └── 第三方平台适配（企微/钉钉/飞书/公众号）
├── 客诉工单
│   ├── AI自动创建工单
│   ├── 智能分类分级
│   ├── 工单流转（状态机）
│   ├── SLA时效管理
│   ├── 坐席分配（轮询/负载）
│   └── 处理超时催办
├── 人工坐席工作台
│   ├── 会话列表/排队队列
│   ├── 会话接管/转接
│   ├── 用户画像/历史记录
│   ├── 快捷回复/话术库
│   ├── 内部协作/备注
│   └── 坐席状态管理（在线/忙碌/离线）
├── 数据分析
│   ├── 实时运营看板
│   ├── 会话趋势分析
│   ├── 热点问题聚类
│   ├── 满意度统计
│   ├── 坐席绩效
│   └── 周报/月报自动生成
└── 系统管理
    ├── 多租户管理
    ├── RBAC权限控制
    ├── Prompt模板管理
    ├── 模型配置/路由
    ├── 系统参数配置
    ├── 操作日志审计
    └── 数据字典
```

---

## 3. 系统架构

### 3.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                        客户端 (Client)                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐   │
│  │ Web嵌入   │  │ 管理后台  │  │ H5移动端  │  │ 第三方平台(企微等) │   │
│  │ (浮窗/全屏)│  │ (Admin)  │  │ (Mobile) │  │ (Webhook/API)    │   │
│  └─────┬─────┘  └─────┬────┘  └─────┬────┘  └────────┬─────────┘   │
│        │              │             │                │              │
└────────┼──────────────┼─────────────┼────────────────┼──────────────┘
         │              │             │                │
         └──────────────┼─────────────┼────────────────┘
                        │             │
                        ▼             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Nginx (反向代理 + 静态资源)                        │
│  路由分发 / 限流 / SSL / Gzip / 前端SPA支持                          │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Spring Boot 应用 (单体服务)                        │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                       拦截器层                                 │  │
│  │  认证拦截(JWT) / 租户拦截 / 限流拦截 / 日志拦截                 │  │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │                      Controller 层                            │  │
│  │  Auth │ Chat │ Session │ Ticket │ Knowledge │ Analytics │ System │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │                       Service 层                              │  │
│  │  ┌─────────┐ ┌─────────┐ ┌──────────┐ ┌──────────────────┐  │  │
│  │  │ Chat    │ │ Intent  │ │Knowledge │ │ Ticket           │  │  │
│  │  │ Service │ │Service  │ │Service   │ │ Service          │  │  │
│  │  └────┬────┘ └────┬────┘ └────┬─────┘ └────────┬─────────┘  │  │
│  │       │           │           │                 │            │  │
│  │       ▼           ▼           ▼                 ▼            │  │
│  │  ┌──────────────────────────────────────────────────────┐    │  │
│  │  │              AI Agent 编排层                          │    │  │
│  │  │  RouterAgent → OrderAgent/FAQAgent/ComplaintAgent   │    │  │
│  │  │  + Tool Calling + RAG Pipeline + Fallback           │    │  │
│  │  └──────────────────────────────────────────────────────┘    │  │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │                        DAO 层                                │  │
│  │  MyBatis-Plus Mapper + Redis Repository + ES Repository     │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
         │              │                │
         ▼              ▼                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       数据层 (Data Layer)                            │
│                                                                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  ┌──────────┐   │
│  │  MySQL   │  │  Redis   │  │  Elasticsearch   │  │  MinIO   │   │
│  │  8.0     │  │  7.2     │  │  8.x (向量+全文) │  │ (OSS)    │   │
│  │ 业务数据  │  │ 缓存/会话 │  │ 知识库/RAG检索   │  │ 文件存储  │   │
│  └──────────┘  └──────────┘  └──────────────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.2 架构决策记录

| 决策点 | 选择 | 原因 |
|--------|------|------|
| 服务架构 | **单体应用** (非微服务) | 客服系统业务模块内聚性强，单体架构降低运维复杂度，后期可按需拆分 |
| 服务发现 | **不需要 Nacos** | 单体应用无需服务注册发现，Nginx直接反向代理 |
| API网关 | **不需要 Spring Cloud Gateway** | Nginx足以胜任路由/限流/SSL终结，无需额外网关层 |
| 消息队列 | **不需要 RabbitMQ** | 异步任务（文档解析、索引重建）使用Spring `@Async` + 线程池即可 |
| 前端框架 | **Vue 3 + Element Plus** | 饿了么UI组件库生态成熟，开发效率高，社区活跃 |
| 向量存储 | **Elasticsearch 8.x** | 同时支持全文检索和向量检索，减少中间件数量 |
| 对象存储 | **MinIO** | 兼容S3协议，自建成本低，与Docker无缝集成 |
| LLM集成 | **Spring AI + 多模型适配** | 统一抽象层，支持DeepSeek/通义千问/OpenAI等模型切换 |

### 3.3 精简说明（相比原设计）

| 移除组件 | 原因 |
|----------|------|
| Nacos (服务注册/配置中心) | 单体服务不需要服务发现；配置使用application.yml + 数据库动态配置 |
| Spring Cloud Gateway | Nginx反向代理已覆盖路由/限流/SSL |
| RabbitMQ | 异步任务量小，Spring @Async线程池足够 |
| GraalVM Python 集成 | 无Python依赖需求，统一使用Java生态 |
| 多模块Maven工程 | 改为单模块分包，减少构建复杂度 |
| 独立的钉钉模块 | 渠道对接统一抽象为Channel Adapter，钉钉/企微/飞书按需实现 |

---

## 4. 技术选型

### 4.1 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17+ | LTS稳定版本 |
| Spring Boot | 3.4.x | 核心框架 |
| Spring AI | 1.0.x | AI集成框架（LLM调用/向量存储/Agent） |
| Spring Security | 6.x | 认证授权 |
| MyBatis-Plus | 3.5.x | ORM框架 |
| MySQL | 8.0 | 业务数据存储 |
| Redis | 7.2 | 缓存/会话/分布式锁 |
| Elasticsearch | 8.11+ | 全文检索+向量检索 |
| MinIO | latest | 对象存储（文件上传） |
| Knife4j | 4.x | API文档生成（Swagger增强） |
| Hutool | 5.x | Java工具类库 |
| Apache POI | 5.x | Office文档解析 |
| Apache PDFBox | 3.x | PDF文档解析 |
| Apache Tika | 2.x | 通用文档内容提取 |

### 4.2 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4+ | 渐进式框架 |
| Vite | 5.x | 构建工具 |
| TypeScript | 5.x | 类型安全 |
| **Element Plus** | 2.x | **UI组件库（饿了么团队）** |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Axios | 1.x | HTTP客户端 |
| ECharts | 5.x | 数据可视化图表 |
| Marked | latest | Markdown渲染 |
| Highlight.js | latest | 代码高亮 |

### 4.3 前端 Element Plus 组件使用清单

| 组件 | 使用场景 |
|------|----------|
| `el-container/el-aside/el-main/el-header` | 管理后台布局 |
| `el-menu` | 侧边栏导航菜单 |
| `el-table` | 会话列表、工单列表、知识库列表、用户列表 |
| `el-pagination` | 所有列表分页 |
| `el-form/el-form-item` | 登录表单、配置表单、知识录入 |
| `el-input/el-input-number` | 文本输入、搜索框 |
| `el-select/el-option` | 下拉选择（分类、状态、平台） |
| `el-button` | 所有操作按钮 |
| `el-dialog` | 弹窗（创建工单、编辑知识、分配坐席） |
| `el-drawer` | 侧滑抽屉（会话详情、用户画像） |
| `el-tag` | 状态标签、分类标签 |
| `el-card` | 卡片容器（仪表盘统计卡片、订单卡片） |
| `el-tabs` | 标签页切换（知识库分类、工单详情） |
| `el-tree` | 树形控件（知识库分类树、组织架构） |
| `el-upload` | 文件上传（知识库文档、工单附件） |
| `el-timeline` | 时间线（物流轨迹、工单流转） |
| `el-badge` | 徽标（未读消息数） |
| `el-popover` | 气泡提示（用户信息卡片） |
| `el-tooltip` | 文字提示 |
| `el-dropdown` | 下拉菜单（操作菜单、用户菜单） |
| `el-switch` | 开关（配置项开关） |
| `el-radio/el-radio-group` | 单选（满意度评价、工单优先级） |
| `el-checkbox/el-checkbox-group` | 多选（批量操作） |
| `el-date-picker` | 日期选择（报表筛选） |
| `el-rate` | 评分（满意度评价） |
| `el-progress` | 进度条（上传进度、索引进度） |
| `el-skeleton` | 骨架屏（加载占位） |
| `el-empty` | 空状态（无数据提示） |
| `el-result` | 结果页（操作成功/失败） |
| `el-message` | 消息提示（操作反馈） |
| `el-message-box` | 消息弹窗（确认删除、二次确认） |
| `el-notification` | 通知（新工单提醒、系统通知） |
| `el-alert` | 警告提示（配置提示、系统公告） |
| `el-loading` | 加载动画（全局/局部） |
| `el-avatar` | 头像（用户/坐席头像） |
| `el-backtop` | 回到顶部 |
| `el-breadcrumb` | 面包屑导航 |
| `el-divider` | 分割线 |
| `el-image` | 图片查看（商品图、工单截图） |
| `el-descriptions` | 描述列表（详细信息展示） |
| `el-statistic` | 统计数值（仪表盘数据卡片） |
| `el-collapse` | 折叠面板（FAQ折叠展示） |
| `el-steps` | 步骤条（退换货流程引导） |
| `el-affix` | 固钉（聊天输入框固定底部） |
| `el-scrollbar` | 滚动条（消息列表自定义滚动） |
| `el-color-picker` | 颜色选择器（主题配置） |
| `el-input-tag` | 标签输入（知识库标签管理） |
| `el-text` | 文本组件（内容截断省略） |
| `el-link` | 链接（跳转链接） |

---

## 5. 后端模块设计

### 5.1 代码结构

```
order-chatbot/
├── src/main/java/com/chatbot/
│   ├── ChatbotApplication.java                  # 启动类
│   │
│   ├── config/                                   # 配置类
│   │   ├── SecurityConfig.java                   # Spring Security配置
│   │   ├── RedisConfig.java                      # Redis配置
│   │   ├── ElasticsearchConfig.java              # ES配置
│   │   ├── AsyncConfig.java                      # 异步线程池配置
│   │   ├── WebMvcConfig.java                     # Web配置（拦截器/CORS）
│   │   ├── MinioConfig.java                      # MinIO配置
│   │   └── AiConfig.java                         # AI模型配置
│   │
│   ├── interceptor/                              # 拦截器
│   │   ├── JwtAuthInterceptor.java               # JWT认证拦截
│   │   ├── TenantInterceptor.java                # 租户隔离拦截
│   │   ├── RateLimitInterceptor.java             # 限流拦截
│   │   └── LogInterceptor.java                   # 请求日志拦截
│   │
│   ├── common/                                   # 通用类
│   │   ├── Result.java                           # 统一响应
│   │   ├── PageResult.java                       # 分页响应
│   │   ├── BusinessException.java                # 业务异常
│   │   ├── ErrorCode.java                        # 错误码枚举
│   │   └── enums/                                # 枚举类
│   │       ├── IntentType.java                   # 意图类型
│   │       ├── SentimentType.java                # 情感类型
│   │       ├── TicketStatus.java                 # 工单状态
│   │       ├── TicketPriority.java               # 工单优先级
│   │       └── ChannelType.java                  # 渠道类型
│   │
│   ├── module/                                   # 业务模块(按领域分包)
│   │   ├── auth/                                 # 认证模块
│   │   │   ├── controller/AuthController.java
│   │   │   ├── service/AuthService.java
│   │   │   ├── dto/LoginDTO.java, TokenDTO.java
│   │   │   └── entity/User.java, Role.java
│   │   │
│   │   ├── chat/                                 # 对话模块(核心)
│   │   │   ├── controller/ChatController.java
│   │   │   ├── service/
│   │   │   │   ├── ChatService.java              # 对话主服务
│   │   │   │   ├── IntentService.java            # 意图识别
│   │   │   │   ├── SentimentService.java         # 情感分析
│   │   │   │   ├── ContextService.java           # 上下文管理
│   │   │   │   └── StreamService.java            # SSE流式服务
│   │   │   ├── agent/                            # AI Agent
│   │   │   │   ├── RouterAgent.java              # 路由Agent
│   │   │   │   ├── ToolRegistry.java             # 工具注册
│   │   │   │   └── tools/                        # 工具定义
│   │   │   │       ├── OrderQueryTool.java
│   │   │   │       ├── KnowledgeSearchTool.java
│   │   │   │       └── TicketCreateTool.java
│   │   │   ├── dto/ChatRequestDTO.java, ChatResponseDTO.java
│   │   │   └── entity/ChatSession.java, ChatMessage.java
│   │   │
│   │   ├── session/                              # 会话管理
│   │   │   ├── controller/SessionController.java
│   │   │   ├── service/SessionService.java
│   │   │   └── entity/ChatSession.java
│   │   │
│   │   ├── ticket/                               # 工单模块
│   │   │   ├── controller/TicketController.java
│   │   │   ├── service/
│   │   │   │   ├── TicketService.java            # 工单CRUD
│   │   │   │   ├── TicketWorkflowService.java    # 工单流转
│   │   │   │   └── TicketAssignService.java      # 工单分配
│   │   │   ├── dto/TicketDTO.java
│   │   │   └── entity/ComplaintTicket.java, TicketFlowLog.java
│   │   │
│   │   ├── knowledge/                            # 知识库模块
│   │   │   ├── controller/KnowledgeController.java
│   │   │   ├── service/
│   │   │   │   ├── DocumentService.java          # 文档管理
│   │   │   │   ├── ChunkService.java             # 文档切片
│   │   │   │   ├── EmbeddingService.java         # 向量化
│   │   │   │   ├── IndexService.java             # 索引管理
│   │   │   │   ├── SearchService.java            # 混合检索
│   │   │   │   └── ParseService.java             # 文档解析
│   │   │   ├── dto/KnowledgeDTO.java
│   │   │   └── entity/KnowledgeDoc.java, KnowledgeChunk.java
│   │   │
│   │   ├── channel/                              # 渠道接入模块
│   │   │   ├── controller/ChannelController.java
│   │   │   ├── adapter/                          # 渠道适配器
│   │   │   │   ├── ChannelAdapter.java           # 适配器接口
│   │   │   │   ├── WebChatAdapter.java           # Web内嵌
│   │   │   │   ├── WecomAdapter.java             # 企业微信
│   │   │   │   ├── DingtalkAdapter.java          # 钉钉
│   │   │   │   └── FeishuAdapter.java            # 飞书
│   │   │   └── dto/ChannelMessageDTO.java
│   │   │
│   │   ├── agent/                                # 坐席工作台
│   │   │   ├── controller/AgentController.java
│   │   │   ├── service/
│   │   │   │   ├── AgentWorkbenchService.java    # 坐席工作台
│   │   │   │   ├── QuickReplyService.java        # 快捷回复
│   │   │   │   └── AgentStatusService.java       # 坐席状态
│   │   │   └── dto/AgentDTO.java
│   │   │
│   │   ├── analytics/                            # 数据分析
│   │   │   ├── controller/AnalyticsController.java
│   │   │   ├── service/
│   │   │   │   ├── DashboardService.java         # 仪表盘
│   │   │   │   ├── TrendService.java             # 趋势分析
│   │   │   │   ├── HotTopicService.java          # 热点分析
│   │   │   │   └── ReportService.java            # 报表生成
│   │   │   └── scheduler/ReportScheduler.java    # 定时报表
│   │   │
│   │   └── system/                               # 系统管理
│   │       ├── controller/
│   │       │   ├── ConfigController.java
│   │       │   ├── PromptController.java
│   │       │   ├── UserController.java
│   │       │   └── LogController.java
│   │       ├── service/
│   │       │   ├── SystemConfigService.java
│   │       │   ├── PromptTemplateService.java
│   │       │   └── AuditLogService.java
│   │       └── entity/SysConfig.java, PromptTemplate.java
│   │
│   └── infra/                                    # 基础设施
│       ├── llm/                                  # LLM客户端
│       │   ├── LlmClient.java                    # LLM调用接口
│       │   ├── DeepSeekClient.java               # DeepSeek适配
│       │   ├── QwenClient.java                   # 通义千问适配
│       │   └── ModelRouter.java                  # 模型路由
│       ├── rag/                                  # RAG基础设施
│       │   ├── EsVectorStore.java                # ES向量存储
│       │   ├── HybridSearchService.java          # 混合搜索
│       │   └── RerankService.java                # 重排序
│       └── util/                                 # 工具类
│           ├── JwtUtil.java                      # JWT工具
│           ├── SnowflakeIdGenerator.java         # 雪花ID
│           └── MarkdownRenderer.java             # Markdown渲染
│
├── src/main/resources/
│   ├── application.yml                           # 通用配置
│   ├── application-dev.yml                       # 开发环境
│   ├── application-docker.yml                    # Docker环境
│   └── db/
│       └── migration/                            # Flyway数据库迁移脚本
│           ├── V1__init_schema.sql
│           └── V2__init_data.sql
```

### 5.2 核心流程时序图

#### 5.2.1 用户咨询全流程

```
客户(Web)    Nginx      Controller    ChatService    IntentService    RAG Pipeline    LLM
  │            │            │              │               │               │           │
  │ POST /chat │            │              │               │               │           │
  │───────────▶│───────────▶│──────────────▶               │               │           │
  │            │            │              │ 识别意图       │               │           │
  │            │            │              │──────────────▶│               │           │
  │            │            │              │◀──────────────│               │           │
  │            │            │              │ intent=order_query             │           │
  │            │            │              │ 检索知识库     │               │           │
  │            │            │              │──────────────────────────────▶│           │
  │            │            │              │◀──────────────────────────────│           │
  │            │            │              │ (知识上下文)                    │           │
  │            │            │              │ 组装Prompt + 调用LLM           │           │
  │            │            │              │──────────────────────────────────────────▶│
  │            │            │              │◀──────────────────────────────────────────│
  │            │            │              │ (AI回复)                        │           │
  │            │            │              │ 情感分析                        │           │
  │            │            │◀─────────────│                                │           │
  │            │◀───────────│              │                                │           │
  │◀───────────│            │              │                                │           │
  │ (AI回复)   │            │              │                                │           │
```

#### 5.2.2 RAG检索流程

```
用户问题 → 查询改写(LLM) → 并行检索 ┬→ 向量检索(ES KNN)
                                    └→ 关键词检索(ES BM25)
                                    ↓
                              RRF融合 → ReRank重排序 → Top-K返回
```

### 5.3 异步任务设计

```
┌────────────────────────────────────────────────────────┐
│              Spring @Async 线程池                        │
│                                                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ 文档解析任务  │  │ 向量化任务   │  │ 索引重建任务  │  │
│  │ (parsePool)  │  │(embedPool)  │  │(indexPool)   │  │
│  │ core=2 max=4 │  │ core=4 max=8│  │ core=2 max=4 │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│                                                        │
│  ┌──────────────┐  ┌──────────────┐                    │
│  │ 报表生成任务  │  │ 渠道消息推送 │                    │
│  │(reportPool)  │  │(notifyPool) │                    │
│  │ core=1 max=2 │  │ core=2 max=4│                    │
│  └──────────────┘  └──────────────┘                    │
└────────────────────────────────────────────────────────┘
```

---

## 6. 前端模块设计

### 6.1 前端路由设计

```typescript
// router/index.ts
const routes = [
  // ==================== 用户端 ====================
  {
    path: '/chat',
    name: 'ChatEmbed',
    component: () => import('@/views/chat/ChatEmbed.vue'),  // Web嵌入浮窗
    meta: { title: '智能客服' }
  },
  {
    path: '/chat/full',
    name: 'ChatFull',
    component: () => import('@/views/chat/ChatFull.vue'),   // 独立全屏页
    meta: { title: '智能客服' }
  },

  // ==================== 认证 ====================
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },

  // ==================== 管理后台 ====================
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '运营看板', icon: 'Odometer' }
      },
      {
        path: 'sessions',
        name: 'Sessions',
        component: () => import('@/views/admin/SessionList.vue'),
        meta: { title: '会话管理', icon: 'ChatDotRound' }
      },
      {
        path: 'sessions/:sessionId',
        name: 'SessionDetail',
        component: () => import('@/views/admin/SessionDetail.vue'),
        meta: { title: '会话详情', hidden: true }
      },
      {
        path: 'tickets',
        name: 'Tickets',
        component: () => import('@/views/admin/TicketList.vue'),
        meta: { title: '工单管理', icon: 'Tickets' }
      },
      {
        path: 'tickets/:ticketNo',
        name: 'TicketDetail',
        component: () => import('@/views/admin/TicketDetail.vue'),
        meta: { title: '工单详情', hidden: true }
      },
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('@/views/admin/KnowledgeList.vue'),
        meta: { title: '知识库', icon: 'Collection' }
      },
      {
        path: 'knowledge/create',
        name: 'KnowledgeCreate',
        component: () => import('@/views/admin/KnowledgeEdit.vue'),
        meta: { title: '录入知识', hidden: true }
      },
      {
        path: 'knowledge/:docId/edit',
        name: 'KnowledgeEdit',
        component: () => import('@/views/admin/KnowledgeEdit.vue'),
        meta: { title: '编辑知识', hidden: true }
      },
      {
        path: 'analytics',
        name: 'Analytics',
        component: () => import('@/views/admin/Analytics.vue'),
        meta: { title: '数据分析', icon: 'DataAnalysis' }
      },
      {
        path: 'system',
        name: 'System',
        redirect: '/admin/system/config',
        meta: { title: '系统管理', icon: 'Setting' },
        children: [
          {
            path: 'config',
            name: 'SystemConfig',
            component: () => import('@/views/admin/system/Config.vue'),
            meta: { title: '系统配置' }
          },
          {
            path: 'prompts',
            name: 'PromptManage',
            component: () => import('@/views/admin/system/PromptManage.vue'),
            meta: { title: 'Prompt管理' }
          },
          {
            path: 'users',
            name: 'UserManage',
            component: () => import('@/views/admin/system/UserManage.vue'),
            meta: { title: '用户管理' }
          },
          {
            path: 'logs',
            name: 'AuditLog',
            component: () => import('@/views/admin/system/AuditLog.vue'),
            meta: { title: '操作日志' }
          }
        ]
      }
    ]
  },

  // ==================== 坐席工作台 ====================
  {
    path: '/agent',
    component: () => import('@/layouts/AgentLayout.vue'),
    redirect: '/agent/workbench',
    meta: { requiresAuth: true, roles: ['agent', 'admin'] },
    children: [
      {
        path: 'workbench',
        name: 'AgentWorkbench',
        component: () => import('@/views/agent/Workbench.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'chat/:sessionId',
        name: 'AgentChat',
        component: () => import('@/views/agent/ChatPanel.vue'),
        meta: { title: '会话处理', hidden: true }
      }
    ]
  },

  // ==================== 404 ====================
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue')
  }
]
```

### 6.2 前端组件树

```
App.vue
├── ChatEmbed.vue (Web嵌入浮窗)
│   ├── ChatHeader.vue (头部: logo + 标题 + 最小化/关闭)
│   ├── ChatMessageList.vue (消息列表)
│   │   ├── ChatBubble.vue (消息气泡) × N
│   │   │   └── ChatStreamRenderer.vue (流式渲染)
│   │   │       ├── Markdown 渲染
│   │   │       ├── 参考来源 (el-tag)
│   │   │       └── 快捷追问 (el-button)
│   │   └── ChatTyping.vue (正在输入动画)
│   ├── ChatInput.vue (输入区)
│   │   ├── el-input (textarea)
│   │   ├── 快捷回复 (el-button-group)
│   │   └── 发送按钮 (el-button)
│   └── ChatFeedback.vue (满意度评价: el-rate)
│
├── Login.vue (登录页)
│   └── el-form + el-input + el-button
│
├── AdminLayout.vue (管理后台布局)
│   ├── AdminSidebar.vue (侧边栏: el-menu)
│   ├── AdminHeader.vue (顶栏: el-breadcrumb + 用户下拉)
│   └── <router-view> (内容区)
│       ├── Dashboard.vue (运营看板)
│       │   ├── StatCard.vue × N (统计卡片: el-statistic)
│       │   ├── TrendChart.vue (趋势图: ECharts)
│       │   ├── PlatformPieChart.vue (渠道分布: ECharts)
│       │   └── HotTopicList.vue (热点列表: el-table)
│       │
│       ├── SessionList.vue (会话列表)
│       │   ├── SessionFilter.vue (筛选: el-form + el-select + el-date-picker)
│       │   ├── SessionTable.vue (表格: el-table + el-pagination)
│       │   └── SessionDetail.vue (详情抽屉: el-drawer)
│       │       ├── ChatMessageList.vue (消息记录)
│       │       ├── UserProfile.vue (用户画像: el-descriptions)
│       │       └── SessionActions.vue (操作: el-button-group)
│       │
│       ├── TicketList.vue (工单列表)
│       │   ├── TicketFilter.vue (筛选)
│       │   ├── TicketTable.vue (表格: el-table)
│       │   └── TicketDetail.vue (详情: el-drawer/新页面)
│       │       ├── TicketInfo.vue (基本信息: el-descriptions)
│       │       ├── TicketTimeline.vue (流转记录: el-timeline)
│       │       └── TicketActions.vue (操作: 分配/处理/关闭)
│       │
│       ├── KnowledgeList.vue (知识库)
│       │   ├── KnowledgeSidebar.vue (分类树: el-tree)
│       │   ├── KnowledgeTable.vue (列表: el-table)
│       │   ├── KnowledgeUpload.vue (上传: el-upload)
│       │   └── KnowledgeEdit.vue (编辑: el-form + Markdown编辑器)
│       │
│       ├── Analytics.vue (数据分析)
│       │   ├── DateFilter.vue (日期筛选: el-date-picker)
│       │   ├── Charts.vue × N (各类图表: ECharts)
│       │   └── ReportGenerate.vue (报表生成: el-button)
│       │
│       └── system/ (系统管理)
│           ├── Config.vue (系统配置: el-form)
│           ├── PromptManage.vue (Prompt管理: el-table + el-dialog)
│           ├── UserManage.vue (用户管理: el-table + el-dialog)
│           └── AuditLog.vue (操作日志: el-table + el-date-picker)
│
└── AgentLayout.vue (坐席工作台布局)
    ├── AgentSidebar.vue (会话队列: el-tabs)
    ├── AgentHeader.vue (顶栏: 坐席状态切换 + 信息)
    └── <router-view>
        ├── Workbench.vue (工作台首页)
        │   ├── MyTickets.vue (我的工单: el-table)
        │   ├── OnlineUsers.vue (在线用户: el-table)
        │   └── QuickStats.vue (快捷统计: el-statistic)
        │
        └── ChatPanel.vue (会话处理)
            ├── ChatMessageList.vue (消息列表)
            ├── ChatInput.vue (输入区)
            │   ├── 文本输入 (el-input)
            │   ├── 快捷回复 (el-button-group)
            │   └── 转接/关闭 (el-button)
            ├── UserProfilePanel.vue (用户信息侧边栏: el-descriptions)
            └── KnowledgePanel.vue (知识库侧边栏: 搜索+结果)
```

### 6.3 前端状态管理 (Pinia Store)

```typescript
// stores/user.ts - 用户认证状态
export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    refreshToken: '',
    userInfo: null as UserInfo | null,
    permissions: [] as string[]
  }),
  actions: {
    async login(form: LoginForm) { /* ... */ },
    async getUserInfo() { /* ... */ },
    logout() { /* ... */ }
  }
})

// stores/chat.ts - 聊天状态
export const useChatStore = defineStore('chat', {
  state: () => ({
    currentSessionId: '',
    messages: [] as Message[],
    isStreaming: false,
    streamingContent: '',
    quickReplies: [] as QuickReply[],
    unreadCount: 0
  }),
  actions: {
    async sendMessage(text: string) { /* ... */ },
    async sendStreamMessage(text: string) { /* ... */ },
    async loadHistory(sessionId: string) { /* ... */ },
    addMessage(msg: Message) { /* ... */ }
  }
})

// stores/agent.ts - 坐席状态
export const useAgentStore = defineStore('agent', {
  state: () => ({
    status: 'offline' as AgentStatus, // online/busy/offline
    waitingSessions: [] as Session[],
    activeSessions: [] as Session[],
    myTickets: [] as Ticket[],
    quickReplies: [] as QuickReply[]
  }),
  actions: {
    setStatus(status: AgentStatus) { /* ... */ },
    acceptSession(sessionId: string) { /* ... */ },
    transferSession(sessionId: string, targetAgentId: number) { /* ... */ }
  }
})

// stores/app.ts - 应用全局状态
export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: false,
    tenantId: '', // 当前租户
    tenantName: '',
    systemConfig: {} as SystemConfig
  })
})
```

### 6.4 前端工程化配置

```
order-chatbot-web/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── index.html
├── .env.development          # 开发环境变量
├── .env.production           # 生产环境变量
├── public/
│   └── favicon.ico
└── src/
    ├── main.ts               # 入口：创建App + 注册Element Plus + 注册全局组件
    ├── App.vue
    ├── router/
    │   └── index.ts           # 路由配置 + 导航守卫
    ├── stores/                # Pinia状态管理
    │   ├── user.ts
    │   ├── chat.ts
    │   ├── agent.ts
    │   └── app.ts
    ├── api/                   # API请求封装
    │   ├── request.ts         # Axios实例 + 拦截器(自动刷新Token)
    │   ├── auth.ts
    │   ├── chat.ts
    │   ├── session.ts
    │   ├── ticket.ts
    │   ├── knowledge.ts
    │   ├── analytics.ts
    │   └── system.ts
    ├── composables/           # 组合式函数
    │   ├── useChat.ts         # 聊天逻辑(发送/接收/流式)
    │   ├── useSSE.ts          # SSE流式接收
    │   ├── usePolling.ts      # 轮询
    │   └── usePermission.ts   # 权限判断
    ├── views/                 # 页面组件
    │   ├── chat/
    │   │   ├── ChatEmbed.vue
    │   │   └── ChatFull.vue
    │   ├── auth/
    │   │   └── Login.vue
    │   ├── admin/
    │   │   ├── Dashboard.vue
    │   │   ├── SessionList.vue
    │   │   ├── SessionDetail.vue
    │   │   ├── TicketList.vue
    │   │   ├── TicketDetail.vue
    │   │   ├── KnowledgeList.vue
    │   │   ├── KnowledgeEdit.vue
    │   │   ├── Analytics.vue
    │   │   └── system/
    │   │       ├── Config.vue
    │   │       ├── PromptManage.vue
    │   │       ├── UserManage.vue
    │   │       └── AuditLog.vue
    │   ├── agent/
    │   │   ├── Workbench.vue
    │   │   └── ChatPanel.vue
    │   └── error/
    │       └── NotFound.vue
    ├── components/            # 公共组件
    │   ├── ChatMessageList.vue
    │   ├── ChatBubble.vue
    │   ├── ChatStreamRenderer.vue
    │   ├── ChatInput.vue
    │   ├── ChatFeedback.vue
    │   ├── FileUploader.vue
    │   ├── MarkdownViewer.vue
    │   ├── UserProfile.vue
    │   └── StatCard.vue
    ├── layouts/               # 布局组件
    │   ├── AdminLayout.vue
    │   └── AgentLayout.vue
    ├── types/                 # TypeScript类型定义
    │   ├── api.ts             # ApiResponse, PageResponse
    │   ├── chat.ts            # ChatRequest, ChatResponse, Message
    │   ├── ticket.ts          # Ticket, TicketQuery
    │   ├── knowledge.ts       # KnowledgeDoc, KnowledgeChunk
    │   └── user.ts            # UserInfo, LoginForm
    ├── utils/                 # 工具函数
    │   ├── date.ts            # 日期格式化
    │   ├── storage.ts         # localStorage封装
    │   └── markdown.ts        # Markdown渲染配置
    └── styles/                # 全局样式
        ├── variables.scss     # SCSS变量
        ├── element-override.scss  # Element Plus样式覆盖
        └── global.scss        # 全局样式
```

---

## 7. 数据库设计

### 7.1 ER图（核心实体关系）

```
┌──────────┐       ┌──────────────┐       ┌──────────────┐
│  Tenant  │       │     User     │       │    Role      │
│ (租户)   │1────N│   (用户)     │N────M│   (角色)     │
└──────────┘       └──────────────┘       └──────────────┘
                          │
          ┌───────────────┼───────────────┐
          │               │               │
          ▼               ▼               ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ ChatSession  │  │   Ticket     │  │ KnowledgeDoc │
│ (会话)       │  │  (工单)      │  │ (知识文档)   │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       │                 │                 │
       ▼                 ▼                 ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ ChatMessage  │  │TicketFlowLog │  │KnowledgeChunk│
│ (消息)       │  │ (流转记录)   │  │ (文档切片)   │
└──────────────┘  └──────────────┘  └──────────────┘
```

### 7.2 MySQL表结构

```sql
-- ==================== 租户表 ====================
CREATE TABLE `sys_tenant` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_code` VARCHAR(32) NOT NULL COMMENT '租户编码',
    `tenant_name` VARCHAR(128) NOT NULL COMMENT '租户名称',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用,0禁用',
    `contact_name` VARCHAR(64) COMMENT '联系人',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `expire_time` DATETIME COMMENT '过期时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_tenant_code` (`tenant_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- ==================== 用户表 ====================
CREATE TABLE `sys_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(256) NOT NULL COMMENT '密码(BCrypt加密)',
    `real_name` VARCHAR(64) COMMENT '真实姓名',
    `email` VARCHAR(128) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(512) COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用,0禁用',
    `role` VARCHAR(32) NOT NULL DEFAULT 'agent' COMMENT '角色:admin/agent/supervisor/knowledge_admin',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_tenant_username` (`tenant_id`, `username`),
    INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ==================== 会话表 ====================
CREATE TABLE `chat_session` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话唯一标识',
    `channel` VARCHAR(32) NOT NULL COMMENT '渠道:web/h5/api/wecom/dingtalk/feishu',
    `channel_user_id` VARCHAR(128) COMMENT '渠道用户ID',
    `user_name` VARCHAR(64) COMMENT '用户昵称',
    `user_avatar` VARCHAR(512) COMMENT '用户头像',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1进行中,2已结束,3已转人工,4排队中',
    `agent_id` BIGINT COMMENT '处理坐席ID',
    `sentiment` VARCHAR(16) COMMENT '最新情感:positive/neutral/negative',
    `last_message` TEXT COMMENT '最后一条消息摘要',
    `last_message_time` DATETIME COMMENT '最后消息时间',
    `message_count` INT NOT NULL DEFAULT 0 COMMENT '消息总数',
    `unread_count` INT NOT NULL DEFAULT 0 COMMENT '坐席未读数',
    `start_time` DATETIME NOT NULL COMMENT '会话开始时间',
    `end_time` DATETIME COMMENT '会话结束时间',
    `close_reason` VARCHAR(256) COMMENT '关闭原因',
    `satisfaction` TINYINT COMMENT '满意度:1-5',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_session_id` (`session_id`),
    INDEX `idx_tenant_channel` (`tenant_id`, `channel`, `channel_user_id`),
    INDEX `idx_agent_status` (`agent_id`, `status`),
    INDEX `idx_status_time` (`status`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- ==================== 消息记录表 ====================
CREATE TABLE `chat_message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
    `message_id` VARCHAR(64) NOT NULL COMMENT '消息唯一ID',
    `role` VARCHAR(16) NOT NULL COMMENT 'role:user/assistant/system',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `message_type` VARCHAR(16) NOT NULL DEFAULT 'text' COMMENT 'text/image/card/file',
    `intent` VARCHAR(32) COMMENT '识别意图',
    `sentiment` VARCHAR(16) COMMENT '情感',
    `confidence` DECIMAL(5,4) COMMENT '置信度',
    `rag_docs` JSON COMMENT 'RAG引用文档',
    `suggestions` JSON COMMENT '推荐追问列表',
    `extra_data` JSON COMMENT '扩展数据(订单卡片/商品卡片等)',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读',
    `message_time` DATETIME NOT NULL COMMENT '消息时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_message_id` (`message_id`),
    INDEX `idx_session_time` (`session_id`, `message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息记录表';

-- ==================== 知识文档表 ====================
CREATE TABLE `knowledge_doc` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `doc_id` VARCHAR(64) NOT NULL COMMENT '文档唯一ID',
    `title` VARCHAR(256) NOT NULL COMMENT '文档标题',
    `category_id` BIGINT COMMENT '分类ID',
    `source` VARCHAR(32) NOT NULL COMMENT '来源:upload/manual/api',
    `file_format` VARCHAR(16) COMMENT '文件格式:pdf/docx/xlsx/txt/md',
    `file_url` VARCHAR(512) COMMENT '文件存储URL',
    `file_size` BIGINT COMMENT '文件大小(bytes)',
    `content` MEDIUMTEXT COMMENT '原始文本内容',
    `tags` JSON COMMENT '标签列表',
    `chunk_count` INT NOT NULL DEFAULT 0 COMMENT '切片数量',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1已发布,0草稿,2索引中,3已归档',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    `creator_id` BIGINT COMMENT '创建人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_doc_id` (`doc_id`),
    INDEX `idx_tenant_category` (`tenant_id`, `category_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识文档表';

-- ==================== 知识分类表 ====================
CREATE TABLE `knowledge_category` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID,0为根节点',
    `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_tenant_parent` (`tenant_id`, `parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识分类表';

-- ==================== 知识切片表 ====================
CREATE TABLE `knowledge_chunk` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `chunk_id` VARCHAR(64) NOT NULL COMMENT '切片唯一ID',
    `doc_id` VARCHAR(64) NOT NULL COMMENT '所属文档ID',
    `chunk_index` INT NOT NULL COMMENT '切片序号',
    `content` TEXT NOT NULL COMMENT '切片内容',
    `content_hash` VARCHAR(64) NOT NULL COMMENT '内容哈希',
    `token_count` INT COMMENT 'Token数量',
    `es_index_name` VARCHAR(128) COMMENT 'ES索引名',
    `es_doc_id` VARCHAR(128) COMMENT 'ES文档ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1正常,0已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_chunk_id` (`chunk_id`),
    INDEX `idx_doc_id` (`doc_id`),
    INDEX `idx_es` (`es_index_name`, `es_doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识切片表';

-- ==================== 客诉工单表 ====================
CREATE TABLE `complaint_ticket` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `ticket_no` VARCHAR(32) NOT NULL COMMENT '工单编号(TK+日期+序号)',
    `session_id` VARCHAR(64) COMMENT '关联会话ID',
    `business_no` VARCHAR(64) COMMENT '关联业务单号(订单号/保单号等)',
    `customer_name` VARCHAR(64) COMMENT '客户姓名',
    `customer_phone` VARCHAR(20) COMMENT '客户电话',
    `channel` VARCHAR(32) NOT NULL COMMENT '来源渠道',
    `ticket_type` VARCHAR(32) NOT NULL COMMENT '工单类型',
    `priority` VARCHAR(8) NOT NULL DEFAULT 'P2' COMMENT '优先级:P0/P1/P2/P3',
    `title` VARCHAR(256) NOT NULL COMMENT '工单标题',
    `description` TEXT COMMENT '工单描述(AI摘要)',
    `original_content` TEXT COMMENT '原始对话内容',
    `attachments` JSON COMMENT '附件列表',
    `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSING/RESOLVED/CLOSED',
    `assignee_id` BIGINT COMMENT '处理人ID',
    `resolution` TEXT COMMENT '处理方案',
    `resolution_type` VARCHAR(32) COMMENT '解决方式',
    `satisfaction` TINYINT COMMENT '满意度:1-5',
    `sla_deadline` DATETIME COMMENT 'SLA截止时间',
    `resolve_time` DATETIME COMMENT '解决时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_ticket_no` (`ticket_no`),
    INDEX `idx_tenant_status` (`tenant_id`, `status`),
    INDEX `idx_assignee` (`assignee_id`),
    INDEX `idx_business_no` (`business_no`),
    INDEX `idx_sla` (`sla_deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客诉工单表';

-- ==================== 工单流转记录表 ====================
CREATE TABLE `ticket_flow_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `ticket_no` VARCHAR(32) NOT NULL COMMENT '工单编号',
    `from_status` VARCHAR(16) COMMENT '原状态',
    `to_status` VARCHAR(16) NOT NULL COMMENT '新状态',
    `operator_id` BIGINT COMMENT '操作人ID',
    `operator_name` VARCHAR(64) COMMENT '操作人姓名',
    `operator_type` VARCHAR(16) COMMENT '操作人类型:system/ai/agent/admin',
    `remark` TEXT COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_ticket_no` (`ticket_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单流转记录表';

-- ==================== 快捷回复表 ====================
CREATE TABLE `quick_reply` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `group_name` VARCHAR(64) COMMENT '分组名称',
    `label` VARCHAR(128) NOT NULL COMMENT '快捷回复标签',
    `content` TEXT NOT NULL COMMENT '回复内容',
    `sort_order` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='快捷回复表';

-- ==================== 系统配置表 ====================
CREATE TABLE `sys_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID,0为全局配置',
    `config_key` VARCHAR(64) NOT NULL COMMENT '配置键',
    `config_value` TEXT NOT NULL COMMENT '配置值',
    `config_type` VARCHAR(16) NOT NULL DEFAULT 'string' COMMENT 'string/int/json/boolean',
    `description` VARCHAR(256) COMMENT '配置说明',
    `group_name` VARCHAR(32) NOT NULL DEFAULT 'default' COMMENT '配置分组',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_tenant_key` (`tenant_id`, `config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ==================== Prompt模板表 ====================
CREATE TABLE `prompt_template` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `template_key` VARCHAR(64) NOT NULL COMMENT '模板标识',
    `template_name` VARCHAR(128) NOT NULL COMMENT '模板名称',
    `template_content` TEXT NOT NULL COMMENT '模板内容',
    `variables` JSON COMMENT '模板变量定义',
    `model_type` VARCHAR(32) COMMENT '适用模型',
    `version` INT NOT NULL DEFAULT 1,
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用,0禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_tenant_template` (`tenant_id`, `template_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt模板表';

-- ==================== 操作日志表 ====================
CREATE TABLE `sys_audit_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `user_id` BIGINT COMMENT '操作人ID',
    `username` VARCHAR(64) COMMENT '操作人用户名',
    `operation` VARCHAR(64) NOT NULL COMMENT '操作类型',
    `module` VARCHAR(64) COMMENT '操作模块',
    `target` VARCHAR(256) COMMENT '操作对象',
    `request_params` JSON COMMENT '请求参数',
    `response_result` JSON COMMENT '响应结果',
    `ip_address` VARCHAR(64) COMMENT 'IP地址',
    `user_agent` VARCHAR(512) COMMENT 'UserAgent',
    `duration` INT COMMENT '耗时(ms)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1成功,0失败',
    `error_msg` TEXT COMMENT '错误信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_tenant_time` (`tenant_id`, `create_time`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ==================== 消息反馈表 ====================
CREATE TABLE `chat_feedback` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `message_id` VARCHAR(64) NOT NULL COMMENT '消息ID',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
    `rating` TINYINT NOT NULL COMMENT '1满意(赞),0不满意(踩)',
    `comment` VARCHAR(512) COMMENT '评价备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_message_id` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息反馈表';
```

### 7.3 Redis数据结构设计

| Key Pattern | 类型 | 说明 | TTL |
|-------------|------|------|-----|
| `auth:token:{userId}` | String | 用户JWT Token | 2h |
| `auth:refresh:{userId}` | String | Refresh Token | 7d |
| `session:context:{sessionId}` | Hash | 会话上下文(历史摘要) | 24h |
| `session:memory:{sessionId}` | List | 近N轮对话记录 | 24h |
| `chat:rate_limit:{userId}` | String | 消息发送频率控制 | 1min |
| `chat:lock:{sessionId}` | String | 会话操作分布式锁 | 30s |
| `knowledge:hot:{tenantId}` | ZSet | 热门知识条目 | 永久 |
| `cache:config:{tenantId}:{groupName}` | Hash | 系统配置缓存 | 10min |
| `stat:daily:{tenantId}:{date}:{metric}` | String | 每日统计 | 48h |
| `agent:status:{agentId}` | String | 坐席在线状态 | 实时 |
| `agent:queue:{tenantId}` | List | 待分配会话队列 | 实时 |
| `channel:token:{channel}` | String | 渠道Access Token | 按渠道TTL |

### 7.4 Elasticsearch索引设计

```json
{
  "index": "kb_chunks_{tenantId}",
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0,
    "analysis": {
      "analyzer": {
        "ik_max_word_analyzer": { "type": "custom", "tokenizer": "ik_max_word" },
        "ik_smart_analyzer": { "type": "custom", "tokenizer": "ik_smart" }
      }
    }
  },
  "mappings": {
    "properties": {
      "chunk_id": { "type": "keyword" },
      "doc_id": { "type": "keyword" },
      "tenant_id": { "type": "keyword" },
      "title": { "type": "text", "analyzer": "ik_max_word_analyzer" },
      "category_id": { "type": "keyword" },
      "content": {
        "type": "text",
        "analyzer": "ik_max_word_analyzer",
        "search_analyzer": "ik_smart_analyzer"
      },
      "content_vector": {
        "type": "dense_vector",
        "dims": 1536,
        "index": true,
        "similarity": "cosine"
      },
      "tags": { "type": "keyword" },
      "status": { "type": "integer" },
      "create_time": { "type": "date" }
    }
  }
}
```

---

## 8. API接口设计

> **接口前缀**: `/api/v1`  
> **鉴权方式**: `Authorization: Bearer {token}` (登录接口除外)  
> **数据格式**: JSON, 编码 UTF-8

### 8.1 统一响应格式

```typescript
// types/api.ts
export interface ApiResponse<T = unknown> {
  code: number        // 200=成功
  message: string     // 提示信息
  data: T             // 响应数据
  timestamp: number   // 服务器时间戳
  traceId: string     // 链路追踪ID
}

export interface PageResponse<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}
```

**业务状态码**:

| code | 含义 | 前端处理 |
|------|------|----------|
| 200 | 成功 | 正常展示 |
| 400 | 参数错误 | `ElMessage.warning(res.message)` |
| 401 | 未登录/Token过期 | 跳转登录页 |
| 403 | 无权限 | `ElMessage.error('无操作权限')` |
| 404 | 资源不存在 | 展示空状态 |
| 429 | 请求频繁 | `ElMessage.warning('操作太频繁，请稍后再试')` |
| 500 | 服务器错误 | `ElMessage.error('服务器异常，请稍后重试')` |
| 1001 | 会话已关闭 | 禁用输入框 |
| 1002 | AI处理超时 | 展示重试按钮 |
| 1003 | 知识库未命中 | 展示"抱歉，我暂时无法回答这个问题"，建议转人工 |

### 8.2 认证接口

#### POST /api/v1/auth/login

```json
// Request
{ "username": "admin", "password": "123456" }

// Response
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi...",
    "expiresIn": 7200,
    "userInfo": {
      "id": 1, "username": "admin", "realName": "管理员",
      "role": "admin", "avatar": "https://..."
    }
  }
}
```

#### POST /api/v1/auth/refresh
刷新Token，传入 `{ "refreshToken": "..." }`。

#### GET /api/v1/auth/me
获取当前用户信息。

#### POST /api/v1/auth/logout
登出。

### 8.3 会话接口

#### GET /api/v1/sessions
查询会话列表，支持分页和筛选。

| 参数 | 类型 | 说明 |
|------|------|------|
| page/pageSize | number | 分页 |
| status | number | 1进行中/2已结束/3已转人工 |
| channel | string | 渠道 |
| keyword | string | 用户名模糊搜索 |

#### GET /api/v1/sessions/{sessionId}
获取会话详情。

#### GET /api/v1/sessions/{sessionId}/messages
获取消息历史，支持分页。

#### POST /api/v1/sessions
创建会话。

#### PUT /api/v1/sessions/{sessionId}/close
关闭会话。`{ "closeReason": "问题已解决" }`

#### POST /api/v1/sessions/{sessionId}/transfer
转人工。`{ "agentId": 3, "reason": "..." }`

### 8.4 聊天/对话接口 (核心)

#### POST /api/v1/chat/send (普通模式)

```json
// Request
{
  "sessionId": "sess-xxx",
  "channel": "web",
  "message": "我的订单什么时候发货？",
  "messageType": "text"
}

// Response
{
  "code": 200,
  "data": {
    "messageId": "msg-xxx",
    "sessionId": "sess-xxx",
    "content": "您好，您的订单预计今天下午发货...",
    "intent": "order_query",
    "sentiment": "neutral",
    "confidence": 0.95,
    "ragDocs": [{"docTitle": "发货时效说明", "chunkContent": "...", "score": 0.91}],
    "suggestions": ["修改地址", "查看物流", "联系人工"],
    "needHumanTransfer": false,
    "tokensUsed": 1250,
    "responseTime": 2.3
  }
}
```

#### POST /api/v1/chat/stream (SSE流式模式，推荐)

```
// Request (同 send)
// Response: SSE事件流
data: {"type":"start","requestId":"req-001","intent":"order_query"}

data: {"type":"content","content":"您好"}

data: {"type":"content","content":"，您的订单"}

data: {"type":"rag_docs","docs":[...]}

data: {"type":"suggestions","suggestions":["修改地址","查看物流"]}

data: {"type":"done","tokensUsed":856,"responseTime":3.1}
```

**SSE事件类型**:

| type | 说明 | 前端处理 |
|------|------|----------|
| `start` | 流开始 | 显示"AI正在输入..." |
| `content` | 增量文本 | 追加到流式缓冲区 |
| `rag_docs` | 知识库引用 | 展示"参考来源" |
| `suggestions` | 推荐追问 | 渲染快捷提问按钮 |
| `done` | 流结束 | 停止loading动画 |
| `error` | 异常中断 | 展示错误信息+重试按钮 |

**前端SSE接收核心代码**:

```typescript
// composables/useSSE.ts
export function useSSE() {
  const streamingContent = ref('')
  const isStreaming = ref(false)
  const ragDocs = ref<RagDoc[]>([])
  const suggestions = ref<string[]>([])
  let abortController: AbortController | null = null

  async function start(url: string, body: object) {
    streamingContent.value = ''
    isStreaming.value = true
    abortController = new AbortController()

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${useUserStore().token}`
      },
      body: JSON.stringify(body),
      signal: abortController.signal
    })

    const reader = response.body!.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const event = JSON.parse(line.slice(6))
          switch (event.type) {
            case 'content': streamingContent.value += event.content; break
            case 'rag_docs': ragDocs.value = event.docs; break
            case 'suggestions': suggestions.value = event.suggestions; break
            case 'error': ElMessage.error(event.message); break
          }
        }
      }
    }
  }

  function stop() { abortController?.abort() }
  onUnmounted(() => stop())

  return { streamingContent, isStreaming, ragDocs, suggestions, start, stop }
}
```

#### POST /api/v1/chat/{messageId}/feedback
评价消息。`{ "rating": 1, "comment": "回答很准确" }` (rating: 1=赞, 0=踩)

#### GET /api/v1/chat/quick-replies
获取快捷回复列表。

### 8.5 工单接口

#### GET /api/v1/tickets
工单列表，支持分页和筛选（status/priority/type/keyword）。

#### POST /api/v1/tickets
创建工单。

```json
{
  "sessionId": "sess-xxx",
  "ticketType": "quality",
  "title": "商品质量问题",
  "description": "客户反馈收到的商品有瑕疵",
  "priority": "P1",
  "businessNo": "ORD20260601001"
}
```

#### GET /api/v1/tickets/{ticketNo}
工单详情，含流转记录时间线。

#### PUT /api/v1/tickets/{ticketNo}/status
更新工单状态。`{ "status": "PROCESSING", "operatorNote": "..." }`

#### POST /api/v1/tickets/{ticketNo}/assign
分配处理人。`{ "assigneeId": 5 }`

#### POST /api/v1/tickets/{ticketNo}/resolve
解决工单。`{ "resolution": "...", "resolutionType": "compensation" }`

### 8.6 知识库接口

#### GET /api/v1/knowledge
知识库文档列表，支持分页和筛选（category/status/keyword）。

#### POST /api/v1/knowledge/upload
上传文档 (multipart/form-data)。支持 PDF/Word/Excel/TXT，单文件 ≤ 20MB。

```vue
<!-- 前端上传组件 -->
<el-upload
  action="/api/v1/knowledge/upload"
  :headers="{ Authorization: `Bearer ${token}` }"
  :data="{ categoryId: selectedCategory }"
  accept=".pdf,.doc,.docx,.xls,.xlsx,.txt,.md"
  :before-upload="(file) => file.size <= 20 * 1024 * 1024"
  :on-success="(res) => ElMessage.success(`上传成功，已拆分为${res.data.chunkCount}个片段`)"
  multiple drag
>
  <el-icon><UploadFilled /></el-icon>
  <div>将文件拖到此处，或<em>点击上传</em></div>
</el-upload>
```

#### POST /api/v1/knowledge/manual
手动录入知识。`{ "title": "...", "content": "...", "categoryId": 1, "tags": [...] }`

#### PUT /api/v1/knowledge/{docId}
更新知识条目。

#### DELETE /api/v1/knowledge/{docId}
删除文档（同时清除向量索引）。

#### PUT /api/v1/knowledge/{docId}/reindex
重建索引。前端轮询 `/api/v1/knowledge/{docId}/reindex/progress` 获取进度。

#### GET /api/v1/knowledge/categories
获取分类树（el-tree渲染）。

#### GET /api/v1/knowledge/search?keyword=xxx
搜索知识库。

### 8.7 数据分析接口

#### GET /api/v1/analytics/dashboard
仪表盘数据，包含今日统计、趋势图、渠道分布、热点话题、意图分布。

```json
{
  "todayStats": {
    "totalSessions": 156, "aiHandledRate": 82.5,
    "avgResponseTime": 2.8, "pendingTickets": 5, "satisfactionRate": 94.2
  },
  "sessionTrend": [{"date":"06-01","count":156}, ...],
  "channelDistribution": [{"channel":"web","count":68,"percentage":43.6}, ...],
  "hotTopics": [{"topic":"物流查询","count":45}, ...],
  "intentDistribution": [{"intent":"order_query","count":52,"percentage":33.3}, ...]
}
```

#### GET /api/v1/analytics/trend?startDate=&endDate=&granularity=day
趋势数据。

#### GET /api/v1/analytics/hot-topics?startDate=&endDate=&limit=20
热点问题。

#### POST /api/v1/analytics/report
生成周报/月报。`{ "type": "weekly", "startDate": "...", "endDate": "..." }`

### 8.8 坐席工作台接口

#### GET /api/v1/agent/status
获取当前坐席状态。

#### PUT /api/v1/agent/status
切换状态。`{ "status": "online" }` (online/busy/offline)

#### GET /api/v1/agent/queue
获取待分配会话队列。

#### POST /api/v1/agent/sessions/{sessionId}/accept
接管会话。

#### POST /api/v1/agent/sessions/{sessionId}/transfer
转接会话。`{ "targetAgentId": 3 }`

### 8.9 系统管理接口

#### GET/PUT /api/v1/system/config
获取/更新系统配置。

#### GET/POST/PUT /api/v1/system/prompts
Prompt模板管理。

#### GET/POST/PUT /api/v1/system/users
用户管理。

#### GET /api/v1/system/logs
操作日志查询。

---

## 9. 知识库设计

### 9.1 文档处理Pipeline

```
文件上传 → 格式识别 → 文本提取 → 智能切片 → 向量化 → ES索引
    │          │          │          │          │
    ▼          ▼          ▼          ▼          ▼
  MinIO    Apache      Apache     滑动窗口   Embedding
  存储      Tika        POI/      语义边界   Model
            PDFBox      Tika       512 tokens
                                  overlap 64
```

### 9.2 混合检索策略

```
用户问题
    │
    ▼
查询改写(LLM) ──→ 并行检索 ─┬─→ 向量检索(ES KNN, cosine相似度)
                            └─→ 关键词检索(ES BM25, IK分词)
                            │
                            ▼
                      RRF融合(Reciprocal Rank Fusion)
                            │
                            ▼
                      ReRank重排序
                            │
                            ▼
                      Top-K返回(默认K=5)
```

### 9.3 渠道适配器设计

所有渠道统一实现 `ChannelAdapter` 接口:

```java
public interface ChannelAdapter {
    /** 渠道标识 */
    String getChannel();
    /** 接收消息，统一转换为内部消息格式 */
    ChannelMessageDTO receiveMessage(Map<String, Object> rawMessage);
    /** 推送回复到渠道 */
    void sendReply(String sessionId, ChannelMessageDTO reply);
    /** 验证回调签名 */
    boolean verifySignature(Map<String, String> params, String body);
    /** 获取/刷新渠道Token */
    String getAccessToken();
}
```

**已适配渠道**: Web内嵌、H5、REST API、企业微信、钉钉、飞书 (按需启用)

---

## 10. AI Agent设计

### 10.1 Agent路由架构

```
用户消息 → RouterAgent (意图识别+路由)
               │
    ┌──────────┼──────────┐
    ▼          ▼          ▼
FAQAgent   OrderAgent  ComplaintAgent  → Fallback: 通用Agent
    │          │           │
    ▼          ▼           ▼
┌─────────────────────────────────────────┐
│              Tool 工具集                  │
│  · search_knowledge(query)              │
│  · query_business(businessNo)           │
│  · query_logistics(businessNo)          │
│  · create_ticket(type, title, desc)     │
│  · check_policy(type)                   │
└─────────────────────────────────────────┘
```

### 10.2 系统Prompt模板

```
你是一个专业的智能客服AI助手，你的职责是帮助用户解决各类问题。

## 你的能力
1. 解答常见问题（基于知识库）
2. 查询业务单号状态和进度
3. 处理售后问题和投诉
4. 提供操作指导和流程说明

## 行为准则
- 始终使用礼貌、专业的语气
- 回复简洁明了，避免冗长
- 无法处理时主动建议转接人工客服
- 用户情绪激动时优先安抚情绪
- 涉及敏感操作时明确告知流程和时效

## 知识库参考信息
{knowledge_context}

## 对话历史摘要
{conversation_summary}

## 当前日期
{current_date}
```

### 10.3 Tool定义 (Function Calling)

```json
{
  "tools": [
    {
      "name": "search_knowledge",
      "description": "从企业知识库中检索相关信息",
      "parameters": {
        "query": { "type": "string", "description": "检索查询语句" },
        "category": { "type": "string", "description": "知识分类，可选" }
      },
      "required": ["query"]
    },
    {
      "name": "query_business",
      "description": "根据业务单号查询业务状态",
      "parameters": {
        "businessNo": { "type": "string", "description": "业务单号" }
      },
      "required": ["businessNo"]
    },
    {
      "name": "query_logistics",
      "description": "查询物流轨迹",
      "parameters": {
        "businessNo": { "type": "string", "description": "业务单号" }
      },
      "required": ["businessNo"]
    },
    {
      "name": "create_ticket",
      "description": "创建客诉工单",
      "parameters": {
        "ticketType": { "type": "string", "description": "工单类型" },
        "title": { "type": "string", "description": "工单标题" },
        "description": { "type": "string", "description": "工单描述" },
        "priority": { "type": "string", "enum": ["P0","P1","P2","P3"] }
      },
      "required": ["ticketType", "title", "description"]
    }
  ]
}
```

---

## 11. Docker容器化部署

### 11.1 部署架构

```
┌─────────────────────────────────────────────────────────────────┐
│                    Docker Host (单机 / 云服务器)                  │
│                                                                 │
│  ┌──────────┐  ┌──────────────────────┐                         │
│  │  Nginx   │  │  Spring Boot App     │                         │
│  │  :80/443 │  │  :8080               │                         │
│  └──────────┘  └──────────────────────┘                         │
│                                                                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────────┐  ┌──────────┐   │
│  │  MySQL   │  │  Redis   │  │ Elasticsearch│  │  MinIO   │   │
│  │  :3306   │  │  :6379   │  │:9200/:9300   │  │  :9000   │   │
│  └──────────┘  └──────────┘  └──────────────┘  └──────────┘   │
│                                                                 │
│  ┌──────────┐  ┌──────────┐                                     │
│  │Prometheus│  │ Grafana  │                                     │
│  │  :9090   │  │  :3000   │                                     │
│  └──────────┘  └──────────┘                                     │
└─────────────────────────────────────────────────────────────────┘
```

### 11.2 docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0.35
    container_name: chatbot-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-Chatbot@2026!}
      MYSQL_DATABASE: chat_bot
      MYSQL_USER: chatbot
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-Chatbot@2026!}
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./mysql/init:/docker-entrypoint-initdb.d
    networks:
      - chatbot-net
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  redis:
    image: redis:7.2-alpine
    container_name: chatbot-redis
    restart: unless-stopped
    command: redis-server --appendonly yes --maxmemory 512mb --maxmemory-policy allkeys-lru
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - chatbot-net
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: chatbot-es
    restart: unless-stopped
    environment:
      - discovery.type=single-node
      - ES_JAVA_OPTS=-Xms1g -Xmx1g
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    volumes:
      - es_data:/usr/share/elasticsearch/data
    networks:
      - chatbot-net
    healthcheck:
      test: ["CMD-SHELL", "curl -s http://localhost:9200/_cluster/health | grep -q 'green\\|yellow'"]

  minio:
    image: minio/minio:latest
    container_name: chatbot-minio
    restart: unless-stopped
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: ${MINIO_USER:-admin}
      MINIO_ROOT_PASSWORD: ${MINIO_PASS:-Chatbot@2026!}
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data
    networks:
      - chatbot-net

  chatbot-app:
    build:
      context: ./app
      dockerfile: Dockerfile
    container_name: chatbot-app
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: docker
      TZ: Asia/Shanghai
      JAVA_OPTS: "-Xms512m -Xmx1024m -XX:+UseG1GC"
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
      elasticsearch:
        condition: service_healthy
    networks:
      - chatbot-net

  nginx:
    image: nginx:1.26-alpine
    container_name: chatbot-nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./nginx/conf.d:/etc/nginx/conf.d
      - ./nginx/ssl:/etc/nginx/ssl
      - ./web/dist:/usr/share/nginx/html
    depends_on:
      - chatbot-app
    networks:
      - chatbot-net

  prometheus:
    image: prom/prometheus:latest
    container_name: chatbot-prometheus
    restart: unless-stopped
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
    networks:
      - chatbot-net

  grafana:
    image: grafana/grafana:latest
    container_name: chatbot-grafana
    restart: unless-stopped
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_USER: ${GRAFANA_USER:-admin}
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_PASS:-Chatbot@2026!}
    volumes:
      - grafana_data:/var/lib/grafana
    networks:
      - chatbot-net

volumes:
  mysql_data:
  redis_data:
  es_data:
  minio_data:
  grafana_data:

networks:
  chatbot-net:
    driver: bridge
```

### 11.3 应用Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache tzdata curl fontconfig ttf-dejavu
ENV TZ=Asia/Shanghai

RUN addgroup -g 1000 chatbot && \
    adduser -u 1000 -G chatbot -s /bin/sh -D chatbot

RUN mkdir -p /app/logs && chown -R chatbot:chatbot /app
WORKDIR /app
COPY order-chatbot.jar app.jar
USER chatbot
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 11.4 Nginx配置要点

```nginx
# 前端SPA路由 (History模式)
location /admin {
    alias /usr/share/nginx/html;
    try_files $uri $uri/ /index.html;
}

# API反向代理
location /api/ {
    proxy_pass http://chatbot-app:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_read_timeout 120s;
}

# SSE流式对话特殊配置
location /api/v1/chat/stream {
    proxy_pass http://chatbot-app:8080;
    proxy_buffering off;
    proxy_cache off;
    proxy_read_timeout 300s;
    chunked_transfer_encoding on;
}
```

### 11.5 环境变量 (.env)

```env
MYSQL_ROOT_PASSWORD=Chatbot@2026!
MYSQL_PASSWORD=Chatbot@2026!
MINIO_USER=admin
MINIO_PASS=Chatbot@2026!
GRAFANA_USER=admin
GRAFANA_PASS=Chatbot@2026!
DEEPSEEK_API_KEY=sk-your-deepseek-api-key
QWEN_API_KEY=sk-your-qwen-api-key
```

---

## 12. 运维监控

### 12.1 监控指标

| 类别 | 指标 | 告警阈值 |
|------|------|----------|
| **系统** | CPU使用率 | > 80% 持续5min |
| | 内存使用率 | > 85% |
| | JVM Heap | > 80% |
| **业务** | 会话QPS | 偏离基线 ±50% |
| | 平均响应时间 | > 5s |
| | 转人工率 | > 30% |
| **中间件** | MySQL连接数 | > 80% max |
| | Redis命中率 | < 80% |
| | ES集群健康 | Yellow/Red |
| **AI** | LLM调用失败率 | > 5% |
| | 意图识别准确率 | < 80% |

### 12.2 Grafana推荐面板

1. **系统概览**: CPU/内存/磁盘/JVM
2. **业务实时**: 会话QPS、响应时间P50/P99、转人工率
3. **AI调用**: LLM调用次数、Token消耗、成功率
4. **知识库**: 文档总量、RAG检索QPS与命中率
5. **工单看板**: 待处理工单、当日新增、SLA超时

---

## 13. 实施计划

### 13.1 里程碑

| 阶段 | 时间 | 内容 |
|------|------|------|
| Phase 1 | 第1-2周 | 基础设施搭建：Docker环境、MySQL/Redis/ES/MinIO部署 |
| Phase 2 | 第3-5周 | 核心AI能力：LLM调用、RAG检索、意图识别、情感分析 |
| Phase 3 | 第6-8周 | 业务功能：会话管理、工单系统、知识库管理、坐席工作台 |
| Phase 4 | 第9-10周 | 前后端联调、数据分析、管理后台、部署上线 |

### 13.2 快速启动命令

```bash
# 1. 克隆项目
git clone <your-repo>/order-chatbot.git
cd order-chatbot

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env，填入 LLM API Key 等

# 3. 构建并启动
docker-compose up -d

# 4. 查看状态
docker-compose ps

# 5. 查看日志
docker-compose logs -f chatbot-app

# 6. 访问
# 管理后台: http://localhost/admin
# API文档: http://localhost:8080/doc.html
# Grafana: http://localhost:3000
```

---

## 14. 附录

### 14.1 安全加固清单

| 项目 | 操作 | 优先级 |
|------|------|--------|
| 数据库密码 | 修改为强密码 | P0 |
| Redis密码 | 设置requirepass | P0 |
| .env文件 | `chmod 600 .env` | P0 |
| JWT密钥 | 使用强随机密钥 | P0 |
| HTTPS | 生产环境强制HTTPS | P0 |
| SQL注入 | 使用MyBatis参数化查询 `#{}` | P0 |
| 敏感数据脱敏 | 手机号/地址日志脱敏 | P1 |
| 防火墙 | 仅开放80/443，内部端口不对外 | P1 |
| 定期备份 | MySQL每日自动备份 | P1 |
| 限流 | Nginx + 应用层双重限流 | P1 |

### 14.2 常见问题排查

**Q: Docker容器启动失败**
```bash
docker-compose logs [service-name]  # 查看具体日志
# 常见原因: 端口冲突 / 内存不足(ES需要1G+) / 权限问题
```

**Q: ES向量检索返回空**
```bash
curl http://localhost:9200/_cat/indices      # 确认索引存在
curl http://localhost:9200/kb_chunks_1/_count # 确认文档已写入
```

**Q: LLM API调用失败**
```bash
# 检查API Key配置
docker-compose logs chatbot-app | grep -i "api.key\|error"
```

### 14.3 关键参考文档

| 文档 | 链接 |
|------|------|
| Spring AI 官方文档 | https://docs.spring.io/spring-ai/reference/ |
| Element Plus 官方文档 | https://element-plus.org/ |
| DeepSeek API 文档 | https://platform.deepseek.com/api-docs/ |
| 通义千问 API 文档 | https://help.aliyun.com/zh/dashscope/ |
| Docker Compose 文档 | https://docs.docker.com/compose/ |
| Elasticsearch KNN 检索 | https://www.elastic.co/guide/en/elasticsearch/reference/current/knn-search.html |

---

> **文档维护**: 本文档随系统迭代持续更新，最新版本以 Git 仓库为准。  
> **文档版本**: v2.0 (2026-06-07)  
> **变更说明**: 从茶具电商专用系统重构为通用智能客服系统，后端精简为单体架构，前端全面采用Element Plus，增加多租户、坐席工作台、渠道适配器等通用能力。