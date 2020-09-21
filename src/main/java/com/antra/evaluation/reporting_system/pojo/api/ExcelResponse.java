package com.antra.evaluation.reporting_system.pojo.api;

import com.antra.evaluation.reporting_system.pojo.report.ExcelFile;

import java.time.LocalDateTime;

public class ExcelResponse {
    private String fileId;
    private LocalDateTime generatedTime;
    private long filesize;
    private String downloadLink;
    private String message;

    public void generateResponseFromFile(ExcelFile excelFile) {
        fileId = excelFile.getFileId();
        generatedTime = excelFile.getGeneratedTime();
        filesize = excelFile.getFilesize();
        downloadLink = excelFile.getDownloadLink();
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public LocalDateTime getGeneratedTime() {
        return generatedTime;
    }

    public void setGeneratedTime(LocalDateTime generatedTime) {
        this.generatedTime = generatedTime;
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
