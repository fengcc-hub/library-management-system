# 图书馆管理系统 (Library Management System)
> 本项目使用AI辅助编码，**全部业务需求、架构设计、权限逻辑、缓存事务、代码校验、Bug调试均由本人独立完成**；AI仅用于代码片段生成、工具类简化，所有逻辑经过人工审核、重构、自测，完整理解每一行代码实现原理。
基于 **Spring Boot + MyBatis-Plus + MySQL + Redis + Vue.js + Element UI + ECharts** 的前后端分离图书馆管理系统。

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 后端框架 |
| MyBatis-Plus | 3.5.3.1 | ORM框架，简化CRUD |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 6.0+ | 缓存（热门图书、登录状态） |
| Spring Security | - | 安全框架 |
| JWT (jjwt) | 0.11.5 | 身份认证 |
| Druid | 1.2.20 | 数据库连接池 |
| Hutool | 5.8.22 | 工具库 |
| Lombok | - | 简化代码 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 2.7.14 | 前端框架 |
| Element UI | 2.15.14 | UI组件库 |
| ECharts | 5.4.3 | 数据可视化 |
| Axios | 0.27.2 | HTTP请求 |
| Vue Router | 3.6.5 | 路由管理 |
| Vuex | 3.6.2 | 状态管理 |

## 功能模块

### 1. 认证与权限 (RBAC)
- JWT Token 身份认证
- 三种角色：管理员(ADMIN)、图书管理员(LIBRARIAN)、读者(READER)
- 接口级权限控制 (`@PreAuthorize`)
- Redis 管理 Token 会话

### 2. 图书管理
- 图书CRUD（书名、ISBN、作者、出版社、分类、定价、库存等）
- 分类管理（树形结构，支持多级分类）
- 热门图书缓存（Redis，30分钟TTL）
- 库存原子操作（SQL级别扣减/回补，防止超借）

### 3. 借阅管理（核心业务）
- **借书**：校验借阅上限(5本) → 检查逾期/罚款 → 原子扣减库存 → 创建借阅记录
- **还书**：归还 → 回补库存 → 计算逾期罚款(0.5元/天) → 更新记录
- **续借**：最多续借2次，每次延长14天
- **罚款**：逾期自动计算，支持在线缴纳
- **逾期检查**：定时任务批量标记逾期记录
- 所有借阅/归还操作使用 `@Transactional` 保证数据一致性

### 4. 读者管理
- 读者CRUD、重置密码、冻结/解冻
- 读者证号自动生成

### 5. 数据统计 (ECharts)
- 首页概览仪表盘
- 月度/日度借阅趋势图（折线图）
- 馆藏分类分布（饼图）
- 热门借阅排行（柱状图）
- 读者活跃度排行

## 项目结构

```
librarianShip/
├── backend/                          # 后端 Spring Boot 项目
│   ├── pom.xml                        # Maven 依赖
│   ├── src/main/resources/
│   │   ├── application.yml            # 配置文件
│   │   ├── mapper/                    # MyBatis XML
│   │   │   ├── UserMapper.xml
│   │   │   ├── BookMapper.xml
│   │   │   ├── CategoryMapper.xml
│   │   │   └── BorrowRecordMapper.xml
│   │   └── sql/
│   │       └── init.sql               # 数据库初始化脚本
│   └── src/main/java/com/library/
│       ├── LibraryApplication.java    # 启动类
│       ├── config/                    # 配置类
│       │   ├── RedisConfig.java       # Redis序列化配置
│       │   ├── MybatisPlusConfig.java # 分页+自动填充
│       │   └── CorsConfig.java        # 跨域配置
│       ├── common/                    # 通用类
│       │   ├── Result.java            # 统一响应
│       │   ├── ResultCode.java       # 状态码枚举
│       │   └── PageResult.java       # 分页结果
│       ├── security/                  # 安全模块
│       │   ├── JwtUtils.java          # JWT工具
│       │   ├── JwtAuthenticationFilter.java  # JWT过滤器
│       │   ├── SecurityConfig.java    # Security配置
│       │   └── UserDetailsServiceImpl.java   # 用户认证
│       ├── entity/                    # 实体类
│       │   ├── User.java
│       │   ├── Book.java
│       │   ├── Category.java
│       │   ├── BorrowRecord.java
│       │   └── Role.java
│       ├── mapper/                    # Mapper接口
│       │   ├── UserMapper.java
│       │   ├── BookMapper.java
│       │   ├── CategoryMapper.java
│       │   ├── BorrowRecordMapper.java
│       │   └── RoleMapper.java
│       ├── service/                   # 业务层
│       │   ├── UserService.java
│       │   ├── BookService.java
│       │   ├── CategoryService.java
│       │   ├── BorrowService.java
│       │   ├── StatisticsService.java
│       │   └── impl/                  # 实现类
│       ├── controller/                # 控制层
│       │   ├── AuthController.java
│       │   ├── UserController.java
│       │   ├── BookController.java
│       │   ├── CategoryController.java
│       │   ├── BorrowController.java
│       │   └── StatisticsController.java
│       └── exception/                 # 异常处理
│           ├── BusinessException.java
│           └── GlobalExceptionHandler.java
│
├── frontend/                          # 前端 Vue.js 项目
│   ├── package.json
│   ├── vue.config.js
│   ├── public/
│   │   └── index.html
│   └── src/
│       ├── main.js                    # 入口文件
│       ├── App.vue
│       ├── router/index.js            # 路由配置
│       ├── store/index.js             # Vuex 状态管理
│       ├── utils/request.js           # Axios 封装（JWT拦截器）
│       ├── api/                       # API 接口
│       │   ├── auth.js
│       │   ├── book.js
│       │   ├── category.js
│       │   ├── borrow.js
│       │   ├── user.js
│       │   └── statistics.js
│       ├── components/layout/
│       │   └── Layout.vue             # 主布局（侧边栏+头部）
│       ├── views/                     # 页面
│       │   ├── Login.vue              # 登录页
│       │   ├── Dashboard.vue          # 首页仪表盘
│       │   ├── BookManage.vue         # 图书管理
│       │   ├── CategoryManage.vue     # 分类管理
│       │   ├── ReaderManage.vue       # 读者管理
│       │   ├── BorrowManage.vue       # 借阅管理
│       │   ├── MyBorrows.vue          # 我的借阅
│       │   └── Statistics.vue         # 数据统计
│       └── assets/styles/global.scss  # 全局样式
│
└── README.md
```

## 快速启动

### 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Node.js 16+

### 1. 初始化数据库
```bash
# 登录 MySQL，执行初始化脚本
mysql -u root -p < backend/src/main/resources/sql/init.sql
```

### 2. 启动 Redis
```bash
redis-server
```

### 3. 启动后端
```bash
cd backend
# 修改 application.yml 中的数据库和 Redis 连接信息
mvn clean install
mvn spring-boot:run
# 后端启动在 http://localhost:8080/api
```

### 4. 启动前端
```bash
cd frontend
npm install
npm run serve
# 前端启动在 http://localhost:8081
```

### 5. 访问系统
浏览器打开 `http://localhost:8081`

## 测试账号

| 角色 | 用户名 | 密码 | 权限 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 全部功能 |
| 图书管理员 | librarian | 123456 | 图书/借阅管理 |
| 读者 | reader01 | 123456 | 借阅/查看 |

## API 接口概览

### 认证 `/api/auth`
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /auth/login | 登录 | 公开 |
| POST | /auth/register | 注册 | 公开 |
| GET | /auth/info | 当前用户 | 已认证 |
| POST | /auth/logout | 退出 | 已认证 |

### 用户管理 `/api/users`
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /users/page | 用户列表 | ADMIN |
| GET | /users/{id} | 用户详情 | ADMIN |
| POST | /users | 添加 | ADMIN |
| PUT | /users | 修改 | ADMIN |
| DELETE | /users/{id} | 删除 | ADMIN |
| PUT | /users/{id}/password | 重置密码 | ADMIN |
| GET | /users/readers | 读者列表 | 已认证 |

### 图书管理 `/api/books`
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /books/page | 分页查询 | 已认证 |
| GET | /books/{id} | 详情 | 已认证 |
| POST | /books | 添加 | ADMIN/LIBRARIAN |
| PUT | /books | 修改 | ADMIN/LIBRARIAN |
| DELETE | /books/{id} | 删除 | ADMIN/LIBRARIAN |
| GET | /books/hot | 热门图书 | 已认证 |

### 借阅管理 `/api/borrow`
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /borrow/borrow | 借书 | 已认证 |
| PUT | /borrow/return/{id} | 还书 | ADMIN/LIBRARIAN |
| PUT | /borrow/renew/{id} | 续借 | 已认证 |
| PUT | /borrow/pay-fine/{id} | 缴罚款 | 已认证 |
| GET | /borrow/page | 借阅列表 | 已认证 |
| GET | /borrow/active/{userId} | 在借列表 | 已认证 |
| POST | /borrow/check-overdue | 检查逾期 | ADMIN/LIBRARIAN |

### 数据统计 `/api/statistics`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /statistics/overview | 概览数据 |
| GET | /statistics/borrow/monthly | 月度趋势 |
| GET | /statistics/borrow/daily | 日度趋势 |
| GET | /statistics/category-distribution | 分类分布 |
| GET | /statistics/top-books | 热门排行 |
| GET | /statistics/reader-ranking | 读者排行 |

## 关键设计说明

### 事务处理
借阅核心操作（借书/还书）使用 `@Transactional(rollbackFor = Exception.class)`，确保：
- 借书：库存扣减 + 借阅记录创建 原子性
- 还书：记录更新 + 库存回补 + 罚款计算 原子性
- 任一步骤失败，全部回滚，避免数据不一致

### Redis 缓存策略
- **热门图书**：`book:hot:` 键，30分钟TTL，查询时先查缓存
- **用户Token**：`user:token:{username}` 键，与JWT同步过期
- 缓存失效：图书增删改时自动清除热门图书缓存

### 库存安全
使用SQL原子操作防止超借：
```sql
UPDATE book SET available_count = available_count - 1
WHERE id = ? AND available_count > 0
-- 返回0表示库存不足，事务回滚
```

### 罚款规则
- 借期：14天
- 续借：最多2次，每次延长14天
- 逾期罚款：0.5元/天
- 罚款状态：0无罚款 → 1待缴 → 2已缴
