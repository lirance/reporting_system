package com.antra.evaluation.reporting_system.pojo.api;

import javax.validation.constraints.NotEmpty;

public class MultiSheetExcelRequest extends ExcelRequest {

    @NotEmpty(message = "SplitBy is mandatory")
    private String splitBy;

    public String getSplitBy() {
        return splitBy;
    }

    public void setSplitBy(String splitBy) {
        this.splitBy = splitBy;
    }
}
