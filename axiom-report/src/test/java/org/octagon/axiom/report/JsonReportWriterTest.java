package org.octagon.axiom.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.octagon.axiom.core.*;

public class JsonReportWriterTest {
  @Test
  void writesJsonReport() throws Exception {
    var rep = new Report(Map.of("target","http://localhost"), new ReportSummary(0,0,0,10,0), List.of());
    Path out = Files.createTempDirectory("axiom-test").resolve("report.json");
    new JsonReportWriter().write(rep, out);
    assertThat(Files.exists(out)).isTrue();
    String s = Files.readString(out);
    assertThat(s).contains("\"target\":\"http://localhost\"");
  }
}

