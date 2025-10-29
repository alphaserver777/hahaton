package org.octagon.axiom.checks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import org.octagon.axiom.core.*;

// Простая проверка Injection: подставляем payload в query-параметр q для GET маршрутов
public final class InjectionCheck implements AxiomCheck {
  @Override public String id() { return "INJECTION"; }
  @Override public String title() { return "Injection (SQL/NoSQL/Path)"; }
  @Override public Severity defaultSeverity() { return Severity.HIGH; }

  @Override public Stream<TestCase> generate(OpenApiModel model, TargetContext ctx) {
    if (!(model instanceof org.octagon.axiom.openapi.OpenApiLoader.SimpleOpenApiModel simple)) return Stream.empty();
    String wl = (String) ctx.extras().getOrDefault("injectionsPath", "wordlists/injections.txt");
    List<String> payloads = loadTop(wl, 12);
    return simple.routes.stream()
        .filter(r -> "GET".equalsIgnoreCase(r.method()))
        .flatMap(r -> payloads.stream().map(p -> {
          String sep = r.path().contains("?") ? "&" : "?";
          String path = r.path() + sep + "q=" + urlEnc(p);
          return new TestCase("INJ-"+UUID.randomUUID(), new ApiRoute(r.method(), path), Map.of("Accept","*/*"), Optional.empty(), Optional.empty(), Map.of("check","INJECTION","payload",p));
        }));
  }

  @Override public Stream<Finding> analyze(Stream<TestResult> results, TargetContext ctx) {
    return results
        .filter(r -> Objects.equals(r.testCase().meta().get("check"), "INJECTION"))
        .filter(r -> r.exchange().status() >= 500)
        .map(r -> new Finding(
            "INJ-"+r.testCase().id(),
            "Возможная инъекция: 5xx на запрос с payload",
            Severity.HIGH,
            Confidence.LOW,
            r.testCase().route(),
            Map.of("status", r.exchange().status(), "payload", r.testCase().meta().get("payload")),
            List.of("Проверьте серверные ошибки и обработку входных данных"),
            List.of("OWASP-API8","Injection")
        ));
  }

  private static List<String> loadTop(String path, int n) {
    try {
      var lines = Files.readAllLines(Path.of(path));
      var list = new ArrayList<String>();
      for (var s : lines) {
        s = s.trim();
        if (s.isEmpty() || s.startsWith("#")) continue;
        list.add(s);
        if (list.size() >= n) break;
      }
      return list;
    } catch (IOException e) {
      return List.of("' OR '1'='1", ") or 1=1 --", "${jndi:ldap://x}" );
    }
  }

  private static String urlEnc(String s) {
    try {
      return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    } catch (Exception e) { return s; }
  }
}

