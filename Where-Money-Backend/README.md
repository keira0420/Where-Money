# Where Money - 个人财务管理系统后端

## 项目简介

Where Money 是一个功能强大的个人财务管理系统，旨在帮助用户轻松管理个人财务，包括资产、账单、预算等方面的管理和统计分析。

## 功能特性

### 核心功能

- **资产管理**：支持多种资产类型的管理，包括银行账户、现金、投资等
- **账单管理**：支持收入、支出、转账、退款等多种账单类型的记录和管理
- **预算管理**：支持为不同分类设置预算，并跟踪预算使用情况
- **统计分析**：提供多种维度的财务统计分析，包括月度、分类等维度
- **分类管理**：支持自定义收入和支出分类
- **账本管理**：支持多账本管理，方便用户对不同场景的财务进行分离管理

### 技术特性

- **RESTful API**：提供标准化的RESTful API接口
- **JWT认证**：使用JWT进行用户认证和授权
- **Redis缓存**：使用Redis缓存提升系统性能
- **数据加密**：对敏感数据进行加密存储
- **文件上传**：支持上传账单相关的图片
- **多环境配置**：支持不同环境的配置管理

## 技术栈

### 后端技术

- **Java 21+**：核心开发语言
- **Spring Boot 3.3+**：应用框架
- **MyBatis**：ORM 框架
- **MySQL**：关系型数据库
- **Redis**：缓存数据库
- **JWT**：认证框架
- **Jasypt**：配置文件加密

### 项目结构

```
src/
├── main/
│   ├── java/shuhuai/wheremoney/
│   │   ├── controller/      # 控制器层
│   │   ├── entity/          # 实体类
│   │   ├── mapper/          # MyBatis 映射器
│   │   ├── service/         # 服务层
│   │   ├── response/        # 响应 DTO
│   │   ├── utils/           # 工具类
│   │   ├── type/            # 类型定义
│   │   ├── excep/           # 异常处理
│   │   └── WhereMoneyApplication.java  # 应用入口
│   └── resources/
│       ├── mapper/          # MyBatis XML 映射文件
│       ├── application.properties  # 应用配置
│       └── schema.sql       # 数据库初始化脚本
└── test/                    # 测试代码
```

## 快速开始

### 环境要求

- JDK 11或更高版本
- MySQL 8.0 或更高版本
- Redis 5.0 或更高版本
- Maven 3.3 或更高版本

### 安装部署

1. **克隆项目**
   ```bash
   git clone https://github.com/yourusername/where-money-backend.git
   cd where-money-backend
   ```

2. **配置数据库**
    - 创建数据库 `where_money`
    - 修改 `src/main/resources/application.properties` 中的数据库连接配置

3. **构建项目**
   ```bash
   mvn clean package -DskipTests
   ```

4. **运行项目**
   ```bash
   java -jar target/where-money-backend-1.0.0.jar
   ```

### 开发环境运行

1. **导入IDE**
    - 使用 IntelliJ IDEA 或 Eclipse 导入项目
    - 确保项目使用 JDK 17 或更高版本

2. **配置运行参数**
    - 在 IDE 中配置 Spring Boot 应用运行参数
    - 确保数据库和 Redis 服务已启动

3. **启动应用**
    - 运行 `WhereMoneyApplication.java` 类的 `main` 方法
    - 应用将在 `http://localhost:5050` 上运行

## API文档

### 认证API

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 资产API

- `GET /api/asset` - 获取所有资产
- `POST /api/asset` - 创建资产
- `PUT /api/asset/{id}` - 更新资产
- `DELETE /api/asset/{id}` - 删除资产

### 账单API

- `GET /api/bill` - 获取账单列表
- `POST /api/bill/income` - 记录收入账单
- `POST /api/bill/pay` - 记录支出账单
- `POST /api/bill/transfer` - 记录转账账单
- `POST /api/bill/refund` - 记录退款账单
- `PUT /api/bill/{id}` - 更新账单
- `DELETE /api/bill/{id}` - 删除账单

### 预算API

- `GET /api/budget` - 获取预算列表
- `POST /api/budget` - 创建预算
- `PUT /api/budget/{id}` - 更新预算
- `DELETE /api/budget/{id}` - 删除预算

### 分类API

- `GET /api/category` - 获取分类列表
- `POST /api/category` - 创建分类
- `PUT /api/category/{id}` - 更新分类
- `DELETE /api/category/{id}` - 删除分类

### 账本API

- `GET /api/book` - 获取账本列表
- `POST /api/book` - 创建账本
- `PUT /api/book/{id}` - 更新账本
- `DELETE /api/book/{id}` - 删除账本

## 数据库设计

### 核心表结构

- **user** - 用户表
- **book** - 账本表
- **asset** - 资产表
- **bill_category** - 账单分类表
- **income_bill** - 收入账单表
- **pay_bill** - 支出账单表
- **transfer_bill** - 转账账单表
- **refund_bill** - 退款账单表
- **budget** - 预算表

### 数据库初始化

项目启动时会自动执行 `src/main/resources/schema.sql` 文件进行数据库初始化。

## 配置说明

### 核心配置项

- **数据库配置**：`spring.datasource.*` 相关配置
- **Redis 配置**：`spring.data.redis.*` 相关配置
- **Token 配置**：`token.*` 相关配置
- **Redis 缓存过期时间**：`redis.*.expire` 相关配置
- **文件上传配置**：`spring.servlet.multipart.*` 相关配置

### 安全配置

- 使用 Jasypt 对敏感配置进行加密，如数据库密码
- 使用 JWT 进行用户认证，确保 API 安全

## 开发规范

### 代码规范

- 遵循 Java 代码规范，使用驼峰命名法
- 类名使用大驼峰命名，方法和变量使用小驼峰命名
- 常量使用全大写，单词间用下划线分隔
- 方法和类添加适当的注释

### 数据库规范

- 表名使用小写，单词间用下划线分隔
- 字段名使用小写，单词间用下划线分隔
- 主键使用自增整数类型
- 外键使用对应表的主键类型

## 监控与维护

### 日志管理

- 使用 Spring Boot 默认的日志框架
- 日志级别可在配置文件中调整

### 性能监控

- 使用 Redis 缓存提升系统性能
- 对关键接口进行性能优化

### 错误处理

- 统一的异常处理机制
- 详细的错误日志记录

## 贡献指南

### 开发流程

1. Fork 项目仓库
2. 创建功能分支
3. 提交代码
4. 创建 Pull Request

### 代码规范

- 遵循项目现有的代码风格和规范
- 添加适当的注释和文档
- 确保代码通过单元测试