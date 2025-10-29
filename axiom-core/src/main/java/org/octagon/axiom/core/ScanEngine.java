package org.octagon.axiom.core;

import java.util.*;
import java.util.stream.Collectors;

// Движок сканера: запускает генерацию кейсов, выполняет HTTP, валидирует контракт, агрегирует результаты
public final class ScanEngine {
  private final HttpExecutor http;
  private final List<AxiomCheck> checks;
  private final ContractValidator contractValidator;

  public ScanEngine(HttpExecutor http, List<AxiomCheck> checks, ContractValidator contractValidator) {
    this.http = http;
    this.checks = checks;
    this.contractValidator = contractValidator;
  }

  public Report run(OpenApiModel oas, TargetContext ctx) {
    return runDetailed(oas, ctx).report();
  }

  public ScanOutcome runDetailed(OpenApiModel oas, TargetContext ctx) {
    long t0 = System.currentTimeMillis();
    var testCases = checks.stream().flatMap(c -> c.generate(oas, ctx)).toList();
    var results = testCases.parallelStream()
        .map(tc -> {
          Optional<String> token = Optional.empty();
          if (tc.tokenRole().isPresent()) {
            token = ctx.auth().stream()
                .filter(a -> Objects.equals(a.role(), tc.tokenRole().get()))
                .map(a -> a.params().getOrDefault("value", ""))
                .findFirst();
          }
          var ex = http.execute(tc, ctx.baseUrl(), token);
          var issues = contractValidator.validate(tc, ex);
          return new TestResult(tc, ex, issues);
        }).toList();

    var findings = checks.stream().flatMap(c -> c.analyze(results.stream(), ctx)).toList();
    var summary = summarize(findings, t0, testCases.size());
    var report = new Report(meta(ctx), summary, findings);
    return new ScanOutcome(report, results);
  }

  private static Map<String, Object> meta(TargetContext ctx) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("target", ctx.baseUrl().toString());
    m.put("timestamp", new Date().toString());
    m.put("version", "0.1.0");
    return m;
  }

  private static ReportSummary summarize(List<Finding> findings, long t0, int endpoints) {
    int high = (int) findings.stream().filter(f -> f.severity() == Severity.HIGH).count();
    int med = (int) findings.stream().filter(f -> f.severity() == Severity.MEDIUM).count();
    int low = (int) findings.stream().filter(f -> f.severity() == Severity.LOW).count();
    long dur = System.currentTimeMillis() - t0;
    return new ReportSummary(high, med, low, dur, endpoints);
  }
}
