
package com.example.CSVDemo.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String lastFileChecksum = null;


    
    @Override
    public String saveFile(MultipartFile file) {
        if (!hasXlsxFormat(file)) {
            return "File is not in Excel format!";
        }
        
        try {
            String newFileChecksum = generateFileChecksum(file);
            if (newFileChecksum.equals(lastFileChecksum)) {
                return "File unchanged since last upload. No update needed.";
            }

            String tableName = processAndSaveData(file);
            lastFileChecksum = newFileChecksum;

            

            return "File processed and saved to table: " + tableName;

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            return "An error occurred while processing the file.";
        }
    }



    @Override
    public boolean hasXlsxFormat(MultipartFile file) {
        return file.getOriginalFilename() != null && file.getOriginalFilename().endsWith(".xlsx");
    }

    private String generateFileChecksum(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = file.getBytes();
        byte[] digest = md.digest(bytes);
        return bytesToHex(digest);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public String processAndSaveData(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getRow(0) == null) {
                throw new IllegalArgumentException("Invalid or empty sheet");
            }

            List<String> headers = extractHeaders(sheet);
            String tableName = sanitizeTableName(file.getOriginalFilename().replace(".xlsx", ""));
            ensureTableStructure(headers, tableName);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    List<String> values = convertRowToValues(row, headers.size());
                    
                    // Check if row is duplicate
                    if (!isDuplicateRow(values, headers, tableName)) {
                        insertData(values, headers, tableName);
                    } else {
                        System.out.println("Skipping duplicate row: " + values);
                    }
                }
            }
            return tableName;
        } catch (IOException | IllegalArgumentException e) {
            return "File is not in Excel format!";
        }
    }

    private List<String> extractHeaders(Sheet sheet) {
        List<String> headers = new ArrayList<>();
        Row headerRow = sheet.getRow(0);
        for (Cell cell : headerRow) {
            headers.add(getCellValueAsString(cell));
        }
        return headers;
    }

    private String sanitizeTableName(String originalName) {
        return originalName.replaceAll("[^a-zA-Z0-9_]", "_").replaceAll("_+", "_").replaceAll("^_|_$", "");
    }

    private void ensureTableStructure(List<String> headers, String tableName) {
        if (!tableExists(tableName)) {
            createTable(headers, tableName);
        } else {
            addMissingColumns(headers, tableName);
        }
    }

    private boolean tableExists(String tableName) {
        String query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
        Integer count = jdbcTemplate.queryForObject(query, new Object[]{tableName.toLowerCase()}, Integer.class);
        return count != null && count > 0;
    }

    private void addMissingColumns(List<String> headers, String tableName) {
        List<String> existingColumns = getExistingColumns(tableName);
        for (String header : headers) {
            String sanitizedHeader = sanitizeColumnName(header);
            if (!existingColumns.contains(sanitizedHeader)) {
                try {
                    String alterTableQuery = "ALTER TABLE " + tableName + " ADD COLUMN " + sanitizedHeader + " VARCHAR(255)";
                    jdbcTemplate.execute(alterTableQuery);
                } catch (Exception e) {
                    System.err.println("Error adding column " + sanitizedHeader + ": " + e.getMessage());
                }
            }
        }
    }

    private List<String> getExistingColumns(String tableName) {
        String query = "SELECT COLUMN_NAME FROM information_schema.columns WHERE table_name = ?";
        return jdbcTemplate.queryForList(query, new Object[]{tableName.toLowerCase()}, String.class);
    }

    private String sanitizeColumnName(String originalName) {
        String sanitized = originalName.replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();
        if (Character.isDigit(sanitized.charAt(0))) {
            sanitized = "_" + sanitized;
        }
        return sanitized;
    }

    private void createTable(List<String> headers, String tableName) {
        Set<String> uniqueColumns = new HashSet<>();
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (";
        createTableQuery += IntStream.range(0, headers.size())
                .mapToObj(i -> {
                    String columnName = sanitizeColumnName(headers.get(i));
                    while (!uniqueColumns.add(columnName)) {
                        columnName += "_dup";
                    }
                    return columnName + " VARCHAR(255)";
                })
                .collect(Collectors.joining(", "));
        createTableQuery += ")";
        jdbcTemplate.execute(createTableQuery);
    }

    private void insertData(List<String> values, List<String> headers, String tableName) {
        List<String> sanitizedHeaders = headers.stream()
                                               .map(this::sanitizeColumnName)
                                               .collect(Collectors.toList());

        String columns = String.join(", ", sanitizedHeaders);
        String placeholders = String.join(", ", Collections.nCopies(sanitizedHeaders.size(), "?"));

        while (values.size() < sanitizedHeaders.size()) {
            values.add("");
        }

        String insertQuery = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
        jdbcTemplate.update(insertQuery, values.toArray());
    }

    private boolean isDuplicateRow(List<String> values, List<String> headers, String tableName) {
        String condition = IntStream.range(0, headers.size())
                .mapToObj(i -> sanitizeColumnName(headers.get(i)) + " = ?")
                .collect(Collectors.joining(" AND "));
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE " + condition;

        Integer count = jdbcTemplate.queryForObject(query, values.toArray(), Integer.class);
        return count != null && count > 0;
    }
    @Override
    public List<Map<String, Object>> getTableInfo() {
        String sql = "SELECT table_name, create_time AS upload_date FROM information_schema.tables WHERE table_schema = 'member' ORDER BY upload_date DESC";
        return jdbcTemplate.queryForList(sql);
    }

   

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private List<String> convertRowToValues(Row row, int expectedSize) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < expectedSize; i++) {
            Cell cell = row.getCell(i);
            values.add(getCellValueAsString(cell));
        }
        return values;
    }



  



    
    }






