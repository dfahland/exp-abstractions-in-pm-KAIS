package org.processmining.models.processtree.analysis;

public class RunExperiment {
  
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.out.println("RunExperiment. Usage:");
      System.out.println("  java org.processmining.models.processtree.analysis.RunExperiment <logRoot> <outputPath>");
      System.out.println("  <logRoot>         root directory for all event logs files to analyze,");
      System.out.println("                    see README for expected structure");
      System.out.println("  <outputPath>      directory to write results to (tree files, PNML files, and statistics)");
      return;
    }
    
    String logRoot = args[0];
    String treeInputPath = args[1];
    String outputPath = args[1];
    
    System.out.println("Running miners");
    RunMiners.main(new String[] {logRoot, treeInputPath} );
    
    System.out.println("Analyzing trees");
    AnalyzeTrees.main(new String[] {treeInputPath} );

    System.out.println("Comparing models");
    CompareMiningResultsPCC.main(new String[] {logRoot, treeInputPath, outputPath} );
  }
}
