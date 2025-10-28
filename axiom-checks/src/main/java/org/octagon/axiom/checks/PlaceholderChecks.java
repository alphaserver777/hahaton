package org.octagon.axiom.checks;

import java.util.stream.Stream;
import org.octagon.axiom.core.*;

// Заглушки для остальных проверок (MVP-скелет)
public final class PlaceholderChecks {
  public static final class BolaCheck implements AxiomCheck {
    public String id() { return "BOLA"; }
    public String title() { return "BOLA/IDOR"; }
    public Severity defaultSeverity() { return Severity.HIGH; }
    public Stream<TestCase> generate(OpenApiModel m, TargetContext c) { return Stream.empty(); }
    public Stream<Finding> analyze(Stream<TestResult> r, TargetContext c) { return Stream.empty(); }
  }
  public static final class InjectionCheck implements AxiomCheck {
    public String id() { return "INJECTION"; }
    public String title() { return "Injection (SQL/NoSQL/Path)"; }
    public Severity defaultSeverity() { return Severity.HIGH; }
    public Stream<TestCase> generate(OpenApiModel m, TargetContext c) { return Stream.empty(); }
    public Stream<Finding> analyze(Stream<TestResult> r, TargetContext c) { return Stream.empty(); }
  }
  public static final class ExcessiveDataExposureCheck implements AxiomCheck {
    public String id() { return "EDE"; }
    public String title() { return "Excessive Data Exposure"; }
    public Severity defaultSeverity() { return Severity.MEDIUM; }
    public Stream<TestCase> generate(OpenApiModel m, TargetContext c) { return Stream.empty(); }
    public Stream<Finding> analyze(Stream<TestResult> r, TargetContext c) { return Stream.empty(); }
  }
  public static final class RateLimitCheck implements AxiomCheck {
    public String id() { return "RATE_LIMIT"; }
    public String title() { return "Rate Limit"; }
    public Severity defaultSeverity() { return Severity.MEDIUM; }
    public Stream<TestCase> generate(OpenApiModel m, TargetContext c) { return Stream.empty(); }
    public Stream<Finding> analyze(Stream<TestResult> r, TargetContext c) { return Stream.empty(); }
  }
  public static final class SecurityHeadersCheck implements AxiomCheck {
    public String id() { return "SEC_HEADERS"; }
    public String title() { return "Security Headers"; }
    public Severity defaultSeverity() { return Severity.LOW; }
    public Stream<TestCase> generate(OpenApiModel m, TargetContext c) { return Stream.empty(); }
    public Stream<Finding> analyze(Stream<TestResult> r, TargetContext c) { return Stream.empty(); }
  }
}

