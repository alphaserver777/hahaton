package org.octagon.axiom.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.octagon.axiom.core.ApiRoute;
import org.octagon.axiom.core.OpenApiModel;

// Простая загрузка OpenAPI (YAML/JSON) в минимальную модель для MVP
public final class OpenApiLoader {
  private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

  public OpenApiModel load(String pathOrUrl) throws IOException {
    Map<?,?> tree;
    if (isUrl(pathOrUrl)) {
      try (var in = new URL(pathOrUrl).openStream()) {
        tree = YAML.readValue(in, Map.class);
      }
    } else {
      tree = YAML.readValue(Files.newInputStream(Path.of(pathOrUrl)), Map.class);
    }
    return SimpleOpenApiModel.from(tree);
  }

  private static boolean isUrl(String s) {
    try { URI u = URI.create(s); return u.getScheme()!=null && (s.startsWith("http://")||s.startsWith("https://")); } catch (Exception e) { return false; }
  }

  // Минимальная реализация модели
  public static final class SimpleOpenApiModel implements OpenApiModel {
    public final List<ApiRoute> routes;

    private SimpleOpenApiModel(List<ApiRoute> routes) { this.routes = routes; }

    public static SimpleOpenApiModel from(Map<?,?> root) {
      List<ApiRoute> rs = new ArrayList<>();
      Object paths = root.get("paths");
      if (paths instanceof Map<?,?> pmap) {
        for (var e : pmap.entrySet()) {
          String path = String.valueOf(e.getKey());
          if (e.getValue() instanceof Map<?,?> ops) {
            for (var oe : ops.entrySet()) {
              String method = String.valueOf(oe.getKey()).toUpperCase(Locale.ROOT);
              if (List.of("GET","POST","PUT","PATCH","DELETE","HEAD","OPTIONS").contains(method)) {
                rs.add(new ApiRoute(method, path));
              }
            }
          }
        }
      }
      return new SimpleOpenApiModel(rs);
    }
  }
}

