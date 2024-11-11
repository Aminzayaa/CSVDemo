package com.example.CSVDemo.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.poi.ss.usermodel.*;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    

    @Override
    public List<String> getTableNames() {
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'member'";
        return jdbcTemplate.queryForList(sql, String.class);

    }
    
    

    @Override
    public boolean hasXlsxFormat(MultipartFile file) {
        return file.getOriginalFilename().endsWith(".xlsx");
    }

    @Override
    public String processAndSaveData(MultipartFile file) throws Exception {
    try (InputStream is = file.getInputStream();
         Workbook workbook = new XSSFWorkbook(is)) {

        Sheet sheet = workbook.getSheetAt(0);
        List<String> headers = new ArrayList<>();
        Row headerRow = sheet.getRow(0); // Assuming the first row contains headers

        // Extracting header values dynamically
        for (Cell cell : headerRow) {
            headers.add(cell.getStringCellValue());
        }

        String tableName = sanitizeTableName(file.getOriginalFilename().replace(".xlsx", ""));
        if (!tableExists(tableName)) {
            createTable(headers, tableName);
        } else {
            addMissingColumns(headers, tableName);
        }

        // Iterate over rows, skipping the header row
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            List<String> values = convertRowToValues(row);
            String uniqueIdentifier = headers.get(0); // Assuming the first column is the unique identifier
            String uniqueFieldValue = values.get(0);

            if (isRecordExists(tableName, uniqueIdentifier, uniqueFieldValue)) {
                if (hasDataChanged(tableName, row, uniqueIdentifier, uniqueFieldValue)) {
                    updateData(values, headers, tableName, uniqueIdentifier, uniqueFieldValue);
                }
            } else {
                insertData(values, headers, tableName);
            }
        }

        return tableName;
    }
}

    
    private String sanitizeTableName(String originalName) {
        return originalName.replaceAll("[^a-zA-Z0-9_]", "_").replaceAll("_+", "_").replaceAll("^_|_$", "");
    }

    private boolean tableExists(String tableName) {
        String query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
        Integer count = jdbcTemplate.queryForObject(query, new Object[]{tableName.toLowerCase()}, Integer.class);
        return count != null && count > 0;
    }

    private void createTable(List<String> headers, String tableName) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
                headers.stream().map(header -> "`" + header + "` VARCHAR(255)").collect(Collectors.joining(", ")) + ")";
        jdbcTemplate.execute(createTableQuery);
    }

    private void addMissingColumns(List<String> headers, String tableName) {
        List<String> existingColumns = getExistingColumns(tableName);
        for (String header : headers) {
            if (!existingColumns.contains(header)) {
                String alterTableQuery = "ALTER TABLE `" + tableName + "` ADD COLUMN `" + header + "` VARCHAR(255)";
                jdbcTemplate.execute(alterTableQuery);
            }
        }
    }

    private List<String> getExistingColumns(String tableName) {
        String query = "SELECT COLUMN_NAME FROM information_schema.columns WHERE table_name = ?";
        return jdbcTemplate.queryForList(query, new Object[]{tableName.toLowerCase()}, String.class);
    }

    private boolean isRecordExists(String tableName, String uniqueIdentifier, String uniqueFieldValue) {
        String query = "SELECT COUNT(*) FROM `" + tableName + "` WHERE `" + uniqueIdentifier + "` = ?";
        Integer count = jdbcTemplate.queryForObject(query, new Object[]{uniqueFieldValue}, Integer.class);
        return count != null && count > 0;
    }
    
    private boolean hasDataChanged(String tableName, Row row, String uniqueIdentifier, String uniqueFieldValue) {
        String query = "SELECT * FROM `" + tableName + "` WHERE `" + uniqueIdentifier + "` = ?";
        List<String> existingData = jdbcTemplate.queryForList(query, String.class, uniqueFieldValue);

        if (existingData.isEmpty()) {
            return true;
        }

        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !cell.toString().equals(existingData.get(i))) {
                return true; // Data has changed
            }
        }
        return false;
    }

    private void updateData(List<String> values, List<String> headers, String tableName, String uniqueIdentifier, String uniqueFieldValue) {
        String setClause = headers.stream().map(header -> "`" + header + "` = ?").collect(Collectors.joining(", "));
        String updateQuery = "UPDATE `" + tableName + "` SET " + setClause + " WHERE `" + uniqueIdentifier + "` = ?";
        List<String> parameters = new ArrayList<>(values);
        parameters.add(uniqueFieldValue);
        jdbcTemplate.update(updateQuery, parameters.toArray(new String[0]));
    }
    public void uploadExcelFile(String filePath) throws IOException, SQLException {
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = WorkbookFactory.create(fis);
        Sheet sheet = workbook.getSheetAt(0);
    
        // Create table dynamically based on the Excel header
        StringBuilder createTableSql = new StringBuilder("CREATE TABLE IF NOT EXISTS test1 (id INT PRIMARY KEY AUTO_INCREMENT, ");
        Row headerRow = sheet.getRow(0);
    
        for (Cell cell : headerRow) {
            String columnName = cell.getStringCellValue();
            createTableSql.append("`").append(columnName).append("` VARCHAR(255), "); // Using VARCHAR for simplicity
        }
    
        // Remove last comma and space, and add closing parenthesis
        createTableSql.setLength(createTableSql.length() - 2);
        createTableSql.append(");");
    
        // Execute create table SQL
        jdbcTemplate.execute(createTableSql.toString());
    
        // Insert data from the Excel file
        for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Start from the second row
            Row row = sheet.getRow(i);
    
            // Prepare insert SQL with backticks
            StringBuilder insertSql = new StringBuilder("INSERT INTO test1 (");
            StringBuilder valuesPlaceholder = new StringBuilder();
    
            for (Cell cell : headerRow) {
                String columnName = cell.getStringCellValue();
                insertSql.append("`").append(columnName).append("`, ");
                valuesPlaceholder.append("?, ");
            }
    
            // Remove last comma and space
            insertSql.setLength(insertSql.length() - 2);
            valuesPlaceholder.setLength(valuesPlaceholder.length() - 2);
    
            // Finalize insert SQL
            insertSql.append(") VALUES (").append(valuesPlaceholder).append(");");
    
            // Prepare values for insertion
            Object[] values = new Object[headerRow.getLastCellNum()];
            for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                values[j] = cell != null ? cell.toString() : null; // Handle null values
            }
    
            // Execute insert SQL
            jdbcTemplate.update(insertSql.toString(), values);
        }
    
        workbook.close();
        fis.close();
    }
    private void insertData(List<String> values, List<String> headers, String tableName) {
        String columnNames = String.join(", ", headers.stream().map(header -> "`" + header + "`").collect(Collectors.toList()));
        String placeholders = values.stream().map(value -> "?").collect(Collectors.joining(", "));
        String insertQuery = "INSERT INTO `" + tableName + "` (" + columnNames + ") VALUES (" + placeholders + ")";
        jdbcTemplate.update(insertQuery, values.toArray(new String[0]));
    }

    private List<String> convertRowToValues(Row row) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            String cleanedValue = (cell != null) ? cell.toString().trim() : "";
            values.add(cleanedValue.isEmpty() ? "NULL" : cleanedValue);
        }
        return values;
    }

    @Override
    public void saveFile(MultipartFile file) throws Exception {
        if (!hasXlsxFormat(file)) {
            throw new IllegalArgumentException("File is not in Excel format!");
        }

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            // Get the first sheet from the Excel file
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet is empty!");
            }

            // Get the headers (assuming first row contains column names)
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Header row is missing!");
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }

            // Create or modify the table based on headers
            String tableName = sanitizeTableName(file.getOriginalFilename().replace(".xlsx", ""));
            if (!tableExists(tableName)) {
                createTable(headers, tableName);
            }

            // Iterate through each row (skipping header row) and insert into the database
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                List<String> values = new ArrayList<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    values.add(getCellValueAsString(cell));
                }

                // Insert the row into the database
                insertData(values, headers, tableName);
            }
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    // @Override
    // public List<String> getTableNames() {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'getTableNames'");
    
        // @Override
        // public List<String> getTableNames() {
        //     String query = "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()";
        //     return jdbcTemplate.queryForList(query, String.class);
    }

