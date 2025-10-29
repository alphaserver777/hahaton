package org.octagon.axiom.cli;

import picocli.CommandLine;

@CommandLine.Command(
    name = "axiom",
    mixinStandardHelpOptions = true,
    version = "0.1.0",
    subcommands = {
        ScanCommand.class,
        FuzzCommand.class,
        CiCommand.class,
        DiscoveryCommand.class
    }
)
public class Main implements Runnable {
  public static void main(String[] args) {
    System.exit(new CommandLine(new Main()).execute(args));
  }
  @Override public void run() { new CommandLine(new Main()).usage(System.out); }
}
