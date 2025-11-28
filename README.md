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

- `javafx-sdk`：已经嵌入的 JavaFX SDK（来自 `D:\javafx-sdk-21.0.9`）。
- `run-expense-manager.bat`：用于启动程序的脚本，会自动把 `javafx-sdk\lib` 加入模块路径。

```powershell
.\run-expense-manager.bat
```

无需安装 JavaFX 的用户即可使用。如果需要更新 JavaFX 版本，只需用新的 SDK 覆盖 `dist\javafx-sdk` 并重新分发。