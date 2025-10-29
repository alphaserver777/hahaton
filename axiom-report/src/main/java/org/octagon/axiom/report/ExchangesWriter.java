package org.octagon.axiom.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.octagon.axiom.core.TestResult;

public final class ExchangesWriter {
  private final ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  public void writeAll(List<TestResult> results, Path outDir) throws IOException {
    Files.createDirectories(outDir);
    int idx = 0;
    for (var r : results) {
      String method = r.testCase().route().method();
      String path = r.testCase().route().path();
      String safe = (method + "_" + path)
          .replaceAll("[^a-zA-Z0-9._-]", "_")
          .replaceAll("_+", "_");
      Path f = outDir.resolve(String.format("%04d_%s.json", ++idx, safe));
      Map<String,Object> doc = new HashMap<>();
      Map<String,Object> req = new HashMap<>();
      req.put("method", method);
      req.put("path", path);
      req.put("headers", r.testCase().headers());
      req.put("body", r.testCase().body().orElse(null));
      req.put("tokenRole", r.testCase().tokenRole().orElse(null));
      Map<String,Object> resp = new HashMap<>();
      resp.put("status", r.exchange().status());
      resp.put("headers", r.exchange().headers());
      resp.put("contentType", r.exchange().contentType());
      resp.put("durationMs", r.exchange().durationMs());
      String b64 = r.exchange().bodyBytesBase64().orElse(null);
      if (b64 != null) {
        try {
          byte[] bytes = Base64.getDecoder().decode(b64);
          String text = new String(bytes, StandardCharsets.UTF_8);
          resp.put("bodyText", text.length() > 8192 ? text.substring(0, 8192) : text);
        } catch (IllegalArgumentException ignore) {
          resp.put("bodyBase64", b64);
        }
      }
      doc.put("request", req);
      doc.put("response", resp);
      doc.put("contractIssues", r.contractIssues());
      om.writeValue(f.toFile(), doc);
    }
  }
}

