package com.example.CSVDemo.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.CSVDemo.entity.Member;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public boolean hasCsvFormat(MultipartFile file) {
        return "text/csv".equals(file.getContentType());
    }

    @Override
    public String processAndSaveData(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            List<String> headers = csvParser.getHeaderNames();
            String tableName = sanitizeTableName(file.getOriginalFilename().replace(".csv", ""));

            if (!tableExists(tableName)) {
                createTable(headers, tableName);
            } else {
                // Check and add new columns if necessary
                addMissingColumns(headers, tableName);
            }

            for (CSVRecord csvRecord : csvParser) {
                List<String> values = convertRecordToValues(csvRecord);
                String uniqueIdentifier = headers.get(0);  // Assuming the first column as the unique identifier
                String uniqueFieldValue = csvRecord.get(uniqueIdentifier); // Get value for the unique identifier

                if (isRecordExists(tableName, uniqueIdentifier, uniqueFieldValue)) {
                    // Check if the data has changed, if so, update it
                    if (hasDataChanged(tableName, csvRecord, uniqueIdentifier, uniqueFieldValue)) {
                        updateData(values, headers, tableName, uniqueIdentifier, uniqueFieldValue);
                    }
                } else {
                    // Insert new data if it doesn't exist
                    insertData(values, headers, tableName);
                }
            }

            return tableName;
        }
    }

    private String sanitizeTableName(String originalName) {
        String sanitized = originalName.replaceAll("[^a-zA-Z0-9_]", "_");
        sanitized = sanitized.replaceAll("_+", "_");
        sanitized = sanitized.replaceAll("^_|_$", "");
        return sanitized.isEmpty() ? "default_table" : sanitized;
    }

    private boolean tableExists(String tableName) {
        String query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
        Integer count = jdbcTemplate.queryForObject(query, new Object[]{tableName.toLowerCase()}, Integer.class);
        return count != null && count > 0;
    }

    private void createTable(List<String> headers, String tableName) throws SQLException {
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

    private boolean hasDataChanged(String tableName, CSVRecord csvRecord, String uniqueIdentifier, String uniqueFieldValue) {
        String query = "SELECT * FROM `" + tableName + "` WHERE `" + uniqueIdentifier + "` = ?";
        List<Map<String, Object>> existingData = jdbcTemplate.queryForList(query, uniqueFieldValue);

        if (existingData.isEmpty()) {
            return true;
        }

        Map<String, Object> existingRow = existingData.get(0);
        for (String header : csvRecord.toMap().keySet()) {
            if (!csvRecord.get(header).equals(existingRow.get(header))) {
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

    private void insertData(List<String> values, List<String> headers, String tableName) {
        String columnNames = String.join(", ", headers.stream().map(header -> "`" + header + "`").collect(Collectors.toList()));
        String placeholders = values.stream().map(value -> "?").collect(Collectors.joining(", "));
        String insertQuery = "INSERT INTO `" + tableName + "` (" + columnNames + ") VALUES (" + placeholders + ")";
        jdbcTemplate.update(insertQuery, values.toArray(new String[0]));
    }

    private List<String> convertRecordToValues(CSVRecord csvRecord) {
        List<String> values = new ArrayList<>();
        for (String field : csvRecord) {
            String cleanedValue = cleanValue(field);
            values.add(cleanedValue.isEmpty() ? "NULL" : cleanedValue);
        }
        return values;
    }

    private String cleanValue(String value) {
        return value.replaceAll("[^\\x20-\\x7E]", "").trim(); // Keeps printable ASCII characters
    }

}
