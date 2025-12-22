package com.kna.model;

import java.sql.Timestamp;

/**
 * Report model for content moderation
 */
public class Report {
    private int reportId;
    private int reporterUserId;
    private String reportedType; // question, answer, user
    private int reportedId;
    private String reason;
    private String description;
    private String status; // pending, reviewed, resolved, dismissed
    private String adminNotes;
    private Integer resolvedBy;
    private Timestamp createdAt;
    private Timestamp resolvedAt;

    // Constructors
    public Report() {}

    public Report(int reporterUserId, String reportedType, int reportedId, String reason, String description) {
        this.reporterUserId = reporterUserId;
        this.reportedType = reportedType;
        this.reportedId = reportedId;
        this.reason = reason;
        this.description = description;
    }

    // Getters and Setters
    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public int getReporterUserId() {
        return reporterUserId;
    }

    public void setReporterUserId(int reporterUserId) {
        this.reporterUserId = reporterUserId;
    }

    public String getReportedType() {
        return reportedType;
    }

    public void setReportedType(String reportedType) {
        this.reportedType = reportedType;
    }

    public int getReportedId() {
        return reportedId;
    }

    public void setReportedId(int reportedId) {
        this.reportedId = reportedId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public Integer getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(Integer resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Timestamp resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
