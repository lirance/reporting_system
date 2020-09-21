package com.antra.evaluation.reporting_system.service;

import com.antra.evaluation.reporting_system.pojo.api.ExcelRequest;
import com.antra.evaluation.reporting_system.pojo.api.MultiSheetExcelRequest;
import com.antra.evaluation.reporting_system.pojo.report.ExcelData;
import com.antra.evaluation.reporting_system.pojo.report.ExcelDataHeader;
import com.antra.evaluation.reporting_system.pojo.report.ExcelDataSheet;
import com.antra.evaluation.reporting_system.pojo.report.ExcelFile;
import com.antra.evaluation.reporting_system.repo.ExcelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExcelServiceImpl implements ExcelService {

    @Autowired
    ExcelRepository excelRepository;

    @Autowired
    ExcelGenerationService excelGenerationService;


    private void validateDate(ExcelRequest request) {

        if (request == null) {
            throw new RuntimeException("Request Error: No Data Specified");
        }

        if (request.getData() == null || request.getData().isEmpty()) {
            throw new RuntimeException("Request Error: No Data to generate");
        }

        if (request.getHeaders() == null || request.getHeaders().isEmpty()) {
            throw new RuntimeException("Request Error: No headers to generate");
        }

    }

    private void validateSplitData(MultiSheetExcelRequest request) {
        validateDate(request);
        String splitBy = request.getSplitBy();
        if (splitBy == null) {
            throw new RuntimeException("Split Excel Data Error: No split column specified");
        } else if (!request.getHeaders().contains(splitBy)) {
            throw new RuntimeException("Split Excel Data Error: split column doesn't exist");
        }
    }

    @Override
    public InputStream getExcelBodyById(String id) throws FileNotFoundException {

        Optional<ExcelFile> fileInfo = excelRepository.getFileById(id);
        if (fileInfo.isPresent()) {
            File file = new File(fileInfo.get().getDownloadLink());

            return new FileInputStream(file);

        }
        return null;
    }

    @Override
    public ExcelFile generateExcelDataFromRequest(ExcelRequest request) throws IOException {
        validateDate(request);
        var sheets = new ArrayList<ExcelDataSheet>();
        List<ExcelDataHeader> headers = buildSheetHeaders(request);

        var sheet1 = new ExcelDataSheet();
        sheet1.setTitle("First Sheet");
        sheet1.setHeaders(headers);
        sheet1.setDataRows(request.getData());
        sheets.add(sheet1);

        return getExcelFile(request, sheets);
    }

    @Override
    public ExcelFile generateMultiSheetExcelDataFromRequest(MultiSheetExcelRequest request) throws IOException, RuntimeException {
        validateSplitData(request);
        var sheets = new ArrayList<ExcelDataSheet>();
        List<ExcelDataHeader> headers = buildSheetHeaders(request);

        String splitBy = request.getSplitBy();
        int index = 0;
        while (index < request.getHeaders().size()) {
            index++;
            if (request.getHeaders().get(index).equals(splitBy)) {
                break;
            }
        }
        Map<Object, Integer> splitMap = new HashMap<>();
        int sheetNum = 1;

        for (List<Object> row : request.getData()) {
            int sheetsInd;
            var sheet = new ExcelDataSheet();
            List<List<Object>> rows = new ArrayList<>();
            if (splitMap.containsKey(row.get(index))) {
                sheetsInd = splitMap.get(row.get(index));
                sheet = sheets.get(sheetsInd);
                rows = sheet.getDataRows();
                rows.add(row);
                sheet.setDataRows(rows);
                sheets.set(sheetsInd, sheet);
            } else {
                splitMap.put(row.get(index), sheetNum - 1);
                rows.add(row);
                sheet.setDataRows(rows);
                sheet.setHeaders(headers);
                sheet.setTitle(row.get(index).toString());
                sheets.add(sheet);
                sheetNum++;
            }

        }

        return getExcelFile(request, sheets);
    }

    @Override
    public List<ExcelFile> getAllExcels() {
        return excelRepository.getFiles();
    }

    @Override
    public ExcelFile deleteExcelById(String id) throws RuntimeException {
        excelGenerationService.deleteFile(id);
        return excelRepository.deleteFile(id);
    }

    @Override
    public List<ExcelFile> generateBatchExcelDataFromRequest(List<MultiSheetExcelRequest> request) throws IOException {

        for (MultiSheetExcelRequest req : request) {
            validateSplitData(req);
        }
        List<ExcelFile> files = new ArrayList<>();
        for (MultiSheetExcelRequest req : request) {
            ExcelFile f = generateMultiSheetExcelDataFromRequest(req);
            files.add(f);
        }
        return files;
    }

    private List<ExcelDataHeader> buildSheetHeaders(ExcelRequest request) {
        List<ExcelDataHeader> headers = new ArrayList<>();
        for (String h : request.getHeaders()) {
            ExcelDataHeader header = new ExcelDataHeader();
            header.setName(h);
            headers.add(header);
        }
        return headers;
    }

    private ExcelFile getExcelFile(ExcelRequest request, ArrayList<ExcelDataSheet> sheets) throws IOException {
        ExcelData excelData = new ExcelData();
        ExcelFile eFile = new ExcelFile();
        // generate new uuid
        String fileId = eFile.generateId();

        excelData.setSheets(sheets);
        excelData.setTitle(request.getDescription());
        excelData.setGeneratedTime(LocalDateTime.now());

        File file = excelGenerationService.generateExcelReport(excelData, fileId);

        eFile.setGeneratedTime(excelData.getGeneratedTime());
        eFile.setFilesize(file.length());
        eFile.setDownloadLink(file.getPath());

        return excelRepository.saveFile(eFile);
    }

}
