package org.octagon.axiom.core;

import java.util.List;

public record TestResult(TestCase testCase, HttpExchange exchange, List<String> contractIssues) {}

