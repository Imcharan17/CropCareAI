package com.cropportal.controller;

import com.cropportal.dto.ReportDashboardResponse;
import com.cropportal.dto.ReportDetailResponse;
import com.cropportal.dto.ReportSummaryResponse;
import com.cropportal.report.ReportExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportExportService reportExportService;

    @GetMapping
    public List<ReportSummaryResponse> reports(Authentication authentication, @RequestParam Map<String, String> filters) {
        return isAdmin(authentication) ? reportExportService.summaries(filters) : reportExportService.summariesForFarmer(authentication.getName(), filters);
    }

    @GetMapping("/dashboard")
    public ReportDashboardResponse dashboard(@RequestParam Map<String, String> filters) {
        return reportExportService.dashboard(filters);
    }

    @GetMapping("/{reportId}")
    public ReportDetailResponse detail(@PathVariable Long reportId) {
        return reportExportService.detail(reportId);
    }

    @GetMapping("/export.csv")
    public ResponseEntity<String> exportCsv(Authentication authentication, @RequestParam Map<String, String> filters) {
        String csv = isAdmin(authentication) ? reportExportService.csv(filters) : csvForFarmer(authentication.getName(), filters);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=disease-reports.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/export.xlsx")
    public ResponseEntity<byte[]> exportExcel(@RequestParam Map<String, String> filters) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=disease-reports.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(reportExportService.excel(filters));
    }

    @GetMapping("/export.pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestParam Map<String, String> filters) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=disease-reports.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(reportExportService.pdf(filters));
    }

    @GetMapping("/{reportId}/download.pdf")
    public ResponseEntity<byte[]> reportPdf(@PathVariable Long reportId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=disease-report-" + reportId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(reportExportService.reportPdf(reportId));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private String csvForFarmer(String email, Map<String, String> filters) {
        StringBuilder csv = new StringBuilder("Report ID,Farmer Name,Crop,Disease,Confidence,Severity,District,Detection Date,Status,Ticket Status\n");
        for (ReportSummaryResponse report : reportExportService.summariesForFarmer(email, filters)) {
            csv.append(report.reportId()).append(',')
                    .append(report.farmerName()).append(',')
                    .append(report.cropName()).append(',')
                    .append(report.diseaseName()).append(',')
                    .append(report.confidence()).append(',')
                    .append(report.severity()).append(',')
                    .append(report.district()).append(',')
                    .append(report.detectionDate()).append(',')
                    .append(report.currentStatus()).append(',')
                    .append(report.ticketStatus()).append('\n');
        }
        return csv.toString();
    }
}
