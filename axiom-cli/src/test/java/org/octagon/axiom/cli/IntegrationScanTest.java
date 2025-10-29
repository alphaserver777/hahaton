package org.octagon.axiom.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.*;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class IntegrationScanTest {
  @Test
  void scanExampleGeneratesReports() throws Exception {
    Path out = Files.createTempDirectory("axiom-out");
    int code = new CommandLine(new Main()).execute(
        "scan",
        "--openapi", Path.of("specs/example.yaml").toString(),
        "--base-url", "http://localhost",
        "--out", out.toString()
    );
    // CommandLine returns 0 on success (we do not System.exit in success path)
    assertThat(code).isEqualTo(0);
    // Reports are written under run-XXX subdir now
    try (var s = Files.list(out)) {
      var run = s.filter(p -> p.getFileName().toString().startsWith("run-")).findFirst();
      assertThat(run).isPresent();
      var rd = run.get();
      assertThat(Files.exists(rd.resolve("report.json"))).isTrue();
      assertThat(Files.exists(rd.resolve("report.html"))).isTrue();
      assertThat(Files.exists(rd.resolve("report.pdf"))).isTrue();
    }
  }
}
