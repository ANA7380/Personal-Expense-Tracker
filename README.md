# Personal-Expense-Tracker

- 软件目前只支持中文，后期可能会加入多语言支持
- 软件使用MySQL数据库，请确保在使用前已正确安装和配置MySQL：
    - 数据库配置：
```
CREATE DATABASE accounting_db;
USE accounting_db;
CREATE TABLE transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,  -- 描述，如“买菜”
    amount DECIMAL(10, 2) NOT NULL,     -- 金额
    date DATE NOT NULL,                 -- 日期
    category VARCHAR(50),               -- 类别，如“收入”或“支出-食品”
    type ENUM('INCOME', 'EXPENSE') NOT NULL  -- 类型：收入或支出
);
```

## 运行已打包程序

项目根目录包含以下内容：

- `ExpenseManager.jar`：当前已打包好的应用。
- `javafx-sdk`：已经嵌入的 JavaFX SDK（来自 `D:\javafx-sdk-21.0.9`）。
- `run-expense-manager.bat`：用于启动程序的脚本，会自动把 `javafx-sdk\lib` 加入模块路径。

```powershell
.\run-expense-manager.bat
```

无需安装 JavaFX 的用户即可使用。如果需要更新 JavaFX 版本，只需用新的 SDK 覆盖 `javafx-sdk` 并重新分发。

## 自定义数据库配置并重新打包

由于 `app.DatabaseConnection` 中的数据库 URL、用户名、密码被硬编码进 Jar，其他用户在运行前需要按以下步骤修改并重新构建：

1. 编辑 `src/main/java/app/DatabaseConnection.java`，用自己的 MySQL 地址、用户名、密码替换 `URL`、`USER`、`PASSWORD` 常量。
2. 重新打包 Jar：
   - 如果安装了 Maven：执行 `mvn clean package`，生成的文件位于 `target/ExpenseManager-1.0-SNAPSHOT.jar`。
   - 如果使用 IntelliJ IDEA：`Build > Build Artifacts > ExpenseManager.jar > Build`，生成的文件位于 `out/artifacts/ExpenseManager_jar/ExpenseManager.jar`。
3. 将生成的 Jar 复制/重命名为项目根目录下的 `ExpenseManager.jar`（覆盖旧文件），保持 `javafx-sdk` 与 `run-expense-manager.bat` 同目录。

这样每位用户只需根据自己的数据库重新构建一次，即可使用同一个启动脚本运行应用。