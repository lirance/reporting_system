package com.antra.evaluation.reporting_system.service;

import com.antra.evaluation.reporting_system.pojo.api.ExcelRequest;
import com.antra.evaluation.reporting_system.pojo.api.MultiSheetExcelRequest;
import com.antra.evaluation.reporting_system.pojo.report.ExcelFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ExcelService {
    InputStream getExcelBodyById(String id) throws FileNotFoundException;

    ExcelFile generateExcelDataFromRequest(ExcelRequest request) throws IOException;

    ExcelFile generateMultiSheetExcelDataFromRequest(MultiSheetExcelRequest request) throws IOException;

    List<ExcelFile> getAllExcels();

    ExcelFile deleteExcelById(String id) throws RuntimeException;

    List<ExcelFile> generateBatchExcelDataFromRequest(List<MultiSheetExcelRequest> request) throws IOException;
}
