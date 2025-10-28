package org.octagon.axiom.ui;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class ReportsListService {
  private final Path outDir = Path.of("dist/out");

  public List<String> listHtmlReports() throws IOException {
    if (!Files.exists(outDir)) return List.of();
    try (var s = Files.list(outDir)) {
      return s.filter(p -> p.getFileName().toString().endsWith(".html"))
          .map(p -> p.getFileName().toString())
          .collect(Collectors.toList());
    }
  }
}

