package com.antra.evaluation.reporting_system;

import com.alibaba.fastjson.JSON;
import com.antra.evaluation.reporting_system.pojo.api.ExcelRequest;
import com.antra.evaluation.reporting_system.pojo.api.MultiSheetExcelRequest;
import com.antra.evaluation.reporting_system.pojo.report.*;
import com.antra.evaluation.reporting_system.service.ExcelGenerationService;
import com.antra.evaluation.reporting_system.service.ExcelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReportingSystemApplicationTests {

    @Autowired
    ExcelGenerationService reportService;

    @Autowired
    ExcelService excelService;

    MultiSheetExcelRequest multiRequest = new MultiSheetExcelRequest();

    ExcelData data = new ExcelData();

    @BeforeEach // We are using JUnit 5, @Before is replaced by @BeforeEach
    public void setUpData() {
        data.setTitle("Test book");
        data.setGeneratedTime(LocalDateTime.now());

        var sheets = new ArrayList<ExcelDataSheet>();
        var sheet1 = new ExcelDataSheet();
        sheet1.setTitle("First Sheet");

        var headersS1 = new ArrayList<ExcelDataHeader>();
        ExcelDataHeader header1 = new ExcelDataHeader();
        header1.setName("NameTest");
        //       header1.setWidth(10000);
        header1.setType(ExcelDataType.STRING);
        headersS1.add(header1);

        ExcelDataHeader header2 = new ExcelDataHeader();
        header2.setName("Age");
        //   header2.setWidth(10000);
        header2.setType(ExcelDataType.NUMBER);
        headersS1.add(header2);

        List<List<Object>> dataRows = new ArrayList<>();
        List<Object> row1 = new ArrayList<>();
        row1.add("Dawei");
        row1.add(12);
        List<Object> row2 = new ArrayList<>();
        row2.add("Dawei2");
        row2.add(23);
        dataRows.add(row1);
        dataRows.add(row2);

        sheet1.setDataRows(dataRows);
        sheet1.setHeaders(headersS1);
        sheets.add(sheet1);
        data.setSheets(sheets);

        var sheet2 = new ExcelDataSheet();
        sheet2.setTitle("second Sheet");
        sheet2.setDataRows(dataRows);
        sheet2.setHeaders(headersS1);
        sheets.add(sheet2);

        String json = "{\n" +
                "  \"description\":\"Student Math Course Report\",\n" +
                "  \"headers\":[\"Student #\",\"Name\",\"Class\",\"Score\"],\n" +
                "  \"data\":[\n" +
                "    [\"s-001\", \"James\", \"Class-A\", \"A+\"],\n" +
                "    [\"s-002\",\"Robert\",\"Class-A\",\"A\"],\n" +
                "    [\"s-003\",\"Jennifer\",\"Class-A\",\"A\"],\n" +
                "    [\"s-004\",\"Linda\",\"Class-B\",\"B\"],\n" +
                "    [\"s-005\",\"Elizabeth\",\"Class-B\",\"B+\"],\n" +
                "    [\"s-006\",\"Susan\",\"Class-C\",\"A\"],\n" +
                "    [\"s-007\",\"Jessica\",\"Class-C\",\"A+\"],\n" +
                "    [\"s-008\",\"Sarah\",\"Class-A\",\"B\"],\n" +
                "    [\"s-009\",\"Thomas\",\"Class-A\",\"B-\"],\n" +
                "    [\"s-010\",\"Joseph\",\"Class-B\",\"A-\"],\n" +
                "    [\"s-011\",\"Charles\",\"Class-C\",\"A\"],\n" +
                "    [\"s-012\",\"Lisa\",\"Class-D\",\"B\"]\n" +
                "  ],\n" +
                "  \"submitter\":\"Mrs. York\",\n" +
                "  \"splitBy\":\"Score\"\n" +
                "}\n";
        multiRequest = JSON.parseObject(json, MultiSheetExcelRequest.class);
    }

    @Test
    public void testExcelGeneration() {
        File file = null;
        try {
            file = reportService.generateExcelReport(data, "temp");
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertNotNull(file);
    }

    @Test
    public void testDeleteFileReportById() throws Exception {
        File currDir = new File("test.xlsx");
        currDir.createNewFile();
        File file = reportService.deleteFile("test");
        assertEquals(file.getName(), "test.xlsx");
    }

    @Test
    public void testGenerateAndDeleteFile() throws Exception {
        ExcelFile file = excelService.generateMultiSheetExcelDataFromRequest(multiRequest);
        assertNotNull(file);
        ExcelFile delFile = excelService.deleteExcelById(file.getFileId());
        assertEquals(delFile, file);


    }

    @Test
    public void testGetExcelBodyById() throws IOException {
        ExcelFile file = excelService.generateExcelDataFromRequest(multiRequest);
        assertNotNull(file);

        String id = file.getFileId();
        assertNotNull(excelService.getExcelBodyById(id));
        assertTrue(excelService.getAllExcels().contains(file));

        ExcelFile delFile = excelService.deleteExcelById(file.getFileId());

        assertEquals(delFile, file);
        assertNull(excelService.getExcelBodyById(id));
        assertFalse(excelService.getAllExcels().contains(file));
    }


}
