package org.octagon.axiom.cli;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
  public Target target = new Target();
  public Execution execution = new Execution();
  public Checks checks = new Checks();
  public Contract contract = new Contract();
  public Report report = new Report();
  public Ui ui = new Ui();

  public static class Target {
    public String baseUrl;
    public String openapi;
    public Auth auth = new Auth();
  }
  public static class Auth {
    public String type;
    public List<Token> tokens = java.util.List.of();
  }
  public static class Token { public String role; public String value; }
  public static class Execution { public int concurrency=8, rateLimitRps=8, timeoutMs=8000, retries=1; }
  public static class Checks {
    public String severityThreshold="MEDIUM";
    public boolean enableDiscovery=true;
    public Wordlists wordlists = new Wordlists();
  }
  public static class Wordlists { public String paths; public String injections; }
  public static class Contract { public boolean strictAdditionalProperties=true; public boolean validateErrorSchemas=true; }
  public static class Report { public String outDir = "dist/out"; public List<String> formats = java.util.List.of("json", "html", "pdf"); }
  public static class Ui { public boolean enabled=true; public String bind="0.0.0.0"; public int port=8080; }
}

