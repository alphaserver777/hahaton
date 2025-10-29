package org.octagon.axiom.checks;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.octagon.axiom.core.*;

// Проверка заголовков безопасности: HSTS, CORS с cred+*, Server leak
public final class SecurityHeadersCheck implements AxiomCheck {
  @Override public String id() { return "SEC_HEADERS"; }
  @Override public String title() { return "Security Headers"; }
  @Override public Severity defaultSeverity() { return Severity.LOW; }

  @Override public Stream<TestCase> generate(OpenApiModel model, TargetContext ctx) {
    // Не генерим свои кейсы — анализируем все результаты других проверок
    return Stream.empty();
  }

  @Override public Stream<Finding> analyze(Stream<TestResult> results, TargetContext ctx) {
    boolean https = Optional.ofNullable(ctx.baseUrl()).map(URI::getScheme).map("https"::equalsIgnoreCase).orElse(false);
    Map<ApiRoute, TestResult> anyByRoute = results.collect(Collectors.toMap(
        r -> r.testCase().route(), r -> r, (a,b) -> a));
    List<Finding> out = new ArrayList<>();
    for (var e : anyByRoute.entrySet()) {
      var r = e.getValue();
      Map<String, List<String>> h = r.exchange().headers();
      Map<String, String> hs = new HashMap<>();
      h.forEach((k,v) -> hs.put(k==null?"":k.toLowerCase(Locale.ROOT), v==null||v.isEmpty()? "" : v.get(0)));

      // CORS: ACAO * вместе с ACC true — риск
      String acao = hs.getOrDefault("access-control-allow-origin", "");
      String acc = hs.getOrDefault("access-control-allow-credentials", "");
      if ("*".equals(acao.trim()) && "true".equalsIgnoreCase(acc.trim())) {
        out.add(new Finding(
            "SEC-CORS-"+r.testCase().id(),
            "CORS: allow-origin '*' вместе с allow-credentials=true",
            Severity.MEDIUM,
            Confidence.MEDIUM,
            r.testCase().route(),
            Map.of("response.headers", h),
            List.of("Не используйте '*' c allow-credentials=true; укажите конкретный origin"),
            List.of("OWASP-API5","CORS")
        ));
      }

      // HSTS: для https отсутствие Strict-Transport-Security — замечание
      if (https && !hs.containsKey("strict-transport-security")) {
        out.add(new Finding(
            "SEC-HSTS-"+r.testCase().id(),
            "Отсутствует Strict-Transport-Security (HSTS)",
            Severity.LOW,
            Confidence.MEDIUM,
            r.testCase().route(),
            Map.of("response.headers", h),
            List.of("Добавьте заголовок HSTS, напр. max-age=15552000; includeSubDomains"),
            List.of("OWASP-API5","HSTS")
        ));
      }

      // Server leak
      if (hs.containsKey("server")) {
        out.add(new Finding(
            "SEC-SERVER-"+r.testCase().id(),
            "Заголовок Server раскрывает стек",
            Severity.LOW,
            Confidence.LOW,
            r.testCase().route(),
            Map.of("server", hs.get("server")),
            List.of("Уберите/обобщите заголовок Server на edge-прокси"),
            List.of("OWASP-API5","Headers")
        ));
      }
    }
    return out.stream();
  }
}

