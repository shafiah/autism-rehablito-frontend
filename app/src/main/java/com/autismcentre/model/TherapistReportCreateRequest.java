
package com.autismcentre.model;

public class TherapistReportCreateRequest {

    private String reportTitle;
    private String reportContent;

    public TherapistReportCreateRequest(
            String reportTitle,
            String reportContent
    ) {
        this.reportTitle = reportTitle;
        this.reportContent = reportContent;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public String getReportContent() {
        return reportContent;
    }

    public void setReportContent(String reportContent) {
        this.reportContent = reportContent;
    }
}
