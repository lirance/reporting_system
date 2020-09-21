package com.antra.evaluation.reporting_system.pojo.api;


import javax.validation.constraints.NotEmpty;
import java.util.List;

public class ExcelRequest {

    @NotEmpty(message = "headers are mandatory")
    private List<String> headers;

    private String description;

    @NotEmpty(message = "data are mandatory")
    private List<List<Object>> data;

    private String submitter;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<List<Object>> getData() {
        return data;
    }

    public void setData(List<List<Object>> data) {
        this.data = data;
    }

    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }


}
