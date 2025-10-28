package org.octagon.axiom.openapi;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.octagon.axiom.core.*;

// Happy-path генерация по маршрутам (минимальная)
public final class CaseGenerator {
  public Stream<TestCase> generate(OpenApiLoader.SimpleOpenApiModel model) {
    return model.routes.stream().map(r -> new TestCase(
        "HP-" + UUID.randomUUID(),
        r,
        Map.of("Accept","application/json"),
        java.util.Optional.empty(),
        java.util.Optional.empty(),
        Map.of("kind","happy-path")
    ));
  }
}

