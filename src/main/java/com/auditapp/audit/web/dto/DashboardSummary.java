package com.auditapp.audit.web.dto;

import java.util.Map;

/**
 * Metrics for the dashboard cards. "Active" is In Progress or In Review, and "high risk" is an
 * open finding of High or Critical severity, the same rules as the front end's dashboard page.
 */
public record DashboardSummary(
        long totalAudits,
        long activeAudits,
        long completedAudits,
        long openFindings,
        long openHighRiskFindings,
        Map<String, Long> auditsByStatus,
        Map<String, Long> openFindingsBySeverity) {
}
