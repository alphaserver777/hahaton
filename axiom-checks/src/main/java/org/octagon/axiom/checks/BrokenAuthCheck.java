package org.octagon.axiom.checks;

import java.util.*;
import java.util.stream.Stream;
import org.octagon.axiom.core.*;
import org.octagon.axiom.openapi.OpenApiLoader;
import org.octagon.axiom.openapi.CaseGenerator;

// Проверка: Broken Authentication — доступ без токена
public final class BrokenAuthCheck implements AxiomCheck {
  @Override public String id() { return "BROKEN_AUTH"; }
  @Override public String title() { return "Broken Authentication: доступ без токена"; }
  @Override public Severity defaultSeverity() { return Severity.HIGH; }

  @Override public Stream<TestCase> generate(OpenApiModel model, TargetContext ctx) {
    if (model instanceof OpenApiLoader.SimpleOpenApiModel simple) {
      var base = new CaseGenerator().generate(simple);
      return base.map(tc -> new TestCase(
          "BA-"+tc.id(),
          tc.route(),
          tc.headers(),
          tc.body(),
          Optional.empty(), // без токена
          Map.of("check","BROKEN_AUTH")
      ));
    }
    return Stream.empty();
  }

  @Override public Stream<Finding> analyze(Stream<TestResult> results, TargetContext ctx) {
    return results.filter(r -> Objects.equals(r.testCase().meta().get("check"), "BROKEN_AUTH"))
        .filter(r -> r.exchange().status() >= 200 && r.exchange().status() < 300)
        .map(r -> new Finding(
            "BROKEN_AUTH-"+r.testCase().id(),
            "Доступ без токена к "+r.testCase().route().method()+" "+r.testCase().route().path(),
            Severity.HIGH,
            Confidence.MEDIUM,
            r.testCase().route(),
            Map.of(
                "request", Map.of("method", r.testCase().route().method(), "path", r.testCase().route().path()),
                "response", Map.of("status", r.exchange().status())
            ),
            List.of("Требовать авторизацию для защищённых ресурсов"),
            List.of("OWASP-API2","Authentication")
        ));
  }
}

