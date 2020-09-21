package com.antra.evaluation.reporting_system;

import com.antra.evaluation.reporting_system.endpoint.ExcelGenerationController;
import com.antra.evaluation.reporting_system.pojo.api.ExcelRequest;
import com.antra.evaluation.reporting_system.pojo.api.MultiSheetExcelRequest;
import com.antra.evaluation.reporting_system.pojo.report.ExcelFile;
import com.antra.evaluation.reporting_system.service.ExcelService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class APITest {
    @Mock
    ExcelService excelService;

    ExcelFile ef = new ExcelFile();

    @BeforeEach
    public void configMock() {
        ef.generateId();
        MockitoAnnotations.initMocks(this);
        RestAssuredMockMvc.standaloneSetup(new ExcelGenerationController(excelService));
    }

    @Test
    public void testExcelGeneration() throws IOException {
        when(excelService.generateExcelDataFromRequest(any(ExcelRequest.class))).thenReturn(ef);
        given().accept("application/json").contentType(ContentType.JSON).body("{\"headers\":[\"Name\",\"Age\"], \"data\":[[\"Teresa\",\"5\"],[\"Daniel\",\"1\"]]}").post("/excel").peek().
                then().assertThat()
                .statusCode(200)
                .body("fileId", Matchers.notNullValue());

    }

    @Test
    public void testMultiSheetExcelGeneration() throws IOException {
        when(excelService.generateMultiSheetExcelDataFromRequest(any(MultiSheetExcelRequest.class))).thenReturn(ef);
        given().accept("application/json").contentType(ContentType.JSON).body("{\"headers\":[\"Name\",\"Age\"], \"data\":[[\"Teresa\",\"5\"],[\"Daniel\",\"1\"]], \"splitBy\":\"class\"}").post("/excel/auto").peek().
                then().assertThat()
                .statusCode(200)
                .body("fileId", Matchers.notNullValue());

    }

    @Test
    public void testBatchSheetExcelGeneration() throws IOException {
        when(excelService.generateMultiSheetExcelDataFromRequest(any(MultiSheetExcelRequest.class))).thenReturn(ef);
        given().accept("application/json").contentType(ContentType.JSON)
                .body("[{\"headers\":[\"Name\",\"Age\"], \"data\":[[\"Teresa\",\"5\"],[\"Daniel\",\"1\"]], \"splitBy\":\"class\"},{\"headers\":[\"Name\",\"Age\"], \"data\":[[\"Teresa\",\"5\"],[\"Daniel\",\"1\"]], \"splitBy\":\"class\"}]")
                .post("/excel/batch").peek().
                then().assertThat()
                .statusCode(200)
                .body("fileId", Matchers.notNullValue());

    }

    @Test
    public void testFileDownload() throws FileNotFoundException {
        when(excelService.getExcelBodyById(anyString())).thenReturn(new FileInputStream("temp.xlsx"));
        given().accept("application/json").get("/excel/123abcd/content").peek().
                then().assertThat()
                .statusCode(200);
    }

    @Test
    public void testListFiles() throws FileNotFoundException {
        given().accept("application/json").get("/excel").peek().
                then().assertThat()
                .statusCode(200);
    }

    @Test
    public void testDeleteFile() throws FileNotFoundException {
        when(excelService.deleteExcelById(anyString())).thenReturn(ef);
        given().accept("application/json").delete("/excel/123abcd").peek().
                then().assertThat()
                .statusCode(200);
    }


}
