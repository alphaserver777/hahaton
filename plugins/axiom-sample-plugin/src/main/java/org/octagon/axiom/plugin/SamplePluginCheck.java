package org.octagon.axiom.plugin;

import java.util.stream.Stream;
import org.octagon.axiom.core.*;

// Пример плагина, реализующего SPI
public final class SamplePluginCheck implements AxiomCheck {
  @Override public String id() { return "SAMPLE"; }
  @Override public String title() { return "Sample Plugin Check"; }
  @Override public Severity defaultSeverity() { return Severity.LOW; }
  @Override public Stream<TestCase> generate(OpenApiModel model, TargetContext ctx) { return Stream.empty(); }
  @Override public Stream<Finding> analyze(Stream<TestResult> results, TargetContext ctx) { return Stream.empty(); }
}

