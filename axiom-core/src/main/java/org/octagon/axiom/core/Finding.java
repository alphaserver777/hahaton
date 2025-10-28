package org.octagon.axiom.core;

import java.util.List;
import java.util.Map;

public record Finding(
    String id,
    String title,
    Severity severity,
    Confidence confidence,
    ApiRoute route,
    Map<String, Object> evidence,
    List<String> remediation,
    List<String> tags
) {}

