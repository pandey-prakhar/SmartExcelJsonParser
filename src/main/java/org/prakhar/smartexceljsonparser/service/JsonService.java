package org.prakhar.smartexceljsonparser.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class JsonService {
    private final ObjectMapper mapper = new ObjectMapper();

    public byte[] convertJsonToExcel(byte[] jsonBytes) throws IOException {
        Map<String, Object> data = mapper.readValue(jsonBytes, new TypeReference<>() {});
        Workbook workbook = new XSSFWorkbook();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String sheetName = entry.getKey();
            Object sheetData = entry.getValue();

            if (sheetName.equals("Ala carte Benefits")) {
                createAlaCarteSheet(workbook, sheetName, (Map<String, Object>) sheetData);
            } else if (sheetName.equals("Policy info")) {
                createKeyValueSheet(workbook, sheetName, (Map<String, String>) sheetData);
            } else {
                createColumnarSheet(workbook, sheetName, (List<Map<String, String>>) sheetData);
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    // For "Ala carte Benefits" (restore metadata)
    private void createAlaCarteSheet(Workbook workbook, String sheetName, Map<String, Object> sheetData) {
        Sheet sheet = workbook.createSheet(sheetName);
        List<String> headers = (List<String>) ((Map<String, Object>) sheetData.get("schema")).get("mandatory");
        headers.addAll((List<String>) ((Map<String, Object>) sheetData.get("schema")).get("optional"));

        // Add metadata rows: [optional]/[mandatory] and data types
        Row metadataRow = sheet.createRow(0);
        Row dataTypeRow = sheet.createRow(1);
        for (int i = 0; i < headers.size(); i++) {
            String field = headers.get(i);
            boolean isOptional = ((List<String>) ((Map<String, Object>) sheetData.get("schema")).get("optional")).contains(field);
            metadataRow.createCell(i).setCellValue(isOptional ? "[optional]" : "[mandatory");
            dataTypeRow.createCell(i).setCellValue("<text>"); // Replace with actual data types if needed
        }

        // Add data rows
        List<Map<String, String>> rows = (List<Map<String, String>>) sheetData.get("data");
        int rowNum = 2;
        for (Map<String, String> row : rows) {
            Row dataRow = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.size(); i++) {
                dataRow.createCell(i).setCellValue(row.get(headers.get(i)));
            }
        }
    }

    // For "Policy info" (key-value)
    private void createKeyValueSheet(Workbook workbook, String sheetName, Map<String, String> data) {
        Sheet sheet = workbook.createSheet(sheetName);
        int rowNum = 0;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
    }

    // For other sheets (columnar)
    private void createColumnarSheet(Workbook workbook, String sheetName, List<Map<String, String>> data) {
        Sheet sheet = workbook.createSheet(sheetName);
        if (data.isEmpty()) return;

        // Headers
        Row headerRow = sheet.createRow(0);
        List<String> headers = new ArrayList<>(data.get(0).keySet());
        for (int i = 0; i < headers.size(); i++) {
            headerRow.createCell(i).setCellValue(headers.get(i));
        }

        // Data rows
        int rowNum = 1;
        for (Map<String, String> row : data) {
            Row dataRow = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.size(); i++) {
                dataRow.createCell(i).setCellValue(row.get(headers.get(i)));
            }
        }
    }
}