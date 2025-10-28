package org.octagon.axiom.core;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

// Исполнитель HTTP на базе HttpClient с простым rate-limit и retry
public final class HttpExecutor {
  private final HttpClient client;
  private final ExecutionProfile profile;
  private final Semaphore rateSemaphore;
  private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());
  private final AtomicLong tokens;

  public HttpExecutor(ExecutionProfile profile) {
    this.profile = profile;
    this.client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .connectTimeout(Duration.ofMillis(profile.timeoutMs()))
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();
    this.rateSemaphore = new Semaphore(profile.concurrency());
    this.tokens = new AtomicLong(profile.rateLimitRps());
  }

  private void acquireRate() {
    // Очень простой скользящий токен-бакет
    while (true) {
      long now = System.currentTimeMillis();
      long ws = windowStart.get();
      if (now - ws >= 1000) {
        windowStart.compareAndSet(ws, now);
        tokens.set(profile.rateLimitRps());
      }
      long left = tokens.get();
      if (left > 0 && tokens.compareAndSet(left, left - 1)) break;
      try { Thread.sleep(5); } catch (InterruptedException ignored) { }
    }
  }

  public HttpExchange execute(TestCase tc, URI baseUrl, Optional<String> token) {
    try {
      rateSemaphore.acquire();
      acquireRate();
      var t0 = System.currentTimeMillis();
      // Заменяем шаблонные параметры {id} на безопасные значения
      String path = tc.route().path();
      String resolvedPath = path.replaceAll("\\{[^/]+\\}", "1");
      if (!resolvedPath.startsWith("/")) resolvedPath = "/" + resolvedPath;
      var uri = baseUrl.resolve(resolvedPath);
      var builder = HttpRequest.newBuilder().uri(uri)
          .timeout(Duration.ofMillis(profile.timeoutMs()));
      String method = tc.route().method().toUpperCase(Locale.ROOT);
      if (List.of("POST","PUT","PATCH").contains(method)) {
        builder.method(method, HttpRequest.BodyPublishers.ofString(tc.body().orElse("")));
      } else {
        builder.method(method, HttpRequest.BodyPublishers.noBody());
      }
      var headers = new ArrayList<String>();
      tc.headers().forEach((k,v) -> { headers.add(k); headers.add(v); });
      token.ifPresent(t -> { headers.add("Authorization"); headers.add("Bearer "+t); });
      if (!headers.isEmpty()) builder.headers(headers.toArray(new String[0]));

      int attempts = 0;
      while (true) {
        attempts++;
        try {
          var resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
          var dur = System.currentTimeMillis() - t0;
          String ct = Optional.ofNullable(resp.headers().firstValue("content-type").orElse("application/octet-stream"))
              .orElse("application/octet-stream");
          Map<String, List<String>> h = resp.headers().map();
          String bodyB64 = Base64.getEncoder().encodeToString(resp.body());
          return new HttpExchange(resp.statusCode(), h, Optional.ofNullable(bodyB64), ct, dur);
        } catch (Exception e) {
          if (attempts <= profile.retries()) {
            try { Thread.sleep(100L * attempts); } catch (InterruptedException ignored) {}
            continue;
          }
          var dur = System.currentTimeMillis() - t0;
          return new HttpExchange(599, Map.of(), Optional.of(e.toString()), "text/plain", dur);
        }
      }
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      return new HttpExchange(499, Map.of(), Optional.of("interrupted"), "text/plain", 0);
    } finally {
      rateSemaphore.release();
    }
  }
}
