package com.antra.evaluation.reporting_system.endpoint;

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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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
    public ResponseEntity<ExcelResponse> createExcel(@RequestBody @Validated ExcelRequest request) {
        ExcelResponse response = new ExcelResponse();
        try {
            ExcelFile file = excelService.generateExcelDataFromRequest(request);
            response.generateResponseFromFile(file);
            response.setMessage("Generate Success");
            log.info("File generate success, file id: " + file.getFileId());
        } catch (RuntimeException | IOException e) {
            log.warn("input format not correct", e);
            response.setMessage("File generated failed, check input format");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/excel/auto")
    @ApiOperation("Generate Multi-Sheet Excel Using Split field")
    public ResponseEntity<ExcelResponse> createMultiSheetExcel(@RequestBody @Validated MultiSheetExcelRequest request) {
        ExcelResponse response = new ExcelResponse();
        try {
            ExcelFile file = excelService.generateMultiSheetExcelDataFromRequest(request);
            response.generateResponseFromFile(file);
            response.setMessage("Generate Success");
            log.info("File generate and split success, file id: " + file.getFileId());
        } catch (RuntimeException | IOException e) {
            log.warn("input format not correct", e);
            response.setMessage("File generated failed, check input format");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
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
            response.sendError(400, "File not Found with id : " + id);
        }

    }

    @DeleteMapping("/excel/{id}")
    public ResponseEntity<ExcelResponse> deleteExcel(@PathVariable String id) {
        var response = new ExcelResponse();
        try {
            ExcelFile file = excelService.deleteExcelById(id);
            response.generateResponseFromFile(file);
            response.setMessage("Delete Success");
            log.info("File delete success, fileId :" + file.getFileId());
        } catch (RuntimeException e) {
            response.setMessage("File Does not Exist with id : " + id);
            log.warn("file does not exist", e);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/excel/batch")
    @ApiOperation("Generate Multiple Excel At Once ")
    public ResponseEntity<List<ExcelResponse>> createExcels(@RequestBody @Validated List<MultiSheetExcelRequest> request) {
        List<ExcelResponse> response = new ArrayList<>();
// if one bad request, do not generate any file
//        try {
//            List<ExcelFile> files = excelService.generateBatchExcelDataFromRequest(request);
//            for (ExcelFile file:files){
//                ExcelResponse resp = new ExcelResponse();
//                resp.generateResponseFromFile(file);
//                resp.setMessage("File Generate Success");
//                log.info("File generate success, fileId :" + file.getFileId());
//            }
//        } catch (IOException e) {
//            log.warn("Something wrong with the input format");
//            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//        }
        // generate all files that is correct, and return the result.
        for (MultiSheetExcelRequest req : request) {
            ExcelResponse resp = new ExcelResponse();
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
            } catch (IOException e) {
                log.error("file error", e);
            } catch (RuntimeException e) {
                resp.setMessage("File cannot be generated");
                log.info("File generated failed");
            } finally {
                response.add(resp);
            }
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
