package com.example.CSVDemo.entity;

import jakarta.persistence.*;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "dynamic_table") 
public class Employee { 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Transient
    private Map<String, Object> columns; 

   
    @Transient 
    private String tableName;
    private Date tableDate;
    

    public Date getTableDate() {
        return tableDate;
    }

    public void setTableDate(Date tableDate) {
        this.tableDate = tableDate;
    }

    public Employee() {
        this.columns = new HashMap<>();
    }

    public void setColumnValue(String columnName, Object value) {
        this.columns.put(columnName, value);
    }
    public Object getColumnValue(String columnName) {
        return this.columns.get(columnName);
    }

    public Long getId() {
        return id;
    }

    public Map<String, Object> getColumns() {
        return this.columns;
    }

    public void setColumns(Map<String, Object> columns) {
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", tableName='" + tableName + '\'' +
                ", columns=" + columns +
                '}';
    }
}
