package com.antra.evaluation.reporting_system.pojo.report;

import com.alibaba.fastjson.JSON;

import java.time.LocalDateTime;
import java.util.UUID;

public class ExcelFile {
    String fileId;
    LocalDateTime generatedTime;
    long filesize;
    String DownloadLink;

    public String generateId() {
        fileId = UUID.randomUUID().toString();
        return fileId;
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
        return DownloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        DownloadLink = downloadLink;
    }

//    @Override
//    public boolean equals(Object obj) {
//        return this.hashCode() == obj.hashCode();
//    }
//
//    @Override
//    public int hashCode() {
//
//        return toString().hashCode();
//    }
//
//    @Override
//    public String toString() {
//        return JSON.toJSONString(this);
//    }
}
