package org.octagon.axiom.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ReportController {
  private final ReportsListService service;
  public ReportController(ReportsListService service){this.service=service;}

  @GetMapping("/")
  public String index(Model model) throws IOException {
    model.addAttribute("reports", service.listHtmlReports());
    return "reports";
  }

  @GetMapping("/report/{name}")
  public String show(@PathVariable String name, Model model) throws IOException {
    Path path = Path.of("dist/out").resolve(name);
    String html = Files.readString(path);
    model.addAttribute("name", name);
    model.addAttribute("html", html);
    return "report_view";
  }

  @GetMapping("/dl/{name:.+}")
  public ResponseEntity<ByteArrayResource> download(@PathVariable String name) throws IOException {
    Path file = Path.of("dist/out").resolve(name);
    byte[] bytes = Files.readAllBytes(file);
    var res = new ByteArrayResource(bytes);
    String ct = name.endsWith(".pdf")? MediaType.APPLICATION_PDF_VALUE : (name.endsWith(".json")? MediaType.APPLICATION_JSON_VALUE : MediaType.TEXT_HTML_VALUE);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+name)
        .contentType(MediaType.parseMediaType(ct))
        .body(res);
  }
}

