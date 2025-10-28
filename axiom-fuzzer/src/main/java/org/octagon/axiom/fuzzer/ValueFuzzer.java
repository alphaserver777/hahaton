package org.octagon.axiom.fuzzer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Генерация значений по типам (минимум)
public final class ValueFuzzer {
  public List<Object> forType(String type, String format) {
    return switch (type == null ? "" : type) {
      case "string" -> List.of("", "a", UUID.randomUUID().toString(), Instant.now().toString(), "x".repeat(1024));
      case "integer" -> List.of(-1, 0, 1, Integer.MAX_VALUE, Integer.MIN_VALUE);
      case "number" -> List.of(-1.0, 0.0, 1.5, Double.NaN, Double.POSITIVE_INFINITY);
      case "boolean" -> List.of(true, false);
      default -> List.of();
    };
  }
}

