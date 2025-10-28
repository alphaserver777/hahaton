package org.octagon.axiom.core;

import java.util.stream.Stream;

// SPI интерфейс проверки
public interface AxiomCheck extends AutoCloseable {
  String id();
  String title();
  Severity defaultSeverity();
  Stream<TestCase> generate(OpenApiModel model, TargetContext ctx);
  Stream<Finding> analyze(Stream<TestResult> results, TargetContext ctx);
  @Override default void close() {}
}

