package org.octagon.axiom.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import picocli.CommandLine;
import org.octagon.axiom.core.*;
import org.octagon.axiom.openapi.OpenApiLoader;
import org.octagon.axiom.openapi.ContractValidators;
import org.octagon.axiom.checks.BrokenAuthCheck;
import org.octagon.axiom.checks.PlaceholderChecks;
import org.octagon.axiom.checks.SecurityHeadersCheck;
import org.octagon.axiom.checks.InjectionCheck;
import org.octagon.axiom.report.*;

@CommandLine.Command(name="scan", description = "Сканирование API по OpenAPI и генерация отчётов")
public class ScanCommand implements Runnable {
  @CommandLine.Option(names = "--openapi", required = true) String openapi;
  @CommandLine.Option(names = "--base-url", required = true) String baseUrl;
  @CommandLine.Option(names = "--config", defaultValue = "axiom.yaml") String configPath;
  @CommandLine.Option(names = "--out", defaultValue = "dist/out") String outDir;
  @CommandLine.Option(names = "--save-exchanges-dir", description = "Каталог для сохранения всех запрос/ответов (по умолчанию RUN/exchanges)")
  String exchangesDir;

  private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());

  @Override public void run() {
    try {
      Config cfg = Files.exists(Path.of(configPath)) ? yaml.readValue(Path.of(configPath).toFile(), Config.class) : new Config();
      if (openapi != null) cfg.target.openapi = openapi;
      if (baseUrl != null) cfg.target.baseUrl = baseUrl;
      if (outDir != null) cfg.report.outDir = outDir;

      var auth = new ArrayList<AuthProfile>();
      if (cfg.target.auth != null && cfg.target.auth.tokens != null) {
        for (var t : cfg.target.auth.tokens) {
          auth.add(new AuthProfile(cfg.target.auth.type, t.role, Map.of("value", envOrValue(t.value))));
        }
      }
      Map<String,Object> extras = new java.util.HashMap<>();
      if (cfg.checks != null && cfg.checks.wordlists != null && cfg.checks.wordlists.injections != null) {
        extras.put("injectionsPath", cfg.checks.wordlists.injections);
      }
      TargetContext ctx = new TargetContext(
          java.net.URI.create(cfg.target.baseUrl),
          auth,
          new ExecutionProfile(cfg.execution.concurrency, cfg.execution.rateLimitRps, cfg.execution.timeoutMs, cfg.execution.retries),
          extras
      );

      var loader = new OpenApiLoader();
      OpenApiModel model = loader.load(cfg.target.openapi);

      var checks = new ArrayList<AxiomCheck>();
      checks.add(new BrokenAuthCheck());
      checks.add(new PlaceholderChecks.BolaCheck());
      checks.add(new InjectionCheck());
      checks.add(new PlaceholderChecks.ExcessiveDataExposureCheck());
      checks.add(new PlaceholderChecks.RateLimitCheck());
      checks.add(new SecurityHeadersCheck());

      var engine = new ScanEngine(new HttpExecutor(ctx.exec()), checks, ContractValidators.basic());
      var outcome = engine.runDetailed(model, ctx);
      var report = outcome.report();

      Path baseOut = Path.of(cfg.report.outDir);
      Path runDir = nextRunDir(baseOut);
      Files.createDirectories(runDir);
      new JsonReportWriter().write(report, runDir.resolve("report.json"));
      new HtmlReportRenderer().render(report, runDir.resolve("report.html"));
      try {
        new PdfExporter().exportFromHtml(runDir.resolve("report.html"), runDir.resolve("report.pdf"));
      } catch (Exception pdfErr) {
        System.err.println("PDF export warning: " + pdfErr.getMessage());
      }
      // Сохраняем все запросы/ответы по эндпоинтам в отдельную папку
      if (exchangesDir != null && !exchangesDir.isBlank()) {
        new org.octagon.axiom.report.ExchangesWriter().writeAll(outcome.results(), Path.of(exchangesDir));
      } else {
        new org.octagon.axiom.report.ExchangesWriter().writeAll(outcome.results(), runDir.resolve("exchanges"));
      }
      System.out.println("OK: run dir " + runDir.toAbsolutePath());
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static Path nextRunDir(Path baseOut) throws java.io.IOException {
    Files.createDirectories(baseOut);
    int max = 0;
    try (var s = java.nio.file.Files.list(baseOut)) {
      for (var p : s.toList()) {
        String name = p.getFileName().toString();
        if (name.startsWith("run-")) {
          try { max = Math.max(max, Integer.parseInt(name.substring(4))); } catch (NumberFormatException ignored) {}
        }
      }
    }
    int next = max + 1;
    return baseOut.resolve(String.format("run-%03d", next));
  }

  private static String envOrValue(String val) {
    if (val == null) return "";
    // поддержка плейсхолдеров ${ENV:default}
    if (val.startsWith("${") && val.endsWith("}")) {
      String inner = val.substring(2, val.length()-1);
      String[] parts = inner.split(":", 2);
      String env = parts[0];
      String def = parts.length>1? parts[1] : "";
      return Optional.ofNullable(System.getenv(env)).orElse(def);
    }
    return val;
  }
}
