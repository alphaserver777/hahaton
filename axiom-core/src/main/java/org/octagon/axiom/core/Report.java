package org.octagon.axiom.core;

import java.util.List;
import java.util.Map;

public record Report(
    Map<String, Object> meta,
    ReportSummary summary,
    List<Finding> findings
) {}

