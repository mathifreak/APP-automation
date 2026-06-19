package com.pice.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel-based test data provider for TestNG @DataProvider.
 * Reads .xlsx files and returns data in formats compatible with TestNG.
 *
 * <p>Expected Excel format:
 * <ul>
 *   <li>Row 0: Header row (column names)</li>
 *   <li>Row 1+: Data rows</li>
 * </ul>
 */
public final class ExcelDataProvider {

    private static final Logger log = LogManager.getLogger(ExcelDataProvider.class);

    private ExcelDataProvider() {
        // Prevent instantiation
    }

    /**
     * Read Excel data as a 2D Object array for TestNG @DataProvider.
     *
     * @param filePath  path to the .xlsx file
     * @param sheetName name of the sheet to read
     * @return 2D array of test data
     */
    public static Object[][] getTestData(String filePath, String sheetName) {
        List<Map<String, String>> data = readExcel(filePath, sheetName);
        if (data.isEmpty()) {
            return new Object[0][0];
        }

        Object[][] result = new Object[data.size()][1];
        for (int i = 0; i < data.size(); i++) {
            result[i][0] = data.get(i);
        }
        return result;
    }

    /**
     * Read Excel data as a list of maps (column name → value).
     *
     * @param filePath  path to the .xlsx file
     * @param sheetName name of the sheet to read
     * @return list of row data as maps
     */
    public static List<Map<String, String>> readExcel(String filePath, String sheetName) {
        List<Map<String, String>> data = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                log.error("Sheet '{}' not found in file: {}", sheetName, filePath);
                return data;
            }

            // Read header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                log.error("Header row is empty in sheet: {}", sheetName);
                return data;
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValueAsString(cell));
            }

            // Read data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowData = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    rowData.put(headers.get(j), getCellValueAsString(cell));
                }
                data.add(rowData);
            }

            log.info("Read {} rows from sheet '{}' in file: {}", data.size(), sheetName, filePath);

        } catch (IOException e) {
            log.error("Failed to read Excel file: {}", filePath, e);
        }

        return data;
    }

    /**
     * Convert any cell type to a string value.
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    yield String.valueOf((long) numValue);
                }
                yield String.valueOf(numValue);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK -> "";
            default -> "";
        };
    }
}
