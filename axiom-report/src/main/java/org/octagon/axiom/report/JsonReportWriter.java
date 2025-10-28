package org.octagon.axiom.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.octagon.axiom.core.Report;

// Запись отчёта в JSON
public final class JsonReportWriter {
  private final ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  public void write(Report report, Path out) throws IOException {
    Files.createDirectories(out.getParent());
    om.writeValue(out.toFile(), report);
  }
}

