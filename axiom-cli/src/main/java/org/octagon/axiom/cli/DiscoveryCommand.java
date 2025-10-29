package org.octagon.axiom.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import org.octagon.axiom.core.ExecutionProfile;
import org.octagon.axiom.core.HttpExchange;
import org.octagon.axiom.core.HttpExecutor;
import picocli.CommandLine;

@CommandLine.Command(name = "discover", description = "Discovery: перебор путей по словарю (бережно)")
public class DiscoveryCommand implements Runnable {
  @CommandLine.Option(names = "--base-url", required = true) String baseUrl;
  @CommandLine.Option(names = "--config", defaultValue = "axiom.yaml") String configPath;
  @CommandLine.Option(names = "--wordlist") String wordlist;
  @CommandLine.Option(names = "--out", defaultValue = "dist/out") String outDir;
  @CommandLine.Option(names = "--include-status", defaultValue = "200,301,302,401,403") String includeStatus;
  @CommandLine.Option(names = "--budget", defaultValue = "1000") int budget;

  private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
  private final ObjectMapper json = new ObjectMapper();

  @Override public void run() {
    try {
      Config cfg = Files.exists(Path.of(configPath)) ? yaml.readValue(Path.of(configPath).toFile(), Config.class) : new Config();
      String wlPath = Optional.ofNullable(wordlist)
          .orElseGet(() -> cfg.checks != null && cfg.checks.wordlists != null ? cfg.checks.wordlists.paths : null);
      if (wlPath == null) throw new IllegalArgumentException("Не задан wordlist путей (--wordlist или checks.wordlists.paths)");

      List<String> raw = Files.readAllLines(Path.of(wlPath));
      List<String> words = raw.stream()
          .map(String::trim)
          .filter(s -> !s.isEmpty() && !s.startsWith("#"))
          .distinct()
          .collect(Collectors.toList());

      // Расширяем словарь популярными префиксами и well-known путями
      List<String> prefixes = List.of("", "/api", "/v1", "/v2", "/v3");
      Set<String> candidates = new LinkedHashSet<>();
      for (String w : words) {
        for (String pr : prefixes) {
          String c = (pr + "/" + w).replaceAll("//+", "/");
          if (!c.startsWith("/")) c = "/" + c;
          candidates.add(c);
        }
      }
      candidates.addAll(List.of(
          "/v3/api-docs", "/api-docs", "/swagger-ui.html", "/swagger-ui/index.html",
          "/openapi", "/openapi.json", "/health", "/status"
      ));
      // Бюджет на запросы
      int limit = Math.min(candidates.size(), Math.max(1, budget));
      List<String> paths = candidates.stream().limit(limit).toList();

      var profile = new ExecutionProfile(cfg.execution.concurrency, cfg.execution.rateLimitRps, cfg.execution.timeoutMs, cfg.execution.retries);
      var http = new HttpExecutor(profile);
      URI base = URI.create(baseUrl);

      Set<Integer> okStatuses = Arrays.stream(includeStatus.split(","))
          .map(String::trim).filter(s -> !s.isEmpty()).map(Integer::parseInt).collect(Collectors.toSet());

      var results = new ConcurrentLinkedQueue<Map<String, Object>>();

      paths.parallelStream().forEach(p -> {
        try {
          String path = p.startsWith("/") ? p : "/" + p;
          var tc = new org.octagon.axiom.core.TestCase(
              "DISC-"+UUID.randomUUID(),
              new org.octagon.axiom.core.ApiRoute("GET", path),
              Map.of("Accept","*/*"),
              Optional.empty(),
              Optional.empty(),
              Map.of("kind","discovery"));
          HttpExchange ex = http.execute(tc, base, Optional.empty());
          if (okStatuses.contains(ex.status())) {
            Map<String,Object> rec = new LinkedHashMap<>();
            rec.put("path", path);
            rec.put("status", ex.status());
            rec.put("contentType", ex.contentType());
            rec.put("headers", ex.headers());
            results.add(rec);
          }
        } catch (Exception ignored) {}
      });

      Path baseOut = Path.of(outDir);
      Path runDir = nextRunDir(baseOut);
      Files.createDirectories(runDir);
      Path out = runDir.resolve("discovery.json");
      json.writerWithDefaultPrettyPrinter().writeValue(out.toFile(), results);
      System.out.println("Discovery найдено: " + results.size() + " → " + out.toAbsolutePath());
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static Path nextRunDir(Path baseOut) throws IOException {
    Files.createDirectories(baseOut);
    int max = 0;
    try (var s = Files.list(baseOut)) {
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
}
