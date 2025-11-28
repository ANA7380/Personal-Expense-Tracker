package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/accounting_db?useSSL=false";
    private static final String USER = "root";  // 你的 MySQL 用户名
    private static final String PASSWORD = "你的密码";  // 你的密码

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
