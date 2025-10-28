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
import org.octagon.axiom.report.*;

@CommandLine.Command(name="scan", description = "Сканирование API по OpenAPI и генерация отчётов")
public class ScanCommand implements Runnable {
  @CommandLine.Option(names = "--openapi", required = true) String openapi;
  @CommandLine.Option(names = "--base-url", required = true) String baseUrl;
  @CommandLine.Option(names = "--config", defaultValue = "axiom.yaml") String configPath;
  @CommandLine.Option(names = "--out", defaultValue = "dist/out") String outDir;

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
      TargetContext ctx = new TargetContext(
          java.net.URI.create(cfg.target.baseUrl),
          auth,
          new ExecutionProfile(cfg.execution.concurrency, cfg.execution.rateLimitRps, cfg.execution.timeoutMs, cfg.execution.retries),
          Map.of()
      );

      var loader = new OpenApiLoader();
      OpenApiModel model = loader.load(cfg.target.openapi);

      var checks = new ArrayList<AxiomCheck>();
      checks.add(new BrokenAuthCheck());
      checks.add(new PlaceholderChecks.BolaCheck());
      checks.add(new PlaceholderChecks.InjectionCheck());
      checks.add(new PlaceholderChecks.ExcessiveDataExposureCheck());
      checks.add(new PlaceholderChecks.RateLimitCheck());
      checks.add(new PlaceholderChecks.SecurityHeadersCheck());

      var engine = new ScanEngine(new HttpExecutor(ctx.exec()), checks, ContractValidators.basic());
      var report = engine.run(model, ctx);

      Path out = Path.of(cfg.report.outDir);
      Files.createDirectories(out);
      new JsonReportWriter().write(report, out.resolve("report.json"));
      new HtmlReportRenderer().render(report, out.resolve("report.html"));
      new PdfExporter().exportFromHtml(out.resolve("report.html"), out.resolve("report.pdf"));
      System.out.println("OK: reports in " + out.toAbsolutePath());
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
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

