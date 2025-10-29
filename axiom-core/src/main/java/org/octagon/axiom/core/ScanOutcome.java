package org.octagon.axiom.core;

import java.util.List;

public record ScanOutcome(Report report, List<TestResult> results) {}

