package com.hami.care.reporting;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelReportListener implements ITestListener {

    private static class RowData {
        final String testId;
        final String title;
        final String preconditions;
        final String steps;
        final String expected;
        final String actual;
        final String status;

        RowData(String testId, String title, String preconditions, String steps, String expected, String actual, String status) {
            this.testId = testId;
            this.title = title;
            this.preconditions = preconditions;
            this.steps = steps;
            this.expected = expected;
            this.actual = actual;
            this.status = status;
        }
    }

    private final Map<String, List<RowData>> resultsByClass = new LinkedHashMap<>();

    @Override
    public void onTestSuccess(ITestResult result) {
        recordResult(result, "PASS", "Executed successfully; all assertions passed.");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable t = result.getThrowable();
        String msg = (t != null && t.getMessage() != null) ? t.getMessage() : "Test failed.";
        recordResult(result, "FAIL", msg);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        Throwable t = result.getThrowable();
        String msg = (t != null && t.getMessage() != null) ? "Skipped: " + t.getMessage() : "Test skipped.";
        recordResult(result, "SKIP", msg);
    }

    private synchronized void recordResult(ITestResult result, String status, String defaultActual) {
        String className = result.getTestClass().getRealClass().getSimpleName();
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        TestCaseMeta meta = method.getAnnotation(TestCaseMeta.class);

        String testId = (meta != null) ? meta.id() : method.getName();
        String title = (meta != null) ? meta.title()
                : (result.getMethod().getDescription() != null ? result.getMethod().getDescription() : method.getName());
        String preconditions = (meta != null) ? meta.preconditions() : "";
        String steps = (meta != null) ? meta.steps() : "";
        String expected = (meta != null) ? meta.expected() : "";

        Object customActual = result.getAttribute("actualResult");
        String actual = (customActual != null) ? customActual.toString() : defaultActual;

        resultsByClass.computeIfAbsent(className, k -> new ArrayList<>())
                .add(new RowData(testId, title, preconditions, steps, expected, actual, status));
    }

    @Override
    public void onFinish(ITestContext context) {
        Path reportDir = Paths.get("target", "excel-reports");
        try {
            Files.createDirectories(reportDir);
            for (Map.Entry<String, List<RowData>> entry : resultsByClass.entrySet()) {
                writeWorkbook(reportDir.resolve(entry.getKey() + "_Report.xlsx"), entry.getValue());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write Excel test reports", e);
        }
    }

    private void writeWorkbook(Path filePath, List<RowData> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test Results");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.LEFT);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);
            wrapStyle.setVerticalAlignment(VerticalAlignment.TOP);

            CellStyle passStyle = workbook.createCellStyle();
            passStyle.cloneStyleFrom(wrapStyle);
            passStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            passStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle failStyle = workbook.createCellStyle();
            failStyle.cloneStyleFrom(wrapStyle);
            failStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            failStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"Test ID", "Test Case Title", "Preconditions", "Test Steps", "Expected Result", "Actual Result", "Status"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (RowData data : rows) {
                Row row = sheet.createRow(rowIdx++);
                String[] values = {data.testId, data.title, data.preconditions, data.steps, data.expected, data.actual, data.status};
                for (int col = 0; col < values.length; col++) {
                    Cell cell = row.createCell(col);
                    cell.setCellValue(values[col]);
                    if (col == 6 && "PASS".equals(data.status)) {
                        cell.setCellStyle(passStyle);
                    } else if (col == 6 && "FAIL".equals(data.status)) {
                        cell.setCellStyle(failStyle);
                    } else {
                        cell.setCellStyle(wrapStyle);
                    }
                }
            }

            int[] widths = {16, 36, 26, 48, 44, 44, 12};
            for (int i = 0; i < widths.length; i++) {
                sheet.setColumnWidth(i, widths[i] * 256);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                workbook.write(fos);
            }
        }
    }
}
