# 订单客服AI系统 - 架构设计文档

> **版本**: v2.0  
> **日期**: 2026-06-01  
> **文档密级**: 内部  
> **目标读者**: 前端开发工程师 / 后端开发工程师 / 运维工程师  
> **架构模式**: 前后端分离 (Vue 3 + Spring Boot)

---

## 目录

1. [项目概述](#1-项目概述)
2. [需求分析](#2-需求分析)
3. [系统架构总览](#3-系统架构总览)
4. [技术选型](#4-技术选型)
5. [前端架构设计](#5-前端架构设计)
6. [后端模块详细设计](#6-后端模块详细设计)
7. [数据库设计](#7-数据库设计)
8. [API 接口设计（前端开发手册）](#8-api-接口设计前端开发手册)
9. [知识库与钉钉对接方案](#9-知识库与钉钉对接方案)
10. [AI Agent 智能体设计](#10-ai-agent-智能体设计)
11. [Docker 容器化部署](#11-docker-容器化部署)
12. [运维监控方案](#12-运维监控方案)
13. [分步实施计划](#13-分步实施计划)
14. [附录](#14-附录)

---

## 1. 项目概述

### 1.1 项目背景

某茶具电商公司，年销售额过亿，办公人员约 50 人，业务覆盖线下门店及全电商平台（天猫、京东、抖音、拼多多等），配套工厂数十人。企业业务体系与办公系统齐全，但数字化运营存在明显短板，新媒体运营效益不高。

当前客服团队面临的核心痛点：
- **人力压力大**：多平台订单咨询集中涌入，销售助理需同时处理大量重复性问题
- **响应不及时**：非工作时间客户咨询无法得到即时回复，影响转化率与客户满意度
- **知识分散**：产品资料、售后政策、订单状态等信息分散在钉钉文档、ERP 系统、各电商后台，查询效率低
- **客诉处理慢**：客诉流程依赖人工判断与流转，缺乏标准化处理机制
- **数据沉淀弱**：客服对话数据未有效结构化存储，无法支撑运营分析与服务优化

### 1.2 项目目标

构建一套**订单客服 AI 系统**，实现以下目标：

| 目标 | 描述 | 量化指标 |
|------|------|----------|
| 智能问答 | 自动回答 80% 以上的常见订单咨询 | 人工介入率 < 20% |
| 知识库驱动 | 对接钉钉企业文件，构建统一知识库 | 知识覆盖率 > 90% |
| 客诉处理 | 自动分类客诉等级，标准化工单流转 | 客诉响应时间 < 5min |
| 多平台统一 | 对接主流电商平台消息通道 | 覆盖天猫/京东/抖音/拼多多 |
| 7x24 服务 | 非工作时间自动应答 | 夜间响应率 100% |
| 数据驱动 | 客服数据沉淀与分析 | 周报自动生成 |

### 1.3 系统边界

```
┌─────────────────────────────────────────────────────────────────┐
│                        订单客服AI系统                             │
│                                                                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐   │
│  │ 智能问答  │  │ 订单查询  │  │ 客诉处理  │  │ 知识库管理   │   │
│  │ Engine   │  │ Engine   │  │ Engine    │  │ Engine       │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────┘   │
│                                                                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐   │
│  │ 多平台   │  │ 钉钉集成  │  │ 数据看板  │  │ 系统管理     │   │
│  │ 消息网关  │  │ 模块     │  │ 模块     │  │ 模块         │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 需求分析

### 2.1 功能需求

#### 2.1.1 智能客服对话 (P0)

| 编号 | 功能 | 描述 |
|------|------|------|
| F-01 | 文本对话 | 支持自然语言多轮对话，理解上下文 |
| F-02 | 意图识别 | 自动识别用户意图（查订单/问产品/售后/投诉） |
| F-03 | 情感分析 | 识别客户情绪，负面情绪自动升级 |
| F-04 | 多轮澄清 | 信息不足时主动追问，补全关键信息 |
| F-05 | 人工转接 | 无法处理时无缝转接人工客服 |
| F-06 | 欢迎语/结束语 | 平台差异化欢迎语与结束语配置 |

#### 2.1.2 订单查询与处理 (P0)

| 编号 | 功能 | 描述 |
|------|------|------|
| F-07 | 订单状态查询 | 根据订单号/手机号查询订单状态 |
| F-08 | 物流追踪 | 查询物流轨迹，自动推送异常信息 |
| F-09 | 退换货引导 | 根据订单状态引导退换货流程 |
| F-10 | 发票处理 | 发票申请、重开等常见问题处理 |

#### 2.1.3 客诉管理 (P1)

| 编号 | 功能 | 描述 |
|------|------|------|
| F-11 | 客诉分类 | 自动分类客诉类型（质量/物流/服务等） |
| F-12 | 等级判定 | 根据规则判定客诉优先级 |
| F-13 | 工单生成 | 自动创建客诉工单，分配处理人 |
| F-14 | 进度追踪 | 客诉处理进度实时可查，自动催办 |
| F-15 | 知识沉淀 | 客诉处理方案自动入库 |

#### 2.1.4 知识库管理 (P0)

| 编号 | 功能 | 描述 |
|------|------|------|
| F-16 | 文档导入 | 支持 PDF/Word/Excel/图片 等多格式导入 |
| F-17 | 钉钉同步 | 自动同步钉钉企业文件到知识库 |
| F-18 | 向量化索引 | 文档自动切片、向量化、建立索引 |
| F-19 | 知识检索 | 语义检索 + 关键词检索混合召回 |
| F-20 | 知识更新 | 支持增量更新与过期知识清理 |

#### 2.1.5 数据分析 (P1)

| 编号 | 功能 | 描述 |
|------|------|------|
| F-21 | 会话统计 | 会话量、响应时长、满意度统计 |
| F-22 | 热点分析 | 高频问题TOP N分析 |
| F-23 | 周报/月报 | 自动生成客服运营报告 |

### 2.2 非功能需求

| 编号 | 类别 | 要求 |
|------|------|------|
| NF-01 | 性能 | 单次问答响应时间 < 3s（含RAG检索） |
| NF-02 | 并发 | 支持峰值 500 QPS |
| NF-03 | 可用性 | 系统可用性 ≥ 99.9% |
| NF-04 | 安全 | API 鉴权、数据脱敏、传输加密 |
| NF-05 | 扩展性 | 支持水平扩展，新电商平台接入 < 3天 |
| NF-06 | 数据 | 会话数据保留 180 天，关键数据永久保留 |

---

## 3. 系统架构总览

### 3.1 前后端分离架构

```
┌──────────────────────────────────────────────────────────────────────┐
│                          客户端 (Client)                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐    │
│  │ 管理后台  │  │ 客服工作台 │  │ 数据看板  │  │  移动端H5(预留)  │    │
│  │ (Admin)  │  │ (Agent)  │  │ (Dashboard)│ │  (Mobile)       │    │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────────┬─────────┘    │
│       └──────────────┴─────────────┴───────────────┘                │
│                          │  HTTP/HTTPS + SSE (流式)                   │
└──────────────────────────┼───────────────────────────────────────────┘
                           │
┌──────────────────────────┼───────────────────────────────────────────┐
│                   前端 SPA (Vue 3 + Vite)                             │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Nginx (静态资源托管 + 反向代理)                                │   │
│  │  · /admin/*  → Vue管理后台静态文件                             │   │
│  │  · /agent/*  → Vue客服工作台静态文件                           │   │
│  │  · /api/*    → 反向代理到后端服务                               │   │
│  └──────────────────────────────────────────────────────────────┘   │
└──────────────────────────┼───────────────────────────────────────────┘
                           │  /api/* 反向代理
                           ▼
┌──────────────────────────────────────────────────────────────────────┐
│                     后端服务 (Spring Boot)                            │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │        API Gateway (Nginx) 限流 / 鉴权 / 路由 / 日志          │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                             │                                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐       │
│  │ 会话管理  │  │ 订单服务  │  │ AI Agent │  │  知识库服务   │       │
│  │ Service  │  │ Service  │  │ Service  │  │  Service     │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────┘       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                         │
│  │ 客诉工单  │  │ 钉钉集成  │  │ 数据分析  │                         │
│  │ Service  │  │ Service  │  │ Service  │                         │
│  └──────────┘  └──────────┘  └──────────┘                         │
│                                                                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐       │
│  │ RabbitMQ │  │  Redis   │  │ MySQL    │  │Elasticsearch │       │
│  │ 消息队列  │  │ 缓存/会话 │  │ 业务数据  │  │ 向量/全文检索 │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────┘       │
│  ┌──────────┐  ┌──────────┐                                        │
│  │  MinIO   │  │ 钉钉API  │                                        │
│  │ 文件存储  │  │ 外部集成  │                                        │
│  └──────────┘  └──────────┘                                        │
└──────────────────────────────────────────────────────────────────────┘
```

### 3.2 架构全景图（含外部集成）

```
                              ┌─────────────────────────────────────┐
                              │           钉钉开放平台              │
                              │  ┌─────────────────────────────┐    │
                              │  │  企业文件 / 审批 / 消息通知  │    │
                              │  └──────────┬──────────────────┘    │
                              └─────────────┼───────────────────────┘
                                            │
  ┌─────────────────┐  ┌───────────────────┼───────────────────────┐
  │  天猫/京东/抖音  │  │                   ▼                      │
  │  拼多多/微信    │  │  ┌─────────────────────────────────┐     │
  │  电商平台消息   │  │  │        API Gateway (Nginx)       │     │
  └────────┬────────┘  │  │   限流 / 鉴权 / 路由 / 日志     │     │
           │           │  └──────────────┬──────────────────┘     │
           ▼           │                 │                         │
  ┌────────────────┐   │  ┌──────────────┼──────────────┐         │
  │  消息接入网关   │   │  │              │              │         │
  │ (Webhook/     │   │  ▼              ▼              ▼         │
  │  长连接/轮询) │   │ ┌──────┐  ┌──────────┐  ┌──────────┐   │
  └───────┬────────┘   │ │订单   │  │ AI Agent │  │知识库    │   │
          │            │ │服务   │  │ 服务     │  │服务      │   │
          ▼            │ │      │  │          │  │          │   │
  ┌────────────────┐   │ │·查订单│ │·意图识别 │  │·文档解析 │   │
  │  消息分发总线   │   │ │·物流  │ │·对话管理 │  │·向量化   │   │
  │  (RabbitMQ)    │   │ │·退换货│ │·RAG检索  │  │·语义检索 │   │
  └───────┬────────┘   │ │      │ │·LLM调用  │  │·钉钉同步 │   │
          │            │ └──┬───┘ └────┬─────┘ └────┬─────┘   │
          ▼            │    │          │            │          │
  ┌────────────────┐   │    │    ┌─────┼────────────┼─────┐   │
  │  会话管理服务   │   │    │    │     ▼            ▼     │   │
  │  ·会话上下文    │   │    │    │ ┌──────────────────┐ │   │
  │  ·多轮记忆     │   │    │    │ │   RAG 检索引擎    │ │   │
  │  ·人工转接     │   │    │    │ │ · 向量检索(ES8)  │ │   │
  └───────┬────────┘   │    │    │ │ · 关键词检索     │ │   │
          │            │    │    │ │ · 重排序(ReRank) │ │   │
          ▼            │    │    │ └──────────────────┘ │   │
  ┌────────────────┐   │    │    └──────────────────────┘   │
  │  客诉工单服务   │   │    │                               │
  │  ·工单创建     │   │    │    ┌──────────────────────┐   │
  │  ·等级判定     │   │    │    │    数据存储层         │   │
  │  ·流转管理     │   │    │    │                      │   │
  └───────┬────────┘   │    │    │ MySQL  Redis  ES     │   │
          │            │    │    │ (业务) (缓存) (向量)  │   │
          ▼            │    │    │                      │   │
  ┌────────────────┐   │    │    │ Milvus/Qdrant       │   │
  │  数据分析服务   │   │    │    │ (向量数据库备选)     │   │
  │  ·统计报表     │   │    │    └──────────────────────┘   │
  │  ·热点分析     │   │    │                               │
  └────────────────┘   │    └───────────────────────────────┘
                       │             订单客服AI系统
                       └───────────────────────────────────────
```

### 3.3 分层架构

```
┌─────────────────────────────────────────────────────────────┐
│                    接入层 (Access Layer)                     │
│  Nginx 负载均衡 / SSL终结 / 限流 / WAF                       │
├─────────────────────────────────────────────────────────────┤
│                    网关层 (Gateway Layer)                    │
│  Spring Cloud Gateway / 认证鉴权 / 请求路由 / 日志           │
├─────────────────────────────────────────────────────────────┤
│                   业务服务层 (Business Service Layer)        │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┐  │
│  │会话管理  │订单查询  │客诉工单  │知识库管理│数据分析  │  │
│  │Service   │Service   │Service   │Service   │Service   │  │
│  └──────────┴──────────┴──────────┴──────────┴──────────┘  │
├─────────────────────────────────────────────────────────────┤
│                   AI能力层 (AI Capability Layer)             │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┐  │
│  │LLM调用   │意图识别  │RAG检索   │情感分析  │Agent编排 │  │
│  │Service   │Service   │Service   │Service   │Service   │  │
│  └──────────┴──────────┴──────────┴──────────┴──────────┘  │
├─────────────────────────────────────────────────────────────┤
│                   中间件层 (Middleware Layer)                │
│  ┌──────────┬──────────┬──────────┬──────────┐             │
│  │RabbitMQ  │Redis     │Elastic-  │Milvus/   │             │
│  │消息队列  │缓存/会话 │search    │Qdrant    │             │
│  └──────────┴──────────┴──────────┴──────────┘             │
├─────────────────────────────────────────────────────────────┤
│                   数据层 (Data Layer)                        │
│  ┌──────────────────────────────────────┐                   │
│  │ MySQL (主) + MySQL (从) 读写分离     │                   │
│  │ · 订单数据  · 用户数据  · 工单数据   │                   │
│  │ · 知识库元数据  · 系统配置           │                   │
│  └──────────────────────────────────────┘                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. 技术选型

### 4.1 技术栈总览

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **开发语言** | Java | 17+ | 主力开发语言 |
| **开发框架** | Spring Boot | 3.5.7 | 微服务基础框架 |
| **AI 框架** | Spring AI Alibaba | 1.1.2.2 | 提供 LLM 调用、RAG、Agent 编排能力 |
| **LLM 模型** | DeepSeek / 通义千问 | - | 大语言模型，按场景切换 |
| **消息队列** | RabbitMQ | 3.13+ | 电商平台消息接入与异步解耦 |
| **缓存** | Redis | 7.x | 会话缓存 / 热点数据 / 分布式锁 |
| **数据库** | MySQL | 8.0+ | 主业务数据库 |
| **搜索引擎** | Elasticsearch | 8.x | 全文检索 + 向量检索 |
| **向量数据库** | Milvus / Qdrant | 2.4+ | 独立向量存储（大知识库场景） |
| **对象存储** | MinIO | latest | 文档/图片文件存储 |
| **网关** | Nginx | 1.26+ | 反向代理 / SSL / 负载均衡 |
| **容器编排** | Docker Compose | v2 | 单机多容器编排 |
| **监控** | Prometheus + Grafana | latest | 系统监控与告警 |
| **日志** | ELK (ES + Logstash + Kibana) | 8.x | 日志采集与分析 |
| **配置中心** | Nacos | 2.3+ | 配置管理与服务发现 |

### 4.2 LLM 模型选择策略

```
                    ┌─────────────────────────────┐
                    │      智能路由 (Router)       │
                    └─────────────┬───────────────┘
                                  │
          ┌───────────────────────┼───────────────────────┐
          ▼                       ▼                       ▼
  ┌───────────────┐      ┌───────────────┐      ┌───────────────┐
  │  DeepSeek-V3  │      │  Qwen-Max     │      │  本地模型      │
  │  复杂推理     │      │  通用对话     │      │  敏感场景      │
  │               │      │               │      │  (可选)       │
  │ · 客诉分析    │      │ · 日常问答    │      │ · 数据不出域  │
  │ · 订单异常    │      │ · 产品介绍    │      │ · 快速响应    │
  │ · 多轮复杂   │      │ · 闲聊       │      │               │
  └───────────────┘      └───────────────┘      └───────────────┘
```

### 4.3 向量数据库方案对比

| 方案 | 优势 | 劣势 | 适用场景 | 推荐 |
|------|------|------|----------|------|
| **Elasticsearch 8.x** | 已有搜索能力，运维成熟 | 向量检索性能非最优 | 中小规模知识库 | ✅ 首选 |
| **Milvus** | 向量检索性能极强 | 需独立部署运维 | 百万级以上向量 | 未来扩展 |
| **Qdrant** | Rust编写，性能好 | 社区相对小 | 纯向量检索需求 | 备选 |

**推荐策略**：初期使用 Elasticsearch 8.x 作为统一的全文检索 + 向量检索引擎，知识库规模增长后再引入 Milvus 作为专用向量数据库。

---

## 6. 后端模块详细设计

### 5.1 模块总览

```
order-chatbot/
├── order-chatbot-common/          # 公共模块
│   ├── dto/                       # 通用DTO
│   ├── enums/                     # 枚举定义
│   ├── exception/                 # 异常定义
│   └── utils/                     # 工具类
│
├── order-chatbot-gateway/         # 消息网关模块
│   ├── platform/                  # 平台适配器
│   │   ├── TmallAdapter.java      # 天猫适配器
│   │   ├── JdAdapter.java         # 京东适配器
│   │   ├── DouyinAdapter.java     # 抖音适配器
│   │   └── PddAdapter.java        # 拼多多适配器
│   ├── dispatcher/                # 消息分发
│   │   └── MessageDispatcher.java
│   └── webhook/                   # Webhook 接收
│       └── WebhookController.java
│
├── order-chatbot-session/         # 会话管理模块
│   ├── controller/
│   │   └── SessionController.java
│   ├── service/
│   │   ├── SessionService.java
│   │   └── ContextManager.java    # 多轮对话上下文管理
│   ├── dao/
│   │   ├── entity/Session.java
│   │   └── repository/SessionRepository.java
│   └── transfer/                   # 人工转接
│       └── AgentTransferService.java
│
├── order-chatbot-agent/           # AI Agent 模块 (核心)
│   ├── intent/                    # 意图识别
│   │   ├── IntentClassifier.java
│   │   └── IntentEnum.java
│   ├── agent/                     # Agent 编排
│   │   ├── OrderAgent.java        # 订单查询Agent
│   │   ├── ProductAgent.java      # 产品咨询Agent
│   │   ├── ComplaintAgent.java    # 客诉处理Agent
│   │   └── RouterAgent.java       # 路由/编排Agent
│   ├── rag/                       # RAG 检索增强
│   │   ├── EmbeddingService.java  # 向量化服务
│   │   ├── VectorSearchService.java
│   │   ├── HybridSearchService.java # 混合检索
│   │   └── RerankService.java     # 重排序
│   ├── llm/                       # LLM 调用
│   │   ├── LlmClient.java
│   │   ├── PromptTemplate.java
│   │   └── ModelRouter.java       # 模型路由
│   ├── sentiment/                 # 情感分析
│   │   └── SentimentAnalyzer.java
│   └── memory/                     # 对话记忆
│       └── ConversationMemory.java
│
├── order-chatbot-order/           # 订单服务模块
│   ├── controller/
│   │   └── OrderController.java
│   ├── service/
│   │   ├── OrderQueryService.java
│   │   ├── LogisticsService.java
│   │   └── RefundService.java
│   ├── dao/
│   │   ├── entity/Order.java
│   │   └── repository/OrderRepository.java
│   └── integration/               # 外部系统集成
│       └── ErpClient.java
│
├── order-chatbot-complaint/       # 客诉工单模块
│   ├── controller/
│   │   └── ComplaintController.java
│   ├── service/
│   │   ├── ComplaintService.java
│   │   ├── TicketService.java
│   │   └── WorkflowService.java
│   └── dao/
│       ├── entity/Complaint.java
│       ├── entity/Ticket.java
│       └── repository/
│
├── order-chatbot-knowledge/       # 知识库模块
│   ├── controller/
│   │   └── KnowledgeController.java
│   ├── service/
│   │   ├── DocumentService.java   # 文档管理
│   │   ├── DingtalkSyncService.java # 钉钉同步
│   │   ├── EmbeddingPipeline.java
│   │   ├── ChunkService.java      # 文档切片
│   │   └── IndexService.java      # 索引管理
│   └── dao/
│       ├── entity/KnowledgeDoc.java
│       └── repository/
│
├── order-chatbot-dingtalk/        # 钉钉集成模块
│   ├── controller/
│   │   └── DingtalkController.java
│   ├── service/
│   │   ├── DingtalkFileService.java   # 文件同步
│   │   ├── DingtalkMessageService.java # 消息推送
│   │   └── DingtalkApproveService.java # 审批流
│   └── client/
│       └── DingtalkOpenClient.java
│
├── order-chatbot-analytics/       # 数据分析模块
│   ├── controller/
│   │   └── AnalyticsController.java
│   ├── service/
│   │   ├── StatisticsService.java
│   │   ├── ReportService.java
│   │   └── HotTopicService.java
│   └── scheduler/
│       └── ReportScheduler.java   # 定时报表
│
└── order-chatbot-admin/           # 系统管理模块
    ├── controller/
    │   ├── ConfigController.java
    │   ├── UserController.java
    │   └── MonitorController.java
    └── service/
        └── SystemConfigService.java
```

### 5.2 核心模块交互流程

#### 5.2.1 用户咨询全流程

```
客户消息 → [平台适配器] → [消息分发] → [会话管理] → [Agent路由]
                                                        │
                                          ┌─────────────┼─────────────┐
                                          ▼             ▼             ▼
                                    意图识别 ←─── RAG检索 ←─── 上下文装配
                                          │             │             │
                                          └─────────────┼─────────────┘
                                                        ▼
                                                  LLM 生成回复
                                                        │
                                          ┌─────────────┼─────────────┐
                                          ▼             ▼             ▼
                                    情感分析       置信度检查     安全审查
                                          │             │             │
                                          └─────────────┼─────────────┘
                                                        ▼
                                                  是否需要转人工？
                                              ┌─────────┴─────────┐
                                              ▼                   ▼
                                         自动回复            人工转接
                                            │                   │
                                            ▼                   ▼
                                      消息回推平台         钉钉通知坐席
```

#### 5.2.2 RAG 检索流程

```
用户问题
    │
    ▼
┌─────────────┐
│  查询改写    │  ← 用LLM改写用户问题，提升检索效果
└──────┬──────┘
       │
       ▼
┌─────────────┐     ┌─────────────┐
│  向量检索    │     │  关键词检索  │   ← 并行检索
│ (语义相似度) │     │ (BM25/全文)│
└──────┬──────┘     └──────┬──────┘
       │                   │
       └─────────┬─────────┘
                 ▼
          ┌─────────────┐
          │  结果融合    │  ← RRF (Reciprocal Rank Fusion)
          └──────┬──────┘
                 ▼
          ┌─────────────┐
          │  ReRank     │  ← 使用重排序模型精排
          └──────┬──────┘
                 ▼
          ┌─────────────┐
          │  Top-K 返回  │  ← 返回最相关的前K个文档片段
          └─────────────┘
```

---

## 7. 数据库设计

### 6.1 MySQL 表结构设计

#### 6.1.1 会话相关

```sql
-- 会话表
CREATE TABLE `chat_session` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话唯一标识(UUID)',
    `platform` VARCHAR(32) NOT NULL COMMENT '平台(tmall/jd/douyin/pdd)',
    `platform_user_id` VARCHAR(128) NOT NULL COMMENT '平台用户ID',
    `user_name` VARCHAR(64) COMMENT '用户昵称',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1进行中,2已结束,3已转人工',
    `agent_id` BIGINT COMMENT '转接坐席ID',
    `start_time` DATETIME NOT NULL COMMENT '会话开始时间',
    `end_time` DATETIME COMMENT '会话结束时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_session_id` (`session_id`),
    INDEX `idx_platform_user` (`platform`, `platform_user_id`),
    INDEX `idx_status_time` (`status`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 消息记录表
CREATE TABLE `chat_message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
    `message_id` VARCHAR(64) NOT NULL COMMENT '消息唯一ID',
    `role` VARCHAR(16) NOT NULL COMMENT '角色:user/assistant/system',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `intent` VARCHAR(32) COMMENT '识别意图',
    `sentiment` VARCHAR(16) COMMENT '情感:positive/neutral/negative',
    `confidence` DECIMAL(5,4) COMMENT '置信度',
    `rag_docs` JSON COMMENT 'RAG检索到的文档引用',
    `message_time` DATETIME NOT NULL COMMENT '消息时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_message_id` (`message_id`),
    INDEX `idx_session_time` (`session_id`, `message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息记录表';
```

#### 6.1.2 知识库相关

```sql
-- 知识文档表
CREATE TABLE `knowledge_doc` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `doc_id` VARCHAR(64) NOT NULL COMMENT '文档唯一ID',
    `title` VARCHAR(256) NOT NULL COMMENT '文档标题',
    `category` VARCHAR(64) NOT NULL COMMENT '分类:product/policy/faq/after_sale',
    `source` VARCHAR(32) NOT NULL COMMENT '来源:dingtalk/upload/manual',
    `dingtalk_file_id` VARCHAR(128) COMMENT '钉钉文件ID',
    `dingtalk_space_id` VARCHAR(128) COMMENT '钉钉空间ID',
    `file_format` VARCHAR(16) COMMENT '文件格式:pdf/docx/xlsx/txt',
    `file_url` VARCHAR(512) COMMENT '文件存储URL',
    `file_size` BIGINT COMMENT '文件大小(bytes)',
    `chunk_count` INT COMMENT '切片数量',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1正常,0已删除,2同步中',
    `last_sync_time` DATETIME COMMENT '最后同步时间',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_doc_id` (`doc_id`),
    INDEX `idx_category` (`category`),
    INDEX `idx_source` (`source`),
    INDEX `idx_dingtalk_file` (`dingtalk_file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识文档表';

-- 文档切片表
CREATE TABLE `knowledge_chunk` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `chunk_id` VARCHAR(64) NOT NULL COMMENT '切片唯一ID',
    `doc_id` VARCHAR(64) NOT NULL COMMENT '所属文档ID',
    `chunk_index` INT NOT NULL COMMENT '切片序号',
    `content` TEXT NOT NULL COMMENT '切片内容',
    `content_hash` VARCHAR(64) NOT NULL COMMENT '内容哈希(判断变更)',
    `token_count` INT COMMENT 'Token数量',
    `es_index_name` VARCHAR(128) COMMENT 'ES索引名',
    `es_doc_id` VARCHAR(128) COMMENT 'ES文档ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1正常,0已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_chunk_id` (`chunk_id`),
    INDEX `idx_doc_id` (`doc_id`),
    INDEX `idx_es` (`es_index_name`, `es_doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识文档切片表';
```

#### 6.1.3 客诉工单相关

```sql
-- 客诉工单表
CREATE TABLE `complaint_ticket` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ticket_no` VARCHAR(32) NOT NULL COMMENT '工单编号(TK+日期+序号)',
    `session_id` VARCHAR(64) COMMENT '关联会话ID',
    `order_no` VARCHAR(64) COMMENT '关联订单号',
    `customer_name` VARCHAR(64) COMMENT '客户姓名',
    `customer_phone` VARCHAR(20) COMMENT '客户电话',
    `platform` VARCHAR(32) NOT NULL COMMENT '来源平台',
    `complaint_type` VARCHAR(32) NOT NULL COMMENT '客诉类型:quality/logistics/service/refund/other',
    `complaint_level` VARCHAR(16) NOT NULL COMMENT '优先级:P0紧急/P1高/P2中/P3低',
    `title` VARCHAR(256) NOT NULL COMMENT '客诉标题',
    `description` TEXT COMMENT '客诉描述(AI摘要)',
    `original_content` TEXT COMMENT '原始对话内容',
    `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态:PENDING/IN_PROGRESS/RESOLVED/CLOSED',
    `assignee_id` BIGINT COMMENT '处理人ID',
    `resolution` TEXT COMMENT '处理方案',
    `satisfaction` TINYINT COMMENT '满意度:1-5',
    `resolve_time` DATETIME COMMENT '解决时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_ticket_no` (`ticket_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_assignee` (`assignee_id`),
    INDEX `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客诉工单表';

-- 工单流转记录
CREATE TABLE `ticket_flow_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ticket_no` VARCHAR(32) NOT NULL COMMENT '工单编号',
    `from_status` VARCHAR(16) COMMENT '原状态',
    `to_status` VARCHAR(16) NOT NULL COMMENT '新状态',
    `operator_id` BIGINT COMMENT '操作人ID',
    `operator_name` VARCHAR(64) COMMENT '操作人姓名',
    `remark` TEXT COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_ticket_no` (`ticket_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单流转记录表';
```

#### 6.1.4 系统配置相关

```sql
-- 系统配置表
CREATE TABLE `sys_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `config_key` VARCHAR(64) NOT NULL COMMENT '配置键',
    `config_value` TEXT NOT NULL COMMENT '配置值',
    `config_type` VARCHAR(16) NOT NULL COMMENT '类型:string/int/json/boolean',
    `description` VARCHAR(256) COMMENT '配置说明',
    `group_name` VARCHAR(32) NOT NULL DEFAULT 'default' COMMENT '配置分组',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- Prompt 模板表
CREATE TABLE `prompt_template` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `template_key` VARCHAR(64) NOT NULL COMMENT '模板标识',
    `template_name` VARCHAR(128) NOT NULL COMMENT '模板名称',
    `template_content` TEXT NOT NULL COMMENT '模板内容',
    `variables` JSON COMMENT '模板变量定义',
    `model_type` VARCHAR(32) COMMENT '适用模型',
    `version` INT NOT NULL DEFAULT 1,
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用,0禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_template_key` (`template_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt模板表';
```

### 6.2 Redis 数据结构设计

| Key Pattern | 类型 | 说明 | TTL |
|-------------|------|------|-----|
| `session:context:{sessionId}` | Hash | 会话上下文（历史消息摘要） | 24h |
| `session:memory:{sessionId}` | List | 近N轮对话记录（JSON序列化） | 24h |
| `chat:rate_limit:{userId}` | String | API调用频率控制 | 1min |
| `chat:lock:{orderNo}` | String | 订单并发操作锁 | 30s |
| `knowledge:hot:{category}` | ZSet | 热门知识条目 | 永久 |
| `cache:product:{productId}` | String | 产品信息缓存 | 1h |
| `cache:order:{orderNo}` | String | 订单信息缓存 | 5min |
| `system:config:{groupName}` | Hash | 系统配置缓存 | 10min |
| `stat:daily:{date}:{metric}` | String | 每日统计数据 | 48h |
| `dingtalk:token` | String | 钉钉Access Token | 7200s |

### 6.3 Elasticsearch 索引设计

```json
// 知识库文档片段索引
{
  "index": "kb_chunks_v1",
  "mappings": {
    "properties": {
      "chunk_id": { "type": "keyword" },
      "doc_id": { "type": "keyword" },
      "title": { "type": "text", "analyzer": "ik_max_word" },
      "category": { "type": "keyword" },
      "content": { 
        "type": "text", 
        "analyzer": "ik_max_word",
        "search_analyzer": "ik_smart"
      },
      "content_vector": { 
        "type": "dense_vector", 
        "dims": 1536,
        "index": true,
        "similarity": "cosine"
      },
      "metadata": { "type": "object" },
      "create_time": { "type": "date" }
    }
  }
}
```

---

## 8. API 接口设计（前端开发手册）

### 7.1 接口规范

- **协议**: HTTPS
- **格式**: JSON
- **编码**: UTF-8
- **版本**: `/api/v1/`
- **鉴权**: Bearer Token (JWT) + API Key (外部调用)

### 7.2 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1717200000000,
  "traceId": "trace-xxx-xxx"
}
```

### 7.3 核心接口清单

#### 7.3.1 会话与消息接口

```
POST   /api/v1/sessions                    # 创建会话
GET    /api/v1/sessions/{sessionId}        # 查询会话详情
PUT    /api/v1/sessions/{sessionId}/close  # 关闭会话
POST   /api/v1/sessions/{sessionId}/transfer # 转人工
GET    /api/v1/sessions/{sessionId}/messages  # 历史消息

POST   /api/v1/chat/send                   # 发送消息(核心接口)
POST   /api/v1/chat/stream                 # 流式对话(SSE)
```

#### 7.3.2 订单查询接口

```
GET    /api/v1/orders/{orderNo}            # 根据订单号查询
GET    /api/v1/orders/search               # 根据手机号/姓名查询
GET    /api/v1/orders/{orderNo}/logistics  # 物流详情
POST   /api/v1/orders/{orderNo}/refund    # 申请退款
```

#### 7.3.3 客诉工单接口

```
POST   /api/v1/tickets                     # 创建工单
GET    /api/v1/tickets/{ticketNo}          # 工单详情
PUT    /api/v1/tickets/{ticketNo}/status   # 更新状态
GET    /api/v1/tickets/pending             # 待处理列表
POST   /api/v1/tickets/{ticketNo}/assign   # 分配处理人
```

#### 7.3.4 知识库管理接口

```
POST   /api/v1/knowledge/upload            # 上传文档
POST   /api/v1/knowledge/batch-import      # 批量导入
POST   /api/v1/knowledge/sync-dingtalk     # 触发钉钉同步
DELETE /api/v1/knowledge/{docId}           # 删除文档
GET    /api/v1/knowledge/search            # 知识检索
PUT    /api/v1/knowledge/{docId}/reindex   # 重建索引
```

#### 7.3.5 钉钉集成接口

```
GET    /api/v1/dingtalk/files              # 获取钉钉文件列表
POST   /api/v1/dingtalk/sync              # 手动同步指定文件
POST   /api/v1/dingtalk/webhook           # 钉钉机器人回调
GET    /api/v1/dingtalk/spaces            # 获取知识库空间列表
```

#### 7.3.6 数据分析接口

```
GET    /api/v1/analytics/dashboard         # 实时看板
GET    /api/v1/analytics/trend             # 趋势分析
GET    /api/v1/analytics/hot-topics        # 热点问题
POST   /api/v1/analytics/report/weekly     # 生成周报
```

### 7.4 核心接口示例

**POST /api/v1/chat/send** (发送消息)

```json
// Request
{
  "sessionId": "sess-uuid-xxx",
  "platform": "tmall",
  "message": "我前天买的紫砂壶什么时候能到？",
  "messageType": "text"
}

// Response
{
  "code": 200,
  "data": {
    "messageId": "msg-uuid-xxx",
    "sessionId": "sess-uuid-xxx",
    "content": "您好，您的紫砂壶订单（订单号：TB2026052812345）已于5月29日发货，目前快递已到达【杭州市分拣中心】，预计明天下午前送达。您可以通过以下链接查看详细物流信息：https://...",
    "intent": "order_query",
    "sentiment": "neutral",
    "confidence": 0.95,
    "references": [
      {
        "docTitle": "物流查询FAQ",
        "chunkContent": "...",
        "score": 0.89
      }
    ],
    "suggestions": ["我要退货", "修改收货地址", "联系人工客服"],
    "needHumanTransfer": false
  }
}
```

---

## 9. 知识库与钉钉对接方案

### 8.1 钉钉集成架构

```
┌─────────────────────────────────────────────────────────┐
│                    钉钉开放平台                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────────┐  │
│  │ 钉钉文档  │  │ 企业文件  │  │ 钉钉知识库(新版)    │  │
│  │ (Doc)    │  │ (File)   │  │ (Knowledge Base)    │  │
│  └────┬─────┘  └────┬─────┘  └──────────┬───────────┘  │
└───────┼──────────────┼───────────────────┼──────────────┘
        │              │                   │
        ▼              ▼                   ▼
┌─────────────────────────────────────────────────────────┐
│               DingtalkOpenClient (钉钉API客户端)         │
│  · 获取 Access Token                                    │
│  · 文件列表获取 / 文件下载                               │
│  · 知识库空间/节点获取                                   │
│  · 文件变更 Webhook 订阅                                 │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│              DingtalkSyncService (同步服务)              │
│                                                         │
│  ┌─────────────┐  ┌─────────────┐  ┌────────────────┐  │
│  │ 全量同步    │  │ 增量同步    │  │ 变更监听       │  │
│  │ (定时任务)  │  │ (定时检测)  │  │ (Webhook)     │  │
│  └──────┬──────┘  └──────┬──────┘  └───────┬────────┘  │
│         │                │                 │            │
│         └────────────────┼─────────────────┘            │
│                          ▼                              │
│               ┌─────────────────────┐                   │
│               │ 文档解析 Pipeline    │                   │
│               │ · PDF 解析          │                   │
│               │ · Word/DOCX 解析    │                   │
│               │ · Excel 解析        │                   │
│               │ · 图片 OCR          │                   │
│               └─────────┬───────────┘                   │
│                         ▼                               │
│               ┌─────────────────────┐                   │
│               │ 文档切片 + 向量化    │                   │
│               └─────────────────────┘                   │
└─────────────────────────────────────────────────────────┘
```

### 8.2 钉钉开放平台接入步骤

#### Step 1: 创建钉钉应用

```
1. 登录钉钉开放平台 (https://open.dingtalk.com)
2. 创建企业内部应用 → 选择"H5微应用"
3. 获取应用凭证:
   - AppKey: dingxxx...
   - AppSecret: xxx...
4. 配置应用权限:
   - 知识库权限: KnowledgeBase.Read / KnowledgeBase.Write
   - 企业文件权限: File.Read
   - 文档权限: Doc.Read
   - 通讯录权限: Contact.Read (用户信息)
   - 机器人权限: Robot.Send (消息推送)
```

#### Step 2: 获取 Access Token

```java
// 伪代码示例
public String getAccessToken() {
    String cacheKey = "dingtalk:token";
    String token = redisTemplate.opsForValue().get(cacheKey);
    if (token != null) return token;
    
    String url = "https://oapi.dingtalk.com/gettoken" +
        "?appkey=" + appKey + "&appsecret=" + appSecret;
    
    DingtalkTokenResponse resp = restTemplate.getForObject(url, ...);
    token = resp.getAccessToken();
    redisTemplate.opsForValue().set(cacheKey, token, 7000, TimeUnit.SECONDS);
    return token;
}
```

#### Step 3: 同步钉钉知识库文档

```java
// 同步流程伪代码
public void syncDingtalkKnowledge() {
    // 1. 获取知识库列表
    List<Space> spaces = dingtalkClient.getKnowledgeSpaces(accessToken);
    
    for (Space space : spaces) {
        // 2. 获取知识节点列表
        List<Node> nodes = dingtalkClient.getNodes(accessToken, space.getId());
        
        for (Node node : nodes) {
            // 3. 检查是否需要更新（对比版本号/修改时间）
            if (!needUpdate(node)) continue;
            
            // 4. 下载文件
            byte[] fileBytes = dingtalkClient.downloadFile(accessToken, node.getFileId());
            
            // 5. 文档解析
            String text = documentParser.parse(fileBytes, node.getFormat());
            
            // 6. 切片
            List<Chunk> chunks = chunkService.split(text);
            
            // 7. 向量化 + 存入ES
            for (Chunk chunk : chunks) {
                float[] vector = embeddingService.embed(chunk.getContent());
                esService.index(chunk, vector);
            }
            
            // 8. 记录同步状态
            updateSyncStatus(node);
        }
    }
}
```

### 8.3 文档处理 Pipeline

```
┌─────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ 文件下载 │───▶│ 格式识别  │───▶│ 文本提取  │───▶│ 智能切片  │───▶│ 向量化   │
└─────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘
                    │               │               │               │
                    ▼               ▼               ▼               ▼
               PDF/Apache     Apache Tika     滑动窗口       Embedding
               POI/Tika/Poi                    语义边界分割    Model
               OCR(图片)
```

**切片策略**:
- 滑动窗口大小: 512 tokens
- 重叠窗口: 64 tokens
- 优先按段落/标题自然边界切分
- 保持语义完整性，避免句子被截断

### 8.4 钉钉文件同步策略

| 同步方式 | 触发条件 | 说明 |
|----------|----------|------|
| **全量同步** | 首次部署 / 每周日凌晨3点 | 遍历全部知识库文档，重建索引 |
| **增量同步** | 每30分钟检查一次 | 对比文档修改时间，仅更新变更文档 |
| **事件触发** | 钉钉 Webhook 回调 | 文档创建/更新/删除时实时同步 |
| **手动触发** | 管理后台手动操作 | 支持指定文件/知识库空间同步 |

---

## 10. AI Agent 智能体设计

### 9.1 Agent 路由编排

```
用户消息 → [RouterAgent]
               │
    ┌──────────┼──────────┐
    ▼          ▼          ▼
OrderAgent  ProductAgent  ComplaintAgent  ──→ 回退:通用Agent
    │          │              │
    ▼          ▼              ▼
┌─────────────────────────────────────────────┐
│              Tool 工具集                      │
│  · query_order(订单号)                       │
│  · query_logistics(订单号)                   │
│  · query_product(产品名/ID)                  │
│  · search_knowledge(query)                   │
│  · create_complaint_ticket(...)              │
│  · check_return_policy(订单号)               │
└─────────────────────────────────────────────┘
```

### 9.2 Prompt 工程

#### 系统 Prompt 模板

```
你是一个专业的茶具电商客服AI助手，你的职责是帮助客户解决订单相关问题。

## 你的能力
1. 查询订单状态和物流信息
2. 解答产品相关问题（材质、工艺、保养等）
3. 处理退换货、退款申请
4. 处理客户投诉和售后问题
5. 提供茶具使用和保养建议

## 行为准则
- 始终使用礼貌、专业的语气
- 回复简洁明了，避免冗长
- 涉及金额/退款等敏感操作时，明确告知客户流程和时效
- 无法处理时主动建议转接人工客服
- 客户情绪激动时优先安抚情绪

## 知识库信息
{knowledge_context}

## 当前订单信息
{order_context}

## 对话历史摘要
{conversation_summary}

## 当前日期
{current_date}
```

### 9.3 Function Calling / Tool 定义

```json
{
  "tools": [
    {
      "name": "query_order",
      "description": "根据订单号或手机号查询订单信息",
      "parameters": {
        "type": "object",
        "properties": {
          "orderNo": {
            "type": "string",
            "description": "订单号，如TB2026..."
          },
          "phone": {
            "type": "string",
            "description": "收件人手机号"
          }
        }
      }
    },
    {
      "name": "query_logistics",
      "description": "查询订单物流轨迹",
      "parameters": {
        "type": "object",
        "properties": {
          "orderNo": {
            "type": "string",
            "description": "订单号"
          }
        },
        "required": ["orderNo"]
      }
    },
    {
      "name": "search_knowledge",
      "description": "从企业知识库中检索相关信息",
      "parameters": {
        "type": "object",
        "properties": {
          "query": {
            "type": "string",
            "description": "检索查询语句"
          },
          "category": {
            "type": "string",
            "enum": ["product", "policy", "faq", "after_sale"],
            "description": "知识分类"
          }
        },
        "required": ["query"]
      }
    },
    {
      "name": "create_ticket",
      "description": "创建客诉工单",
      "parameters": {
        "type": "object",
        "properties": {
          "complaintType": {
            "type": "string",
            "enum": ["quality", "logistics", "service", "refund", "other"]
          },
          "title": { "type": "string" },
          "description": { "type": "string" },
          "orderNo": { "type": "string" },
          "priority": {
            "type": "string",
            "enum": ["P0", "P1", "P2", "P3"]
          }
        },
        "required": ["complaintType", "title", "description"]
      }
    }
  ]
}
```

---

## 11. Docker 容器化部署

### 10.1 整体部署架构

```
┌─────────────────────────────────────────────────────────────────┐
│                    Docker Host (单机 / 云服务器)                  │
│                                                                 │
│  ┌──────────┐  ┌──────────────┐  ┌──────────────────────────┐  │
│  │  Nginx   │  │ Spring Boot  │  │ Spring Boot × N (集群)   │  │
│  │  :80/443 │  │ App :8080    │  │ App :8081,8082,...       │  │
│  └──────────┘  └──────────────┘  └──────────────────────────┘  │
│                                                                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐   │
│  │  MySQL   │  │  Redis   │  │ RabbitMQ │  │ Elasticsearch│   │
│  │  :3306   │  │  :6379   │  │:5672/15672│ │ :9200/:9300 │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────┘   │
│                                                                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                     │
│  │  MinIO   │  │ Prometheus│  │ Grafana  │                     │
│  │  :9000   │  │:9090     │  │:3000     │                     │
│  └──────────┘  └──────────┘  └──────────┘                     │
│                                                                 │
│  ┌──────────────────────────────────────────┐                   │
│  │        Docker Network: chatbot-net        │                   │
│  └──────────────────────────────────────────┘                   │
└─────────────────────────────────────────────────────────────────┘
```

### 10.2 服务器目录结构

```bash
# 在服务器上创建以下目录结构 (以 /opt/order-chatbot 为例)
mkdir -p /opt/order-chatbot/{nginx/{conf.d,ssl},mysql/init,redis,elasticsearch,app,minio/data,prometheus,grafana/datasources}
```

```
/opt/order-chatbot/
├── docker-compose.yml          # 主编排文件
├── .env                        # 环境变量(含密钥，勿提交Git)
├── nginx/
│   ├── nginx.conf              # Nginx 主配置
│   ├── conf.d/
│   │   └── chatbot.conf        # 站点配置
│   └── ssl/
│       ├── cert.pem            # SSL证书
│       └── key.pem             # SSL私钥
├── mysql/
│   ├── init/
│   │   ├── 01_schema.sql       # 建表语句
│   │   └── 02_init_data.sql    # 初始数据(系统配置/Prompt模板)
│   └── my.cnf                  # MySQL自定义配置
├── redis/
│   └── redis.conf              # Redis配置
├── elasticsearch/
│   └── elasticsearch.yml       # ES配置
├── app/
│   ├── Dockerfile              # 应用镜像构建文件
│   └── order-chatbot.jar       # 应用JAR包
├── prometheus/
│   └── prometheus.yml          # Prometheus采集配置
└── grafana/
    └── datasources/
        └── datasource.yml      # Grafana数据源配置
```

### 10.3 docker-compose.yml (完整编排文件)

```yaml
version: '3.8'

services:
  # ==================== 基础设施 ====================

  mysql:
    image: mysql:8.0.35
    container_name: chatbot-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-Chatbot@2026!}
      MYSQL_DATABASE: order_chatbot
      MYSQL_USER: chatbot
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-Chatbot@2026!}
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./mysql/init:/docker-entrypoint-initdb.d
      - ./mysql/my.cnf:/etc/mysql/conf.d/my.cnf
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
    command: redis-server /usr/local/etc/redis/redis.conf
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
      - ./redis/redis.conf:/usr/local/etc/redis/redis.conf
    networks:
      - chatbot-net
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: chatbot-es
    restart: unless-stopped
    environment:
      - discovery.type=single-node
      - ES_JAVA_OPTS=-Xms1g -Xmx1g
      - xpack.security.enabled=false
      - xpack.security.enrollment.enabled=false
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - es_data:/usr/share/elasticsearch/data
      - ./elasticsearch/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml
    networks:
      - chatbot-net
    ulimits:
      memlock:
        soft: -1
        hard: -1
    healthcheck:
      test: ["CMD-SHELL", "curl -s http://localhost:9200/_cluster/health | grep -q 'green\|yellow'"]
      interval: 10s

  rabbitmq:
    image: rabbitmq:3.13-management-alpine
    container_name: chatbot-rabbitmq
    restart: unless-stopped
    environment:
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_USER:-admin}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASS:-Chatbot@2026!}
      RABBITMQ_DEFAULT_VHOST: /
    ports:
      - "5672:5672"    # AMQP
      - "15672:15672"  # Management UI
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - chatbot-net
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "check_port_connectivity"]
      interval: 10s

  minio:
    image: minio/minio:latest
    container_name: chatbot-minio
    restart: unless-stopped
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: ${MINIO_USER:-admin}
      MINIO_ROOT_PASSWORD: ${MINIO_PASS:-Chatbot@2026!}
    ports:
      - "9000:9000"   # API
      - "9001:9001"   # Console
    volumes:
      - minio_data:/data
    networks:
      - chatbot-net

  # ==================== 业务应用 ====================

  chatbot-app:
    build:
      context: ./app
      dockerfile: Dockerfile
    container_name: chatbot-app
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: docker
      TZ: Asia/Shanghai
      JAVA_OPTS: "-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
    ports:
      - "8080:8080"
    volumes:
      - app_logs:/app/logs
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
      elasticsearch:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    networks:
      - chatbot-net

  # ==================== 反向代理 ====================

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
    depends_on:
      - chatbot-app
    networks:
      - chatbot-net

  # ==================== 监控告警 ====================

  prometheus:
    image: prom/prometheus:latest
    container_name: chatbot-prometheus
    restart: unless-stopped
    ports:
      - "9090:9090"
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=30d'
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
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
      GF_INSTALL_PLUGINS: grafana-piechart-panel
    volumes:
      - grafana_data:/var/lib/grafana
      - ./grafana/datasources:/etc/grafana/provisioning/datasources
    depends_on:
      - prometheus
    networks:
      - chatbot-net

volumes:
  mysql_data:
  redis_data:
  es_data:
  rabbitmq_data:
  minio_data:
  app_logs:
  prometheus_data:
  grafana_data:

networks:
  chatbot-net:
    driver: bridge
```

### 10.4 应用 Dockerfile

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

### 10.5 环境变量配置 (.env)

```env
# ==================== 数据库 ====================
MYSQL_ROOT_PASSWORD=Chatbot@2026!
MYSQL_PASSWORD=Chatbot@2026!

# ==================== 消息队列 ====================
RABBITMQ_USER=admin
RABBITMQ_PASS=Chatbot@2026!

# ==================== 对象存储 ====================
MINIO_USER=admin
MINIO_PASS=Chatbot@2026!

# ==================== 监控面板 ====================
GRAFANA_USER=admin
GRAFANA_PASS=Chatbot@2026!

# ==================== AI模型API密钥 ====================
DEEPSEEK_API_KEY=sk-your-deepseek-api-key-here
QWEN_API_KEY=sk-your-qwen-api-key-here

# ==================== 钉钉应用凭证 ====================
DINGTALK_APP_KEY=dingxxxxxxxxxxxx
DINGTALK_APP_SECRET=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
DINGTALK_CORP_ID=xxxxxxxxxxxx
```

### 10.6 Nginx 配置 (nginx.conf)

```nginx
user nginx;
worker_processes auto;

events {
    worker_connections 2048;
    use epoll;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent"';

    sendfile        on;
    tcp_nopush      on;
    tcp_nodelay     on;
    keepalive_timeout 65;
    client_max_body_size 50m;

    gzip on;
    gzip_min_length 1k;
    gzip_comp_level 6;
    gzip_types text/plain application/json application/javascript text/css;

    include /etc/nginx/conf.d/*.conf;
}
```

### 10.7 Nginx 站点配置 (chatbot.conf)

```nginx
upstream chatbot_backend {
    least_conn;
    server chatbot-app:8080 weight=1 max_fails=3 fail_timeout=30s;
}

server {
    listen 80;
    server_name chatbot.yourcompany.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name chatbot.yourcompany.com;

    ssl_certificate     /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    client_max_body_size 50m;

    # 管理后台
    location /admin/ {
        alias /usr/share/nginx/html/admin/;
        try_files $uri $uri/ /admin/index.html;
    }

    # API 代理
    location /api/ {
        proxy_pass http://chatbot_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        proxy_connect_timeout 30s;
        proxy_read_timeout 120s;
        proxy_send_timeout 30s;
    }

    # SSE (Server-Sent Events) 流式对话
    location /api/v1/chat/stream {
        proxy_pass http://chatbot_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 300s;
        chunked_transfer_encoding on;
    }

    # MinIO 文件访问
    location /files/ {
        proxy_pass http://chatbot-minio:9000/;
        proxy_set_header Host $host;
    }

    # WebSocket (预留)
    location /ws/ {
        proxy_pass http://chatbot_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 86400s;
    }

    # 健康检查端点
    location /health {
        proxy_pass http://chatbot_backend/actuator/health;
        access_log off;
    }
}
```

### 10.8 MySQL 配置 (my.cnf)

```ini
[mysqld]
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
max_connections = 200
innodb_buffer_pool_size = 512M
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
slow_query_log = 1
slow_query_log_file = /var/lib/mysql/slow.log
long_query_time = 2

[mysql]
default-character-set = utf8mb4
```

### 10.9 Redis 配置 (redis.conf)

```
port 6379
bind 0.0.0.0
protected-mode no

maxmemory 512mb
maxmemory-policy allkeys-lru

save 900 1
save 300 10
save 60 10000

dbfilename dump.rdb
dir /data

appendonly yes
appendfsync everysec
```

### 10.10 Elasticsearch 配置 (elasticsearch.yml)

```yaml
cluster.name: chatbot-es-cluster
node.name: chatbot-es-node-1
network.host: 0.0.0.0
http.port: 9200
transport.port: 9300
discovery.type: single-node
xpack.security.enabled: false
path.data: /usr/share/elasticsearch/data
path.logs: /usr/share/elasticsearch/logs

indices.memory.index_buffer_size: 20%
```

### 10.11 Prometheus 配置 (prometheus.yml)

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'chatbot-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['chatbot-app:8080']

  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
```

### 10.12 Spring Boot Docker Profile 配置 (application-docker.yml)

```yaml
spring:
  config:
    activate:
      on-profile: docker

  datasource:
    url: jdbc:mysql://chatbot-mysql:3306/order_chatbot?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: chatbot
    password: ${MYSQL_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000

  data:
    redis:
      host: chatbot-redis
      port: 6379
      password: ${REDIS_PASSWORD:}
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5

  elasticsearch:
    uris: http://chatbot-es:9200

  rabbitmq:
    host: chatbot-rabbitmq
    port: 5672
    username: ${RABBITMQ_USER:admin}
    password: ${RABBITMQ_PASS}

  ai:
    deepseek:
      api-key: ${DEEPSEEK_API_KEY}
      base-url: https://api.deepseek.com
      chat:
        options:
          model: deepseek-chat
          temperature: 0.7
          max-tokens: 2048
    dashscope:
      api-key: ${QWEN_API_KEY}
      chat:
        options:
          model: qwen-max

  servlet:
    multipart:
      max-file-size: 20MB
      max-request-size: 50MB

minio:
  endpoint: http://chatbot-minio:9000
  access-key: ${MINIO_USER:admin}
  secret-key: ${MINIO_PASS}
  bucket: chatbot-files

dingtalk:
  app-key: ${DINGTALK_APP_KEY}
  app-secret: ${DINGTALK_APP_SECRET}
  corp-id: ${DINGTALK_CORP_ID}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,env
  metrics:
    export:
      prometheus:
        enabled: true
  health:
    elasticsearch:
      enabled: true
    redis:
      enabled: true
    rabbit:
      enabled: true

logging:
  file:
    path: /app/logs
  level:
    root: INFO
    com.ai: DEBUG
```

### 10.13 MySQL 初始化脚本 (01_schema.sql)

```sql
-- 创建数据库 (docker-compose environment已创建，此为冗余保障)
CREATE DATABASE IF NOT EXISTS order_chatbot DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE order_chatbot;

-- 会话表
CREATE TABLE IF NOT EXISTS `chat_session` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话唯一标识',
    `platform` VARCHAR(32) NOT NULL COMMENT '平台:tmall/jd/douyin/pdd',
    `platform_user_id` VARCHAR(128) NOT NULL COMMENT '平台用户ID',
    `user_name` VARCHAR(64) COMMENT '用户昵称',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1进行中,2已结束,3已转人工',
    `agent_id` BIGINT COMMENT '转接坐席ID',
    `start_time` DATETIME NOT NULL COMMENT '会话开始时间',
    `end_time` DATETIME COMMENT '会话结束时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_session_id` (`session_id`),
    INDEX `idx_platform_user` (`platform`, `platform_user_id`),
    INDEX `idx_status_time` (`status`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 消息记录表
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `session_id` VARCHAR(64) NOT NULL,
    `message_id` VARCHAR(64) NOT NULL COMMENT '消息唯一ID',
    `role` VARCHAR(16) NOT NULL COMMENT '角色:user/assistant/system',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `intent` VARCHAR(32) COMMENT '识别意图',
    `sentiment` VARCHAR(16) COMMENT '情感:positive/neutral/negative',
    `confidence` DECIMAL(5,4) COMMENT '置信度',
    `rag_docs` JSON COMMENT 'RAG检索引用',
    `message_time` DATETIME NOT NULL COMMENT '消息时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_message_id` (`message_id`),
    INDEX `idx_session_time` (`session_id`, `message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息记录表';

-- 知识文档表
CREATE TABLE IF NOT EXISTS `knowledge_doc` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `doc_id` VARCHAR(64) NOT NULL,
    `title` VARCHAR(256) NOT NULL COMMENT '文档标题',
    `category` VARCHAR(64) NOT NULL COMMENT '分类:product/policy/faq/after_sale',
    `source` VARCHAR(32) NOT NULL COMMENT '来源:dingtalk/upload/manual',
    `dingtalk_file_id` VARCHAR(128) COMMENT '钉钉文件ID',
    `dingtalk_space_id` VARCHAR(128) COMMENT '钉钉空间ID',
    `file_format` VARCHAR(16) COMMENT '文件格式',
    `file_url` VARCHAR(512) COMMENT '文件存储URL',
    `file_size` BIGINT COMMENT '文件大小',
    `chunk_count` INT COMMENT '切片数量',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1正常,0已删除,2同步中',
    `last_sync_time` DATETIME COMMENT '最后同步时间',
    `version` INT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_doc_id` (`doc_id`),
    INDEX `idx_category` (`category`),
    INDEX `idx_source` (`source`),
    INDEX `idx_dingtalk_file` (`dingtalk_file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识文档表';

-- 知识切片表
CREATE TABLE IF NOT EXISTS `knowledge_chunk` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `chunk_id` VARCHAR(64) NOT NULL,
    `doc_id` VARCHAR(64) NOT NULL,
    `chunk_index` INT NOT NULL COMMENT '切片序号',
    `content` TEXT NOT NULL,
    `content_hash` VARCHAR(64) NOT NULL COMMENT '内容哈希',
    `token_count` INT COMMENT 'Token数量',
    `es_index_name` VARCHAR(128) COMMENT 'ES索引名',
    `es_doc_id` VARCHAR(128) COMMENT 'ES文档ID',
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_chunk_id` (`chunk_id`),
    INDEX `idx_doc_id` (`doc_id`),
    INDEX `idx_es` (`es_index_name`, `es_doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识文档切片表';

-- 客诉工单表
CREATE TABLE IF NOT EXISTS `complaint_ticket` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ticket_no` VARCHAR(32) NOT NULL COMMENT '工单编号',
    `session_id` VARCHAR(64) COMMENT '关联会话ID',
    `order_no` VARCHAR(64) COMMENT '关联订单号',
    `customer_name` VARCHAR(64) COMMENT '客户姓名',
    `customer_phone` VARCHAR(20) COMMENT '客户电话',
    `platform` VARCHAR(32) NOT NULL COMMENT '来源平台',
    `complaint_type` VARCHAR(32) NOT NULL COMMENT '客诉类型',
    `complaint_level` VARCHAR(16) NOT NULL COMMENT '优先级:P0/P1/P2/P3',
    `title` VARCHAR(256) NOT NULL COMMENT '客诉标题',
    `description` TEXT COMMENT '客诉描述(AI摘要)',
    `original_content` TEXT COMMENT '原始对话内容',
    `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/IN_PROGRESS/RESOLVED/CLOSED',
    `assignee_id` BIGINT COMMENT '处理人ID',
    `resolution` TEXT COMMENT '处理方案',
    `satisfaction` TINYINT COMMENT '满意度1-5',
    `resolve_time` DATETIME COMMENT '解决时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_ticket_no` (`ticket_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_assignee` (`assignee_id`),
    INDEX `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客诉工单表';

-- 工单流转记录
CREATE TABLE IF NOT EXISTS `ticket_flow_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ticket_no` VARCHAR(32) NOT NULL,
    `from_status` VARCHAR(16) COMMENT '原状态',
    `to_status` VARCHAR(16) NOT NULL COMMENT '新状态',
    `operator_id` BIGINT COMMENT '操作人ID',
    `operator_name` VARCHAR(64) COMMENT '操作人姓名',
    `remark` TEXT COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_ticket_no` (`ticket_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单流转记录表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `sys_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `config_key` VARCHAR(64) NOT NULL,
    `config_value` TEXT NOT NULL,
    `config_type` VARCHAR(16) NOT NULL DEFAULT 'string',
    `description` VARCHAR(256) COMMENT '配置说明',
    `group_name` VARCHAR(32) NOT NULL DEFAULT 'default',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- Prompt模板表
CREATE TABLE IF NOT EXISTS `prompt_template` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `template_key` VARCHAR(64) NOT NULL,
    `template_name` VARCHAR(128) NOT NULL,
    `template_content` TEXT NOT NULL,
    `variables` JSON COMMENT '模板变量定义',
    `model_type` VARCHAR(32) COMMENT '适用模型',
    `version` INT NOT NULL DEFAULT 1,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_template_key` (`template_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt模板表';
```

### 10.14 初始数据脚本 (02_init_data.sql)

```sql
USE order_chatbot;

-- 系统默认配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `description`, `group_name`) VALUES
('chat.max_history_rounds', '10', 'int', '对话历史最大保留轮次', 'chat'),
('chat.confidence_threshold', '0.75', 'float', '意图识别置信度阈值(低于则追问)', 'chat'),
('chat.auto_transfer_threshold', '3', 'int', '连续无法理解的次数触发转人工', 'chat'),
('chat.response_timeout', '30', 'int', '响应超时时间(秒)', 'chat'),
('rag.top_k', '5', 'int', 'RAG检索返回Top K文档片段', 'rag'),
('rag.similarity_threshold', '0.7', 'float', 'RAG相似度阈值', 'rag'),
('rag.chunk_size', '512', 'int', '文档切片大小(tokens)', 'rag'),
('rag.chunk_overlap', '64', 'int', '文档切片重叠大小(tokens)', 'rag');

-- 默认Prompt模板
INSERT INTO `prompt_template` (`template_key`, `template_name`, `template_content`, `variables`, `model_type`) VALUES
('system_prompt', '系统角色Prompt', '你是一个专业的茶具电商客服AI助手...\n\n{knowledge_context}\n\n{order_context}\n\n{conversation_summary}', '["knowledge_context","order_context","conversation_summary"]', 'deepseek-chat'),
('intent_classify', '意图分类Prompt', '请分析以下用户消息的意图，从以下选项中选一个最匹配的...\n\n用户消息: {user_message}', '["user_message"]', 'deepseek-chat'),
('sentiment_analyze', '情感分析Prompt', '分析以下用户消息的情感倾向...\n\n用户消息: {user_message}', '["user_message"]', 'deepseek-chat'),
('complaint_summary', '客诉摘要Prompt', '请根据以下对话内容，生成客诉工单摘要...\n\n{conversation}', '["conversation"]', 'qwen-max');
```

---

## 12. 运维监控方案

### 11.1 监控指标体系

| 类别 | 监控指标 | 采集方式 | 告警阈值 |
|------|----------|----------|----------|
| **系统健康** | CPU 使用率 | Prometheus + Node Exporter | > 80% 持续 5min |
| | 内存使用率 | Prometheus | > 85% |
| | 磁盘使用率 | Prometheus | > 85% |
| | JVM Heap 使用率 | Actuator + Prometheus | > 80% |
| | JVM GC 暂停时间 | Actuator | > 500ms |
| | 线程池活跃数 | Actuator | > 80% 容量 |
| **业务指标** | 会话 QPS | 自定义 Metrics | 偏离基线 ±50% |
| | 平均响应时长 | 自定义 Metrics | > 5s |
| | P99 响应时长 | 自定义 Metrics | > 10s |
| | 意图识别准确率 | 人工标注 + 统计 | < 80% |
| | RAG 召回率 | 评估数据集 | < 70% |
| | 转人工率 | 业务统计 | > 30% |
| **中间件** | MySQL 连接数 | Exporter | > 80% max |
| | Redis 命中率 | Exporter | < 80% |
| | ES 集群健康 | ES API | Yellow/Red |
| | RabbitMQ 消息堆积 | RabbitMQ API | > 1000 |
| **错误告警** | 5xx 错误率 | 日志 + Prometheus | > 1% |
| | LLM API 调用失败率 | 自定义 Metrics | > 5% |
| | 钉钉同步失败次数 | 应用日志 | > 3次/小时 |

### 11.2 日志管理

```
应用日志 → Filebeat → Logstash → Elasticsearch → Kibana
    │                                              │
    │                                              ▼
    │                                      告警通知(钉钉群)
    ▼
ERROR级别日志 → 钉钉群实时推送
```

**日志规范**:
- 每次 API 调用记录: `traceId`, `userId`, `接口`, `耗时`, `结果`
- 每次 LLM 调用记录: `model`, `prompt长度`, `response长度`, `耗时`, `token消耗`
- 每次 RAG 检索记录: `query`, `召回数量`, `Top1分数`, `耗时`

### 11.3 Grafana 仪表盘

**推荐预建面板**:
1. **系统概览**: CPU/内存/磁盘/JVM 核心曲线
2. **业务实时**: 会话QPS、响应时长P50/P99、转人工率
3. **AI 调用**: LLM调用次数、Token消耗、平均耗时、成功率
4. **知识库**: 文档总量、同步状态、RAG检索QPS与命中率
5. **客诉看板**: 待处理工单、当日新增、各状态分布

---

## 13. 分步实施计划

### 12.1 整体里程碑

```
Phase 1 ──────── Phase 2 ──────── Phase 3 ──────── Phase 4
基础设施搭建    核心AI能力      业务功能完善      上线与优化
  (Week 1-2)    (Week 3-5)      (Week 6-8)       (Week 9-10)
```

### 12.2 详细分步操作指南

#### Phase 1: 基础设施搭建 (第1-2周)

**Step 1.1: 服务器准备 (运维)**
```bash
# 1. 准备服务器 (CentOS 7.9+ / Ubuntu 20.04+ / Rocky Linux 8+)
#    推荐配置: 8C16G + 200G SSD
# 2. 安装必要工具
sudo yum install -y curl wget vim git net-tools

# 3. 关闭 SELinux (如果开启)
sudo setenforce 0
sudo sed -i 's/SELINUX=enforcing/SELINUX=disabled/' /etc/selinux/config

# 4. 配置防火墙
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

**Step 1.2: 安装 Docker 环境 (运维)**
```bash
# 1. 卸载旧版本
sudo yum remove docker docker-client docker-client-latest docker-common \
  docker-latest docker-latest-logrotate docker-logrotate docker-engine

# 2. 安装 Docker
curl -fsSL https://get.docker.com | sudo sh

# 3. 启动并设置开机自启
sudo systemctl start docker
sudo systemctl enable docker

# 4. 安装 Docker Compose v2
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" \
  -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 5. 验证安装
docker --version
docker-compose --version

# 6. 配置 Docker 镜像加速 (国内服务器)
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://mirror.ccs.tencentyun.com"],
  "log-driver": "json-file",
  "log-opts": {"max-size": "100m", "max-file": "3"}
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
```

**Step 1.3: 创建项目目录结构 (运维)**
```bash
# 在服务器上执行
sudo mkdir -p /opt/order-chatbot
sudo chown -R $USER:$USER /opt/order-chatbot
cd /opt/order-chatbot

# 创建所有子目录
mkdir -p nginx/{conf.d,ssl}
mkdir -p mysql/init
mkdir -p redis
mkdir -p elasticsearch
mkdir -p app
mkdir -p minio/data
mkdir -p prometheus
mkdir -p grafana/datasources
```

**Step 1.4: 编写配置文件 (开发)**
```bash
# 按照本文档第 10 章各节的配置文件内容，依次创建:
# - docker-compose.yml
# - .env (注意修改API Key等敏感信息)
# - nginx/nginx.conf
# - nginx/conf.d/chatbot.conf
# - mysql/init/01_schema.sql
# - mysql/init/02_init_data.sql
# - mysql/my.cnf
# - redis/redis.conf
# - elasticsearch/elasticsearch.yml
# - prometheus/prometheus.yml
# - grafana/datasources/datasource.yml
```

**Step 1.5: 启动基础设施 (运维)**
```bash
cd /opt/order-chatbot

# 拉取所有镜像 (可提前执行，加快后续启动)
docker-compose pull

# 启动所有基础设施服务 (暂时排除应用)
docker-compose up -d mysql redis elasticsearch rabbitmq minio

# 查看启动状态
docker-compose ps

# 查看日志确认无错误
docker-compose logs -f mysql
```

**Step 1.6: 验证基础设施 (开发/运维)**
```bash
# 1. 验证 MySQL
docker exec -it chatbot-mysql mysql -uchatbot -pChatbot@2026! -e "SHOW DATABASES;"
# 预期输出: 包含 order_chatbot 数据库

# 2. 验证 Redis
docker exec -it chatbot-redis redis-cli PING
# 预期输出: PONG

# 3. 验证 Elasticsearch
curl http://localhost:9200/_cluster/health
# 预期输出: status 为 green 或 yellow

# 4. 验证 RabbitMQ
# 浏览器访问 http://服务器IP:15672
# 用户名: admin / 密码: Chatbot@2026!

# 5. 验证 MinIO
# 浏览器访问 http://服务器IP:9001
# 用户名: admin / 密码: Chatbot@2026!
```

#### Phase 2: 核心 AI 能力开发 (第3-5周)

**Step 2.1: 搭建 Spring Boot 项目骨架 (开发)**
```bash
# 1. 克隆项目模板 (基于现有 order-chatbot 项目扩展)
cd your-dev-machine
git clone <your-git-repo>/order-chatbot.git

# 2. 在 pom.xml 中确认/添加核心依赖:
#    - spring-ai-alibaba-starter-dashscope (已有)
#    - spring-ai-deepseek (已有)
#    - spring-boot-starter-data-redis
#    - spring-boot-starter-data-elasticsearch
#    - spring-boot-starter-amqp (RabbitMQ)
#    - spring-boot-starter-actuator
#    - micrometer-registry-prometheus
#    - dingtalk-open-api (钉钉SDK)
```

**Step 2.2: 实现 LLM 调用层 (开发)**
```
任务清单:
□ 实现 ModelRouter: 根据意图/成本路由到不同模型
□ 实现 LlmClient: 统一封装DeepSeek和通义千问调用
□ 实现 PromptTemplate: 从数据库加载模板，动态渲染
□ 实现调用日志记录与Token用量统计
□ 编写单元测试，模拟各场景LLM调用
```

**Step 2.3: 实现 RAG 检索引擎 (开发)**
```
任务清单:
□ 实现 EmbeddingService: 调用Embedding模型生成向量
□ 实现 ChunkService: 文档智能切片(滑动窗口+语义边界)
□ 实现 VectorSearchService: ES向量检索
□ 实现 HybridSearchService: 向量+关键词混合检索 + RRF融合
□ 实现 RerankService: 重排序
□ 配置ES索引Mapping并完成IK分词器安装
□ 编写检索质量评估脚本
```

**Step 2.4: 实现意图识别与情感分析 (开发)**
```
任务清单:
□ 定义意图枚举: order_query, product_inquiry, after_sale, 
                 complaint, logistics, refund, greeting, other
□ 实现 IntentClassifier: 基于LLM的少样本意图分类
□ 实现 SentimentAnalyzer: 情感分析(正面/中性/负面)
□ 负面情绪自动标记并触发升级流程
□ 编写准确率评估脚本
```

#### Phase 3: 业务功能完善 (第6-8周)

**Step 3.1: 实现会话管理 (开发)**
```
任务清单:
□ 实现 SessionService: 会话创建/查询/关闭
□ 实现 ContextManager: 多轮对话上下文管理(Redis存储)
□ 实现 ConversationMemory: 历史消息摘要压缩
□ 实现 AgentTransferService: 人工转接逻辑+钉钉通知
□ 实现消息记录持久化到MySQL
```

**Step 3.2: 实现 Agent 编排 (开发)**
```
任务清单:
□ 实现 RouterAgent: 主路由Agent，意图分发
□ 实现 OrderAgent: 订单查询Agent + Tool定义
□ 实现 ProductAgent: 产品咨询Agent + RAG检索
□ 实现 ComplaintAgent: 客诉处理Agent + 工单创建
□ 集成 Spring AI Alibaba Agent Framework
□ 实现 Agent 间上下文传递
```

**Step 3.3: 实现知识库管理与钉钉同步 (开发)**
```
任务清单:
□ 实现 DingtalkOpenClient: 封装钉钉OpenAPI调用
   - AccessToken管理(Redis缓存+自动刷新)
   - 知识库空间列表获取
   - 知识节点列表获取
   - 文件下载
□ 实现 DingtalkSyncService:
   - 全量同步(定时任务:@Scheduled cron)
   - 增量同步(每30分钟检查修改时间)
   - Webhook事件监听(文档变更实时同步)
□ 实现 DocumentParser: 多格式解析
   - PDF解析(Apache PDFBox)
   - Word解析(Apache POI)
   - Excel解析(Apache POI)
   - 图片OCR(预留接口)
□ 实现 KnowledgeController: 知识库管理API
□ 实现手动上传/批量导入功能
```

**Step 3.4: 实现客诉工单 (开发)**
```
任务清单:
□ 实现 ComplaintService: 客诉分类与等级判定
□ 实现 TicketService: 工单CRUD
□ 实现 WorkflowService: 工单流转状态机
   - PENDING → IN_PROGRESS → RESOLVED → CLOSED
□ 实现工单自动分配(轮询/负载均衡)
□ 实现处理超时自动催办(钉钉通知)
```

**Step 3.5: 实现数据分析 (开发)**
```
任务清单:
□ 实现 StatisticsService: 实时统计(Redis计数器)
□ 实现 HotTopicService: 热点问题聚类分析
□ 实现 ReportScheduler: 周报/月报定时生成
□ 实现统计数据API
```

#### Phase 4: 部署上线与优化 (第9-10周)

**Step 4.1: 应用打包与镜像构建 (开发/运维)**
```bash
# 1. 在开发机上执行Maven打包
cd order-chatbot
mvn clean package -DskipTests

# 2. 将JAR包复制到部署目录
scp target/order-chatbot-1.0-SNAPSHOT.jar user@server:/opt/order-chatbot/app/order-chatbot.jar

# 3. 构建Docker镜像
cd /opt/order-chatbot
docker-compose build chatbot-app

# 4. 启动全部服务
docker-compose up -d

# 5. 查看启动日志
docker-compose logs -f chatbot-app
```

**Step 4.2: ES 索引初始化 (运维)**
```bash
# 使用 Kibana Dev Tools 或 curl 创建知识库索引
curl -X PUT "http://localhost:9200/kb_chunks_v1" -H 'Content-Type: application/json' -d '{
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
      "title": { "type": "text", "analyzer": "ik_max_word" },
      "category": { "type": "keyword" },
      "content": { "type": "text", "analyzer": "ik_max_word", "search_analyzer": "ik_smart" },
      "content_vector": { "type": "dense_vector", "dims": 1536, "index": true, "similarity": "cosine" },
      "metadata": { "type": "object" },
      "create_time": { "type": "date" }
    }
  }
}'
```

**Step 4.3: SSL 证书配置 (运维)**
```bash
# 方式1: 使用 Let's Encrypt 免费证书
# 安装 certbot
sudo yum install -y certbot

# 申请证书 (需要域名已解析到服务器)
sudo certbot certonly --standalone -d chatbot.yourcompany.com

# 复制证书到 Nginx 目录
sudo cp /etc/letsencrypt/live/chatbot.yourcompany.com/fullchain.pem /opt/order-chatbot/nginx/ssl/cert.pem
sudo cp /etc/letsencrypt/live/chatbot.yourcompany.com/privkey.pem /opt/order-chatbot/nginx/ssl/key.pem

# 设置自动续期 cron
echo "0 3 * * * certbot renew --quiet --post-hook 'docker restart chatbot-nginx'" | sudo crontab -
```

**Step 4.4: 钉钉应用配置 (开发/管理员)**
```
操作步骤:
1. 登录钉钉开放平台 → 应用开发 → 企业内部应用
2. 找到已创建的应用，进入"应用功能" → "机器人"
3. 配置机器人消息接收地址(Webhook URL):
   https://chatbot.yourcompany.com/api/v1/dingtalk/webhook
4. 在"权限管理"中确认已开启:
   □ 企业文件 - 读取
   □ 知识库 - 读取/写入
   □ 文档 - 读取
   □ 通讯录 - 读取
5. 发布应用，等待管理员审批通过
```

**Step 4.5: 首次知识库同步 (运维+业务)**
```bash
# 通过API触发首次全量同步
curl -X POST http://localhost:8080/api/v1/knowledge/sync-dingtalk \
  -H 'Content-Type: application/json' \
  -d '{"mode": "full", "spaces": ["all"]}'

# 查看同步日志
docker-compose logs -f chatbot-app | grep "DingtalkSync"

# 同步完成后验证知识库
curl http://localhost:8080/api/v1/knowledge/search?q=紫砂壶保养
```

**Step 4.6: 冒烟测试 (QA)**
```
测试场景清单:
□ 1. 简单问候: "你好" → AI正确回复欢迎语
□ 2. 订单查询: "帮我查下TB20260528xxxx" → 返回订单状态
□ 3. 物流查询: "我的快递到哪了" → 返回物流轨迹
□ 4. 产品咨询: "这个紫砂壶是什么泥料" → 从知识库检索回答
□ 5. 退换货: "我要退货" → 引导退换货流程
□ 6. 客诉处理: "你们的壶有质量问题" → 创建工单+钉钉通知
□ 7. 多轮对话: 连续3轮上下文测试
□ 8. 转人工: "帮我转人工客服" → 触发转接流程
□ 9. 流式响应: SSE接口是否正常推送
□ 10. 并发压测: Jmeter 500并发，观察系统表现
```

### 12.3 后续迭代规划

| 版本 | 时间 | 功能 |
|------|------|------|
| v1.1 | 上线后2周 | 多平台消息网关对接(先对接天猫/京东) |
| v1.2 | 上线后4周 | 管理后台(Web UI) + 数据看板 |
| v1.3 | 上线后6周 | 抖音/拼多多平台对接 + 商品推荐能力 |
| v2.0 | 上线后3月 | 主动营销(促销推送/复购提醒) + 语音接入 + 大知识库升级Milvus |

---

## 13. 附录

### 13.1 常见问题排查

**Q1: Docker 容器启动失败**
```bash
# 查看具体容器日志
docker-compose logs [service-name]

# 常见原因:
# - 端口冲突: 修改 docker-compose.yml 中的端口映射
# - 内存不足: ES 默认需要1G内存,可调小ES_JAVA_OPTS
# - 权限问题: chown -R 1000:1000 /opt/order-chatbot/elasticsearch/data
```

**Q2: 钉钉同步失败**
```bash
# 1. 检查AppKey/AppSecret是否正确
curl https://oapi.dingtalk.com/gettoken?appkey=YOUR_KEY&appsecret=YOUR_SECRET

# 2. 检查应用权限是否审批通过
# 3. 确认知识库空间ID是否正确
# 4. 查看应用日志中的详细错误
docker-compose logs chatbot-app | grep -i "dingtalk\|error"
```

**Q3: ES 向量检索返回空**
```bash
# 1. 确认索引存在
curl http://localhost:9200/_cat/indices

# 2. 确认文档已写入
curl http://localhost:9200/kb_chunks_v1/_count

# 3. 确认vector字段有值
curl http://localhost:9200/kb_chunks_v1/_search -H 'Content-Type: application/json' -d '{"query":{"exists":{"field":"content_vector"}}}'
```

### 13.2 安全加固清单

| 项目 | 操作 | 优先级 |
|------|------|--------|
| MySQL root密码 | 修改为强密码 | P0 |
| Redis密码 | 设置requirepass | P0 |
| .env文件权限 | `chmod 600 .env` | P0 |
| API鉴权 | 所有API接口启用JWT/API Key | P0 |
| HTTPS | 生产环境强制HTTPS | P0 |
| Docker daemon | 启用TLS认证 | P1 |
| 敏感数据脱敏 | 手机号/地址等在日志中脱敏 | P1 |
| 防火墙 | 仅开放80/443端口,内部端口不对外 | P1 |
| 定期备份 | MySQL每日自动备份 | P1 |
| SQL注入 | 使用参数化查询,MyBatis #{} | P0 |

### 13.3 关键参考文档

| 文档 | 链接 |
|------|------|
| Spring AI 官方文档 | https://docs.spring.io/spring-ai/reference/ |
| Spring AI Alibaba | https://java2ai.com/ |
| DeepSeek API 文档 | https://platform.deepseek.com/api-docs/ |
| 通义千问 API 文档 | https://help.aliyun.com/zh/dashscope/ |
| 钉钉开放平台 | https://open.dingtalk.com/ |
| Elasticsearch 向量检索 | https://www.elastic.co/guide/en/elasticsearch/reference/current/knn-search.html |
| Docker Compose 文档 | https://docs.docker.com/compose/ |

---

> **文档维护**: 本文档随系统迭代持续更新，最新版本请以 Git 仓库为准。  
> **技术负责人**: ___________  
> **审核人**: ___________  
> **批准人**: ___________
