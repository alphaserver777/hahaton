package org.octagon.axiom.report;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Экспорт HTML в PDF (openhtmltopdf)
public final class PdfExporter {
  public void exportFromHtml(Path htmlFile, Path pdfOut) throws IOException {
    String html = Files.readString(htmlFile, StandardCharsets.UTF_8);
    try (var os = new FileOutputStream(pdfOut.toFile())) {
      var builder = new PdfRendererBuilder();
      builder.withHtmlContent(html, htmlFile.getParent().toUri().toString());
      builder.toStream(os);
      builder.useFastMode();
      builder.run();
    } catch (Exception e) {
      throw new IOException("PDF export failed", e);
    }
  }
}

