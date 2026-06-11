# Test Management System

面向测开课程和面试演示的测试管理系统，包含：

- 测试用例 CRUD、搜索、筛选、分页和接口冒烟
- JUnit 5、AssertJ、MockMvc、JSON/CSV/YAML 数据驱动测试
- Playwright Swagger UI 自动化
- Testcontainers MySQL + Redis 多容器集成测试
- JaCoCo 80% 覆盖率门禁、Allure 报告和 GitHub Actions

## 环境要求

- JDK 17
- Maven 3.9+
- Docker Desktop：仅多容器测试需要

首次执行 UI 测试前安装 Chromium：

```powershell
mvn -q test-compile org.codehaus.mojo:exec-maven-plugin:3.5.0:java "-Dexec.mainClass=com.microsoft.playwright.CLI" "-Dexec.classpathScope=test" "-Dexec.args=install chromium"
```

## 快速启动

```powershell
mvn spring-boot:run
```

本地默认使用 H2 内存数据库，无需安装 MySQL。启动后访问：

- 系统首页：<http://localhost:8080/>
- Swagger：<http://localhost:8080/swagger-ui/index.html>
- H2 Console：<http://localhost:8080/h2-console>

构建并运行 JAR：

```powershell
mvn -q clean package
java -jar target/test-management-1.0-SNAPSHOT.jar
```

## 核心命令

```powershell
# 单元测试
mvn -q "-Dtest=TestCaseServiceTest,ResultTest" test

# 接口与数据驱动测试
mvn -q "-Dtest=TestCaseControllerTest,TestCaseDataDrivenTest" test

# UI 测试：测试会自行启动随机端口服务
mvn -q "-Dtest=SwaggerUITest" test

# MySQL + Redis 多容器测试
powershell -ExecutionPolicy Bypass -File .\run-tests.ps1

# 全量测试、JaCoCo 报告和 80% 门禁
mvn -q clean verify

# 根据测试结果生成 Allure HTML 报告
mvn -q allure:report
```

报告位置：

- JaCoCo：`target/site/jacoco/index.html`
- Allure：`target/site/allure-maven-plugin/index.html`

完整下载、运行、测试、MySQL 和面试演示步骤见
[作品使用操作流程](docs/作品使用操作流程.md)。
