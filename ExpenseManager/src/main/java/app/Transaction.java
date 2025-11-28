package app;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Transaction {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty description = new SimpleStringProperty(this, "description");
    private final DoubleProperty amount = new SimpleDoubleProperty(this, "amount");
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(this, "date");
    private final StringProperty category = new SimpleStringProperty(this, "category");
    private final StringProperty type = new SimpleStringProperty(this, "type");

    // 空构造器（必须）
    public Transaction() {}

    // 带参构造器（可选）
    public Transaction(int id, String description, double amount, LocalDate date,
                       String category, String type) {
        this.id.set(id);
        this.description.set(description);
        this.amount.set(amount);
        this.date.set(date);
        this.category.set(category);
        this.type.set(type);
    }

    // 必须的 Property 方法（名字必须是 xxxProperty）
    public IntegerProperty idProperty() { return id; }
    public StringProperty descriptionProperty() { return description; }
    public DoubleProperty amountProperty() { return amount; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty typeProperty() { return type; }

    // 普通的 getter/setter（也可以保留，方便使用）
    public int getId() { return id.get(); }
    public String getDescription() { return description.get(); }
    public double getAmount() { return amount.get(); }
    public LocalDate getDate() { return date.get(); }
    public String getCategory() { return category.get(); }
    public String getType() { return type.get(); }

    public void setId(int id) { this.id.set(id); }
    public void setDescription(String desc) { this.description.set(desc); }
    public void setAmount(double amount) { this.amount.set(amount); }
    public void setDate(LocalDate date) { this.date.set(date); }
    public void setCategory(String category) { this.category.set(category); }
    public void setType(String type) { this.type.set(type); }
}