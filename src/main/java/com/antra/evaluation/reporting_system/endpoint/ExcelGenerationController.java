package com.antra.evaluation.reporting_system.endpoint;

import com.antra.evaluation.reporting_system.pojo.api.ErrorResponse;
import com.antra.evaluation.reporting_system.pojo.api.ExcelRequest;
import com.antra.evaluation.reporting_system.pojo.api.ExcelResponse;
import com.antra.evaluation.reporting_system.pojo.api.MultiSheetExcelRequest;
import com.antra.evaluation.reporting_system.pojo.report.ExcelFile;
import com.antra.evaluation.reporting_system.service.ExcelService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ExcelGenerationController {

    private static final Logger log = LoggerFactory.getLogger(ExcelGenerationController.class);

    ExcelService excelService;

    @Autowired
    public ExcelGenerationController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @PostMapping("/excel")
    @ApiOperation("Generate Excel")
    public ResponseEntity<ExcelResponse> createExcel(@RequestBody @Valid ExcelRequest request) throws IOException {
        ExcelResponse response = new ExcelResponse();

        ExcelFile file = excelService.generateExcelDataFromRequest(request);
        response.generateResponseFromFile(file);
        response.setMessage("Generate Success");
        log.info("File generate success, file id: " + file.getFileId());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/excel/auto")
    @ApiOperation("Generate Multi-Sheet Excel Using Split field")
    public ResponseEntity<ExcelResponse> createMultiSheetExcel(@RequestBody @Valid MultiSheetExcelRequest request) throws IOException {
        ExcelResponse response = new ExcelResponse();

        ExcelFile file = excelService.generateMultiSheetExcelDataFromRequest(request);
        response.generateResponseFromFile(file);
        response.setMessage("Generate Success");
        log.info("File generate and split success, file id: " + file.getFileId());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/excel")
    @ApiOperation("List all existing files")
    public ResponseEntity<List<ExcelResponse>> listExcels() {
        var response = new ArrayList<ExcelResponse>();
        List<ExcelFile> files = excelService.getAllExcels();
        for (ExcelFile file : files) {
            ExcelResponse resp = new ExcelResponse();
            resp.generateResponseFromFile(file);
            resp.setMessage("Success");
            response.add(resp);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/excel/{id}/content")
    public void downloadExcel(@PathVariable String id, HttpServletResponse response) throws IOException {
        InputStream fis = excelService.getExcelBodyById(id);
        response.setHeader("Content-Type", "application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + id + ".xls\"");
        if (fis != null) {
            FileCopyUtils.copy(fis, response.getOutputStream());
            log.info("File Download, fileId : " + id);
            fis.close();
        } else {
            log.warn("File not Found with id: " + id);
            throw new FileNotFoundException("File not Found with id : " + id);
        }

    }

    @DeleteMapping("/excel/{id}")
    public ResponseEntity<ExcelResponse> deleteExcel(@PathVariable String id) throws FileNotFoundException {
        var response = new ExcelResponse();
        ExcelFile file = excelService.deleteExcelById(id);
        response.generateResponseFromFile(file);
        response.setMessage("Delete Success");
        log.info("File delete success, fileId :" + file.getFileId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/excel/batch")
    @ApiOperation("Generate Multiple Excel At Once ")
    public ResponseEntity<List<ExcelResponse>> createExcels(@RequestBody @Valid List<MultiSheetExcelRequest> request) {
        List<ExcelResponse> response = new ArrayList<>();
        for (MultiSheetExcelRequest req : request) {
            ExcelResponse resp = new ExcelResponse();
            // instead of throw the Exception and let exception handler to handle it,
            // i use try catch here because I want the batch generate api to generate as many as possible
            //even if some of the request cannot be generate.
            try {
                ExcelFile file;
                if (req.getSplitBy() != null) {
                    file = excelService.generateMultiSheetExcelDataFromRequest(req);
                } else {
                    file = excelService.generateExcelDataFromRequest(req);
                }
                resp.generateResponseFromFile(file);
                resp.setMessage("File Generate Success");
                log.info("File generate success, fileId :" + file.getFileId());
            } catch (RuntimeException | IOException e) {
                resp.setMessage("File cannot be generated,check your input");
                log.info("File generated failed");
            } finally {
                response.add(resp);
            }
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(Exception ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setMessage(ex.getMessage());
        log.error("Controller Error", ex);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponse> exceptionHandlerFileNotFound(Exception ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.NOT_FOUND.value());
        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> exceptionHandlerFileCannotGenerate(Exception ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.NOT_FOUND.value());
        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> exceptionHandlerFileError(Exception ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.NOT_FOUND.value());
        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.EXPECTATION_FAILED);
    }
}
