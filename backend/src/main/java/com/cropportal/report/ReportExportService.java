package com.cropportal.report;

import com.cropportal.dto.ReportDashboardResponse;
import com.cropportal.dto.ReportDetailResponse;
import com.cropportal.dto.ReportSummaryResponse;
import com.cropportal.entity.*;
import com.cropportal.exception.ResourceNotFoundException;
import com.cropportal.repository.DiseaseReportRepository;
import com.cropportal.repository.FarmerRepository;
import com.cropportal.repository.TicketRepository;
import com.cropportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ReportExportService {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DiseaseReportRepository diseaseReportRepository;
    private final FarmerRepository farmerRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public ReportDashboardResponse dashboard(Map<String, String> filters) {
        List<ReportSummaryResponse> reports = summaries(filters);
        List<Ticket> tickets = ticketRepository.findAll();
        long healthy = reports.stream().filter(report -> isHealthy(report.diseaseName())).count();
        long blocked = userRepository.findAll().stream().filter(User::isBlocked).count();

        return new ReportDashboardResponse(
                reports.size(),
                farmerRepository.count(),
                farmerRepository.findAll().stream().filter(farmer -> !farmer.getUser().isBlocked()).count(),
                reports.size(),
                healthy,
                reports.size() - healthy,
                tickets.stream().filter(ticket -> ticket.getStatus() == TicketStatus.OPEN
                        || ticket.getStatus() == TicketStatus.AI_ANALYZED
                        || ticket.getStatus() == TicketStatus.UNDER_TREATMENT).count(),
                tickets.stream().filter(ticket -> ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED).count(),
                blocked,
                round(reports.stream().map(ReportSummaryResponse::confidence).filter(Objects::nonNull).mapToDouble(Double::doubleValue).average().orElse(0)),
                top(reports, ReportSummaryResponse::cropName),
                top(reports, ReportSummaryResponse::diseaseName),
                top(reports, ReportSummaryResponse::farmerName),
                reports.stream().filter(report -> sameDay(report.detectionDate(), LocalDate.now())).count(),
                reports.stream().filter(report -> sameMonth(report.detectionDate(), YearMonth.now())).count(),
                reports.stream().filter(report -> sameWeek(report.detectionDate(), LocalDate.now())).count(),
                distribution(reports, ReportSummaryResponse::diseaseName),
                distribution(reports, ReportSummaryResponse::cropName),
                reports.stream().collect(Collectors.groupingBy(report -> monthKey(report.detectionDate()), TreeMap::new, Collectors.counting())),
                distribution(reports, report -> report.severity() == null ? "UNKNOWN" : report.severity().name()),
                distribution(reports, ReportSummaryResponse::district)
        );
    }

    public List<ReportSummaryResponse> summaries(Map<String, String> filters) {
        return summariesFrom(diseaseReportRepository.findAll(), filters);
    }

    public List<ReportSummaryResponse> summariesForFarmer(String email, Map<String, String> filters) {
        return summariesFrom(diseaseReportRepository.findByFarmerUserEmail(email, org.springframework.data.domain.Pageable.unpaged()).getContent(), filters);
    }

    private List<ReportSummaryResponse> summariesFrom(List<DiseaseReport> source, Map<String, String> filters) {
        Map<Long, Ticket> ticketsByReport = ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getDiseaseReport() != null)
                .collect(Collectors.toMap(ticket -> ticket.getDiseaseReport().getId(), Function.identity(), (first, second) -> first));
        return source.stream()
                .map(report -> toSummary(report, ticketsByReport.get(report.getId())))
                .filter(report -> matches(report, filters))
                .sorted(Comparator.comparing(ReportSummaryResponse::detectionDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    public ReportDetailResponse detail(Long reportId) {
        DiseaseReport report = diseaseReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        Ticket ticket = ticketRepository.findAll().stream()
                .filter(item -> item.getDiseaseReport() != null && item.getDiseaseReport().getId().equals(reportId))
                .findFirst()
                .orElse(null);
        Farmer farmer = report.getFarmer();
        Crop crop = report.getCrop();
        return new ReportDetailResponse(
                report.getId(),
                Instant.now(),
                new ReportDetailResponse.FarmerDetails(
                        farmer.getUser().getFullName(),
                        farmer.getUser().getPhone(),
                        farmer.getFarmLocation(),
                        district(farmer.getFarmLocation())
                ),
                new ReportDetailResponse.CropDetails(
                        cropName(report),
                        crop == null ? "Not specified" : crop.getSeason(),
                        crop == null ? "Farmer reported crop" : crop.getDescription()
                ),
                imageUrl(report),
                value(report.getDiseaseName()),
                scientificName(report.getDiseaseName()),
                report.getConfidenceScore(),
                report.getSeverityLevel(),
                value(report.getConfidenceExplanation()),
                readJsonList(report.getSymptoms()),
                readJsonList(report.getCauses()),
                value(report.getTreatment()),
                value(report.getPreventionMeasures()),
                value(report.getRecommendedFertilizers()),
                value(report.getRecommendedPesticides()),
                value(report.getOrganicTreatment()),
                value(report.getExpectedRecoveryTime()),
                ticket == null ? value(report.getTreatment()) : value(ticket.getTreatmentRecommendation()),
                ticket == null ? null : ticket.getStatus(),
                history(report, ticket)
        );
    }

    public String csv(Map<String, String> filters) {
        StringBuilder csv = new StringBuilder("Report ID,Farmer Name,Crop,Disease,Confidence,Severity,District,Detection Date,Status,Ticket Status\n");
        for (ReportSummaryResponse report : summaries(filters)) {
            csv.append(report.reportId()).append(',')
                    .append(escape(report.farmerName())).append(',')
                    .append(escape(report.cropName())).append(',')
                    .append(escape(report.diseaseName())).append(',')
                    .append(percent(report.confidence())).append(',')
                    .append(escape(report.severity() == null ? "" : report.severity().name())).append(',')
                    .append(escape(report.district())).append(',')
                    .append(escape(format(report.detectionDate()))).append(',')
                    .append(escape(report.currentStatus())).append(',')
                    .append(escape(report.ticketStatus() == null ? "NO_TICKET" : report.ticketStatus().name())).append('\n');
        }
        return csv.toString();
    }

    public byte[] excel(Map<String, String> filters) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("Report ID", "Farmer Name", "Mobile Number", "District", "Crop", "Disease", "Confidence", "Severity",
                "Detection Date", "Ticket Status", "Resolution Status", "AI Recommendation Summary"));
        for (ReportSummaryResponse report : summaries(filters)) {
            rows.add(List.of(
                    String.valueOf(report.reportId()),
                    value(report.farmerName()),
                    value(report.mobileNumber()),
                    value(report.district()),
                    value(report.cropName()),
                    value(report.diseaseName()),
                    percent(report.confidence()),
                    report.severity() == null ? "" : report.severity().name(),
                    format(report.detectionDate()),
                    report.ticketStatus() == null ? "NO_TICKET" : report.ticketStatus().name(),
                    value(report.currentStatus()),
                    value(report.aiRecommendationSummary())
            ));
        }
        return xlsx("Disease Reports", rows);
    }

    public byte[] pdf(Map<String, String> filters) {
        List<String> lines = new ArrayList<>();
        lines.add("AI Powered Crop Disease Detection and Farmer Support Portal");
        lines.add("Official Disease Reports Summary");
        lines.add("Generated Date: " + format(Instant.now()));
        lines.add("");
        ReportDashboardResponse dashboard = dashboard(filters);
        lines.add("Total Reports: " + dashboard.totalDiseaseReports() + "    Average AI Confidence: " + percent(dashboard.averageAiConfidence()));
        lines.add("Most Common Disease: " + dashboard.mostCommonDisease() + "    Most Affected Crop: " + dashboard.mostAffectedCrop());
        lines.add("");
        lines.add("Report ID | Farmer | Crop | Disease | Confidence | Severity | District | Ticket");
        for (ReportSummaryResponse report : summaries(filters)) {
            lines.add("#" + report.reportId() + " | " + value(report.farmerName()) + " | " + value(report.cropName()) + " | "
                    + value(report.diseaseName()) + " | " + percent(report.confidence()) + " | " + value(report.severity()) + " | "
                    + value(report.district()) + " | " + (report.ticketStatus() == null ? "NO_TICKET" : report.ticketStatus()));
        }
        lines.add("");
        lines.add("Generated by AI Powered Crop Disease Detection and Farmer Support Portal");
        return simplePdf(lines, "Disease Reports");
    }

    public byte[] reportPdf(Long reportId) {
        ReportDetailResponse report = detail(reportId);
        List<String> lines = new ArrayList<>();
        lines.add("AI Powered Crop Disease Detection and Farmer Support Portal");
        lines.add("Disease Detection Report");
        lines.add("Report Number: #" + report.reportNumber());
        lines.add("Generated Date: " + format(report.generatedDate()));
        lines.add("");
        lines.add("Farmer Information");
        lines.add("Name: " + report.farmer().name());
        lines.add("Mobile: " + value(report.farmer().mobileNumber()));
        lines.add("Location: " + value(report.farmer().location()));
        lines.add("District: " + value(report.farmer().district()));
        lines.add("");
        lines.add("Crop Information");
        lines.add("Crop: " + value(report.crop().name()));
        lines.add("Disease: " + value(report.detectedDisease()));
        lines.add("Scientific Name: " + value(report.scientificName()));
        lines.add("Confidence Score: " + percent(report.confidenceScore()));
        lines.add("Severity: " + value(report.severityLevel()));
        lines.add("");
        lines.add("Symptoms: " + String.join("; ", report.symptoms()));
        lines.add("Causes: " + String.join("; ", report.possibleCauses()));
        lines.add("Treatment Plan: " + value(report.treatmentRecommendation()));
        lines.add("Preventive Measures: " + value(report.preventiveMeasures()));
        lines.add("Recommended Fertilizer: " + value(report.recommendedFertilizers()));
        lines.add("Recommended Pesticide: " + value(report.recommendedPesticides()));
        lines.add("Organic Alternatives: " + value(report.organicAlternatives()));
        lines.add("Recovery Timeline: " + value(report.estimatedRecoveryTime()));
        lines.add("AI Recommendation: " + value(report.aiRecommendation()));
        lines.add("Ticket Status: " + value(report.ticketStatus()));
        lines.add("");
        lines.add("Generated by AI Powered Crop Disease Detection and Farmer Support Portal");
        return simplePdf(lines, "Disease Report #" + report.reportNumber());
    }

    private ReportSummaryResponse toSummary(DiseaseReport report, Ticket ticket) {
        Farmer farmer = report.getFarmer();
        String treatment = StringUtils.hasText(report.getTreatment()) ? report.getTreatment() : report.getPreventionMeasures();
        return new ReportSummaryResponse(
                report.getId(),
                farmer.getUser().getFullName(),
                farmer.getUser().getPhone(),
                district(farmer.getFarmLocation()),
                cropName(report),
                report.getDiseaseName(),
                report.getConfidenceScore(),
                report.getSeverityLevel(),
                farmer.getFarmLocation(),
                report.getCreatedAt(),
                ticket == null ? null : ticket.getStatus(),
                isHealthy(report.getDiseaseName()) ? "HEALTHY" : "DISEASE_DETECTED",
                summarize(treatment),
                summarize(ticket == null ? treatment : ticket.getTreatmentRecommendation()),
                imageUrl(report)
        );
    }

    private boolean matches(ReportSummaryResponse report, Map<String, String> filters) {
        if (filters == null || filters.isEmpty()) return true;
        return contains(report.cropName(), filters.get("crop"))
                && contains(report.diseaseName(), filters.get("disease"))
                && contains(report.farmerName(), filters.get("farmer"))
                && contains(report.district(), filters.get("district"))
                && equalsText(report.severity() == null ? null : report.severity().name(), filters.get("severity"))
                && equalsText(report.ticketStatus() == null ? "NO_TICKET" : report.ticketStatus().name(), filters.get("ticketStatus"))
                && matchesSearch(report, filters.get("search"))
                && inDateRange(report.detectionDate(), filters.get("from"), filters.get("to"));
    }

    private boolean matchesSearch(ReportSummaryResponse report, String search) {
        if (!StringUtils.hasText(search)) return true;
        String haystack = normalizeSearchText(Arrays.asList(
                report.reportId(),
                report.cropName(),
                report.diseaseName(),
                report.farmerName(),
                report.mobileNumber(),
                report.district(),
                report.location(),
                report.currentStatus(),
                report.treatmentSummary(),
                report.aiRecommendationSummary(),
                report.confidence() == null ? null : Math.round(report.confidence() * 100),
                report.severity() == null ? null : report.severity().name(),
                report.ticketStatus() == null ? "NO_TICKET" : report.ticketStatus().name()
        ));
        return Arrays.stream(normalizeSearchText(List.of(search)).split(" "))
                .filter(StringUtils::hasText)
                .allMatch(haystack::contains);
    }

    private String normalizeSearchText(List<?> values) {
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.joining(" "))
                .toLowerCase(Locale.ROOT)
                .replace('_', ' ')
                .replace('-', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean inDateRange(Instant instant, String from, String to) {
        if (instant == null) return true;
        LocalDate date = instant.atZone(ZONE).toLocalDate();
        if (StringUtils.hasText(from) && date.isBefore(LocalDate.parse(from))) return false;
        return !StringUtils.hasText(to) || !date.isAfter(LocalDate.parse(to));
    }

    private Map<String, Long> distribution(List<ReportSummaryResponse> reports, Function<ReportSummaryResponse, String> key) {
        return reports.stream().collect(Collectors.groupingBy(report -> value(key.apply(report)), TreeMap::new, Collectors.counting()));
    }

    private String top(List<ReportSummaryResponse> reports, Function<ReportSummaryResponse, String> key) {
        return distribution(reports, key).entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("N/A");
    }

    private String cropName(DiseaseReport report) {
        if (report.getCrop() != null) return report.getCrop().getName();
        return value(report.getFarmer().getPrimaryCrop());
    }

    private String district(String location) {
        if (!StringUtils.hasText(location)) return "Unknown";
        return location.split(",")[0].trim();
    }

    private boolean isHealthy(String diseaseName) {
        return StringUtils.hasText(diseaseName) && diseaseName.toLowerCase(Locale.ROOT).contains("healthy");
    }

    private boolean sameDay(Instant instant, LocalDate date) {
        return instant != null && instant.atZone(ZONE).toLocalDate().equals(date);
    }

    private boolean sameMonth(Instant instant, YearMonth month) {
        return instant != null && YearMonth.from(instant.atZone(ZONE)).equals(month);
    }

    private boolean sameWeek(Instant instant, LocalDate date) {
        if (instant == null) return false;
        LocalDate value = instant.atZone(ZONE).toLocalDate();
        LocalDate start = date.minusDays(date.getDayOfWeek().getValue() - 1L);
        LocalDate end = start.plusDays(6);
        return !value.isBefore(start) && !value.isAfter(end);
    }

    private String monthKey(Instant instant) {
        return instant == null ? "Unknown" : YearMonth.from(instant.atZone(ZONE)).toString();
    }

    private String imageUrl(DiseaseReport report) {
        return report.getImageUpload() == null ? null : "/api/files/" + report.getImageUpload().getStoredFileName();
    }

    private List<String> history(DiseaseReport report, Ticket ticket) {
        List<String> history = new ArrayList<>();
        history.add("Disease detection completed on " + format(report.getCreatedAt()));
        if (ticket != null) {
            history.add("Support ticket #" + ticket.getId() + " created on " + format(ticket.getCreatedAt()));
            history.add("Ticket status: " + ticket.getStatus());
        }
        return history;
    }

    private List<String> readJsonList(String json) {
        if (!StringUtils.hasText(json)) return List.of();
        String cleaned = json.replace("[", "").replace("]", "").replace("\"", "");
        return Arrays.stream(cleaned.split(",")).map(String::trim).filter(StringUtils::hasText).toList();
    }

    private String scientificName(String diseaseName) {
        if (!StringUtils.hasText(diseaseName)) return "Not available";
        String lower = diseaseName.toLowerCase(Locale.ROOT);
        if (lower.contains("late blight")) return "Phytophthora infestans";
        if (lower.contains("powdery mildew")) return "Erysiphales";
        if (lower.contains("rust")) return "Pucciniales";
        if (lower.contains("bacterial")) return "Bacterial phytopathogen";
        if (lower.contains("healthy")) return "No pathogen detected";
        return "Field diagnosis required";
    }

    private String summarize(String value) {
        if (!StringUtils.hasText(value)) return "No recommendation available";
        return value.length() > 160 ? value.substring(0, 157) + "..." : value;
    }

    private boolean contains(String value, String filter) {
        return !StringUtils.hasText(filter) || value(value).toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
    }

    private boolean equalsText(String value, String filter) {
        return !StringUtils.hasText(filter) || value(value).equalsIgnoreCase(filter);
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private String percent(Double confidence) {
        if (confidence == null) return "";
        return Math.round(confidence * 100) + "%";
    }

    private String percent(double confidence) {
        return Math.round(confidence * 100) + "%";
    }

    private String format(Instant instant) {
        return instant == null ? "" : DATE.format(instant.atZone(ZONE));
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String escape(String value) {
        return '"' + value(value).replace("\"", "\"\"") + '"';
    }

    private byte[] xlsx(String sheetName, List<List<String>> rows) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(out)) {
                put(zip, "[Content_Types].xml", """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                          <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                          <Default Extension="xml" ContentType="application/xml"/>
                          <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                          <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                        </Types>
                        """);
                put(zip, "_rels/.rels", """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                        </Relationships>
                        """);
                put(zip, "xl/workbook.xml", """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                          <sheets><sheet name="%s" sheetId="1" r:id="rId1"/></sheets>
                        </workbook>
                        """.formatted(xml(sheetName)));
                put(zip, "xl/_rels/workbook.xml.rels", """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                        </Relationships>
                        """);
                put(zip, "xl/worksheets/sheet1.xml", sheetXml(rows));
            }
            return out.toByteArray();
        } catch (Exception ex) {
            return csv(Map.of()).getBytes(StandardCharsets.UTF_8);
        }
    }

    private String sheetXml(List<List<String>> rows) {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            xml.append("<row r=\"").append(rowIndex + 1).append("\">");
            List<String> row = rows.get(rowIndex);
            for (int col = 0; col < row.size(); col++) {
                xml.append("<c r=\"").append(cell(col, rowIndex)).append("\" t=\"inlineStr\"><is><t>")
                        .append(xml(row.get(col))).append("</t></is></c>");
            }
            xml.append("</row>");
        }
        return xml.append("</sheetData><autoFilter ref=\"A1:L1\"/></worksheet>").toString();
    }

    private String cell(int col, int row) {
        return String.valueOf((char) ('A' + col)) + (row + 1);
    }

    private void put(ZipOutputStream zip, String path, String content) throws Exception {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String xml(String value) {
        return value(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private byte[] simplePdf(List<String> lines, String title) {
        StringBuilder text = new StringBuilder();
        for (String line : lines) {
            for (String wrapped : wrap(line, 92)) {
                text.append(wrapped).append('\n');
            }
            if (line.isBlank()) text.append('\n');
        }
        String stream = "BT /F1 10 Tf 42 760 Td 14 TL (" + pdfEscape(text.toString()).replace("\n", ") Tj T* (") + ") Tj ET";
        String body = "1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
                + "2 0 obj<</Type/Pages/Count 1/Kids[3 0 R]>>endobj\n"
                + "3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]/Resources<</Font<</F1 4 0 R>>>>/Contents 5 0 R>>endobj\n"
                + "4 0 obj<</Type/Font/Subtype/Type1/BaseFont/Helvetica>>endobj\n"
                + "5 0 obj<</Length " + stream.length() + ">>stream\n" + stream + "\nendstream endobj\n"
                + "6 0 obj<</Title(" + pdfEscape(title) + ")>>endobj\n";
        return ("%PDF-1.4\n" + body + "trailer<</Root 1 0 R/Info 6 0 R>>\n%%EOF").getBytes(StandardCharsets.UTF_8);
    }

    private List<String> wrap(String value, int width) {
        if (value == null || value.length() <= width) return List.of(value(value));
        List<String> lines = new ArrayList<>();
        String remaining = value;
        while (remaining.length() > width) {
            int cut = remaining.lastIndexOf(' ', width);
            if (cut < 20) cut = width;
            lines.add(remaining.substring(0, cut));
            remaining = remaining.substring(cut).trim();
        }
        lines.add(remaining);
        return lines;
    }

    private String pdfEscape(String value) {
        return value(value).replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
