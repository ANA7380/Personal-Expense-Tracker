package app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class Controller {
    // 支出相关字段
    @FXML private TextField expenseDescField, expenseAmountField, expenseCategoryField;
    @FXML private DatePicker expenseDatePicker;
    
    // 收入相关字段
    @FXML private TextField incomeDescField, incomeAmountField, incomeCategoryField;
    @FXML private DatePicker incomeDatePicker;
    
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, Integer> idCol;
    @FXML private TableColumn<Transaction, String> descCol, categoryCol, typeCol;
    @FXML private TableColumn<Transaction, Double> amountCol;
    @FXML private TableColumn<Transaction, LocalDate> dateCol;

    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    public void initialize() {
        // 设置默认日期为今天
        expenseDatePicker.setValue(LocalDate.now());
        incomeDatePicker.setValue(LocalDate.now());
        
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        transactionTable.setItems(transactions);
        loadTransactions();  // 加载数据
    }

    @FXML
    private void addExpense(ActionEvent event) {
        addTransaction(expenseDescField, expenseAmountField, expenseDatePicker, expenseCategoryField, "EXPENSE");
    }
    
    @FXML
    private void addIncome(ActionEvent event) {
        addTransaction(incomeDescField, incomeAmountField, incomeDatePicker, incomeCategoryField, "INCOME");
    }
    
    private void addTransaction(TextField descField, TextField amountField, DatePicker datePicker, TextField categoryField, String dbType) {
        String desc = descField.getText();
        // 如果描述为空，设置为"-"
        if (desc == null || desc.trim().isEmpty()) {
            desc = "-";
        } else {
            desc = desc.trim();
        }
        
        String amountText = amountField.getText();
        if (amountText == null || amountText.trim().isEmpty()) {
            showAlert("错误", "请输入金额");
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showAlert("错误", "金额格式不正确");
            return;
        }
        
        LocalDate date = datePicker.getValue();
        if (date == null) {
            showAlert("错误", "请选择日期");
            return;
        }
        
        String category = categoryField.getText();

        String sql = "INSERT INTO transactions (description, amount, date, category, type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, desc);
            pstmt.setDouble(2, amount);
            pstmt.setDate(3, Date.valueOf(date));
            pstmt.setString(4, category);
            pstmt.setString(5, dbType);
            pstmt.executeUpdate();
            
            // 清空输入框
            descField.clear();
            amountField.clear();
            categoryField.clear();
            datePicker.setValue(LocalDate.now());
            
            loadTransactions();  // 刷新表格
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("错误", "保存失败: " + e.getMessage());
        }
    }

    @FXML
    private void deleteExpense(ActionEvent event) {
        deleteTransaction("支出");
    }
    
    @FXML
    private void deleteIncome(ActionEvent event) {
        deleteTransaction("收入");
    }
    
    private void deleteTransaction(String type) {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择要删除的记录");
            return;
        }
        
        // 检查选中的记录类型是否匹配
        String selectedType = selected.getType();
        if (!selectedType.equals(type)) {
            showAlert("错误", "请选择" + type + "类型的记录");
            return;
        }
        
        String sql = "DELETE FROM transactions WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selected.getId());
            pstmt.executeUpdate();
            loadTransactions();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("错误", "删除失败: " + e.getMessage());
        }
    }

    private void loadTransactions() {
        transactions.clear();
        String sql = "SELECT * FROM transactions";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // 将数据库中的英文转换为中文显示
                String dbType = rs.getString("type");
                String displayType = dbType.equals("INCOME") ? "收入" : "支出";
                transactions.add(new Transaction(
                        rs.getInt("id"),
                        rs.getString("description"),
                        rs.getDouble("amount"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("category"),
                        displayType
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void generateReport(ActionEvent event) {
        double totalIncome = 0, totalExpense = 0;
        
        // 使用英文查询，因为数据库中存储的是英文
        String incomeSql = "SELECT SUM(amount) as total FROM transactions WHERE type='INCOME'";
        String expenseSql = "SELECT SUM(amount) as total FROM transactions WHERE type='EXPENSE'";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // 查询总收入
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(incomeSql)) {
                if (rs.next()) {
                    totalIncome = rs.getDouble("total");
                    if (rs.wasNull()) totalIncome = 0;
                }
            }
            
            // 查询总支出
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(expenseSql)) {
                if (rs.next()) {
                    totalExpense = rs.getDouble("total");
                    if (rs.wasNull()) totalExpense = 0;
                }
            }
            
            double balance = totalIncome - totalExpense;
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("财务报表");
            alert.setHeaderText("收支统计");
            alert.setContentText(String.format("总收入: ¥%.2f\n总支出: ¥%.2f\n余额: ¥%.2f", 
                totalIncome, totalExpense, balance));
            alert.setResizable(true);
            alert.getDialogPane().setPrefWidth(300);
            alert.showAndWait();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("错误", "生成报表失败: " + e.getMessage());
        }
    }
    
    @FXML
    private void exportToCSV(ActionEvent event) {
        // 创建对话框让用户选择导出范围
        Dialog<ExportOption> dialog = new Dialog<>();
        dialog.setTitle("导出CSV");
        dialog.setHeaderText("请选择要导出的数据范围");
        
        // 创建对话框内容
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));
        
        RadioButton allData = new RadioButton("导出所有数据");
        RadioButton selectMonth = new RadioButton("选择年月");
        ToggleGroup group = new ToggleGroup();
        allData.setToggleGroup(group);
        selectMonth.setToggleGroup(group);
        allData.setSelected(true);
        
        // 年份和月份选择
        ComboBox<Integer> yearCombo = new ComboBox<>();
        ComboBox<Integer> monthCombo = new ComboBox<>();
        
        // 填充年份（从当前年份往前10年到往后1年）
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 10; i <= currentYear + 1; i++) {
            yearCombo.getItems().add(i);
        }
        yearCombo.setValue(currentYear);
        
        // 填充月份
        for (int i = 1; i <= 12; i++) {
            monthCombo.getItems().add(i);
        }
        monthCombo.setValue(LocalDate.now().getMonthValue());
        
        // 禁用年月选择（默认选择全部）
        yearCombo.setDisable(true);
        monthCombo.setDisable(true);
        
        // 切换选项时启用/禁用年月选择
        allData.setOnAction(e -> {
            yearCombo.setDisable(true);
            monthCombo.setDisable(true);
        });
        selectMonth.setOnAction(e -> {
            yearCombo.setDisable(false);
            monthCombo.setDisable(false);
        });
        
        HBox monthBox = new HBox(10);
        monthBox.getChildren().addAll(
            new Label("年份:"), yearCombo,
            new Label("月份:"), monthCombo
        );
        
        content.getChildren().addAll(allData, selectMonth, monthBox);
        dialog.getDialogPane().setContent(content);
        
        // 添加按钮
        ButtonType exportButtonType = new ButtonType("导出", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(exportButtonType, cancelButtonType);
        
        // 设置结果转换器
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == exportButtonType) {
                if (allData.isSelected()) {
                    return new ExportOption(true, null, null);
                } else {
                    return new ExportOption(false, yearCombo.getValue(), monthCombo.getValue());
                }
            }
            return null;
        });
        
        // 显示对话框并获取结果
        java.util.Optional<ExportOption> result = dialog.showAndWait();
        result.ifPresent(option -> {
            try {
                exportCSV(option);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("错误", "导出失败: " + e.getMessage());
            }
        });
    }
    
    private void exportCSV(ExportOption option) throws SQLException, IOException {
        // 构建SQL查询
        String sql = "SELECT * FROM transactions";
        if (!option.isAllData()) {
            int year = option.getYear();
            int month = option.getMonth();
            sql += " WHERE YEAR(date) = " + year + " AND MONTH(date) = " + month;
        }
        sql += " ORDER BY date DESC, id DESC";
        
        // 查询数据
        java.util.List<TransactionData> dataList = new java.util.ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String dbType = rs.getString("type");
                String displayType = dbType.equals("INCOME") ? "收入" : "支出";
                dataList.add(new TransactionData(
                    rs.getInt("id"),
                    rs.getString("description"),
                    rs.getDouble("amount"),
                    rs.getDate("date").toLocalDate(),
                    rs.getString("category"),
                    displayType
                ));
            }
        }
        
        if (dataList.isEmpty()) {
            showAlert("提示", "没有找到符合条件的数据");
            return;
        }
        
        // 选择保存位置
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存CSV文件");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV文件", "*.csv")
        );
        
        // 生成默认文件名
        String defaultFileName;
        if (option.isAllData()) {
            defaultFileName = "所有收支数据.csv";
        } else {
            defaultFileName = String.format("%d年%d月收支数据.csv", option.getYear(), option.getMonth());
        }
        fileChooser.setInitialFileName(defaultFileName);
        
        // 获取当前窗口
        Stage stage = (Stage) transactionTable.getScene().getWindow();
        java.io.File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            // 写入CSV文件
            try (FileWriter writer = new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
                // 写入BOM以支持Excel正确显示中文
                writer.write('\ufeff');
                
                // 写入表头
                writer.append("ID,描述,金额,日期,类别,类型\n");
                
                // 写入数据
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                for (TransactionData data : dataList) {
                    writer.append(String.valueOf(data.getId())).append(",");
                    writer.append(escapeCSV(data.getDescription())).append(",");
                    writer.append(String.format("%.2f", data.getAmount())).append(",");
                    writer.append(data.getDate().format(dateFormatter)).append(",");
                    writer.append(escapeCSV(data.getCategory() != null ? data.getCategory() : "")).append(",");
                    writer.append(data.getType()).append("\n");
                }
            }
            
            showAlert("成功", "数据已成功导出到: " + file.getAbsolutePath());
        }
    }
    
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // 如果包含逗号、引号或换行符，需要用引号包裹并转义引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // 内部类：导出选项
    private static class ExportOption {
        private final boolean allData;
        private final Integer year;
        private final Integer month;
        
        public ExportOption(boolean allData, Integer year, Integer month) {
            this.allData = allData;
            this.year = year;
            this.month = month;
        }
        
        public boolean isAllData() {
            return allData;
        }
        
        public Integer getYear() {
            return year;
        }
        
        public Integer getMonth() {
            return month;
        }
    }
    
    // 内部类：交易数据（用于导出）
    private static class TransactionData {
        private final int id;
        private final String description;
        private final double amount;
        private final LocalDate date;
        private final String category;
        private final String type;
        
        public TransactionData(int id, String description, double amount, LocalDate date, String category, String type) {
            this.id = id;
            this.description = description;
            this.amount = amount;
            this.date = date;
            this.category = category;
            this.type = type;
        }
        
        public int getId() { return id; }
        public String getDescription() { return description; }
        public double getAmount() { return amount; }
        public LocalDate getDate() { return date; }
        public String getCategory() { return category; }
        public String getType() { return type; }
    }
}

