package org.processmining.models.processtree.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.processtree.analysis.imbasic.MiningParametersIMFlower;
import org.processmining.models.processtree.analysis.imbasic.MiningParametersIMOptionality;
import org.processmining.models.processtree.analysis.imbasic.MiningParametersIMOptionalityPartialConcurrentFallthrough;
import org.processmining.models.processtree.analysis.imbasic.MiningParametersIMTrueBasic;
import org.processmining.models.processtree.analysis.imbasic.MiningParametersIMfOptionality;
import org.processmining.models.processtree.analysis.imbasic.MiningParametersIMfOptionalityPartialConcurrentFallthrough;
import org.processmining.models.processtree.analysis.imbasic.MiningParametersIMfTrueBasic;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2HumanReadableString;
import org.processmining.plugins.InductiveMiner.efficienttree.ProcessTree2EfficientTree;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMa;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMf;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMfa;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.plugins.InductiveMiner.reduceacceptingpetrinet.ReduceAcceptingPetriNetKeepLanguage;
import org.processmining.plugins.pnml.exporting.PnmlExportNetToPNML;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.PetrinetWithMarkings;
import org.processmining.projectedrecallandprecision.framework.CompareParameters;
import org.processmining.projectedrecallandprecision.helperclasses.AutomatonFailedException;
import org.processmining.projectedrecallandprecision.helperclasses.EfficientLog;
import org.processmining.projectedrecallandprecision.plugins.CompareLog2EfficientTreePlugin;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult.ProjectedMeasuresFailedException;

public class RunMiners {

  public static EfficientTree runIM(XLog log, MiningParameters params) {
    ProcessTree tree = IMProcessTree.mineProcessTree(log, params);
    EfficientTree et = ProcessTree2EfficientTree.convert(tree);
    return et;
  }

  public static AcceptingPetriNet runIM_PN(XLog log, MiningParameters params) {
    
    Canceller canceller = new Canceller() {
      public boolean isCancelled() {
        return false;
      }
    };
    
    ProcessTree tree = IMProcessTree.mineProcessTree(log, params);
    PetrinetWithMarkings pn = null;
    try {
      
      pn = ProcessTree2Petrinet.convert(tree);
    } catch (NotYetImplementedException e) {
      e.printStackTrace();
    } catch (InvalidProcessTreeException e) {
      e.printStackTrace();
    }

    AcceptingPetriNet a = AcceptingPetriNetFactory.createAcceptingPetriNet(
        pn.petrinet, pn.initialMarking, pn.finalMarking);

    ReduceAcceptingPetriNetKeepLanguage.reduce(a, canceller);

    return a;
  }
  
  static MiningParameters[] params = new MiningParameters[] { 
//      new MiningParametersIM(),
//      new MiningParametersIMOptionality(),      
//      new MiningParametersIMOptionalityPartialConcurrentFallthrough(),
      new MiningParametersIMTrueBasic(), 
      new MiningParametersIMa(),
//      new org.processmining.models.processtree.analysis.imabasic.MiningParametersIMaTrueBasic(),
//      new org.processmining.models.processtree.analysis.imabasic.MiningParametersIMaOptionality(),
//      new org.processmining.models.processtree.analysis.imabasic.MiningParametersIMaOptionalityPartialConcurrentFallthrough(),
      new MiningParametersIMf(),
//      new MiningParametersIMfOptionality(),
//      new MiningParametersIMfOptionalityPartialConcurrentFallthrough(),
//      new MiningParametersIMfTrueBasic(), 
      new MiningParametersIMfa(),
//      new org.processmining.models.processtree.analysis.imabasic.MiningParametersIMfaTrueBasic(),
//      new org.processmining.models.processtree.analysis.imabasic.MiningParametersIMfaOptionality(),
//      new org.processmining.models.processtree.analysis.imabasic.MiningParametersIMfaOptionalityPartialConcurrentFallthrough(),
      new MiningParametersIMFlower()
  };
  
  static String names[] = new String[] { 
//      "im",
//      "im-opt", 
//      "im-opt-pc",
      "im-basic", 
      "ima", 
//      "ima-basic",
//      "ima-basic-opt", 
//      "ima-basic-opt-pc",
      "imf",
//      "imf-opt",
//      "imf-opt-pc",
//      "imf-basic", 
      "imfa", 
//      "imfa-basic",
//      "imfa-basic-opt",
//      "imfa-basic-opt-pc",
      "flower"
  };
  
  public static void run(XUniversalParser parser, String logRoot, String logPath, String logName, String outputPath) throws Exception {

    String logDir = logPath.substring((logRoot+"/").length(),logPath.length()-1);
    String outputFile = outputPath+"/"+logDir+"-"+logName;
    
    System.out.println(logName);
    
    File inFile = new File(logPath+logName);
    if (parser.canParse(inFile)) {
      System.out.println("reading");
      Collection<XLog> logs = parser.parse(inFile);
      for (XLog log : logs) {
        System.out.println("mining");
        
        for (int i=0;i<params.length; i++) {
          System.out.println(names[i]);
          EfficientTree tree = runIM(log,params[i]);
          BufferedWriter result = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile+"-"+names[i]+".tree")));
          result.append(EfficientTree2HumanReadableString.toString(tree));
          result.flush();
          result.close();
          
          AcceptingPetriNet pn_m = runIM_PN(log, params[i]);

          System.out.println("checking "+names[i]);
          double[] p_and_r = compute(log, tree);
          System.out.println(p_and_r[0]+","+p_and_r[1]);
          result_quality_csv.append(logName+","+names[i]+","+p_and_r[0]+","+p_and_r[1]+"\n");
          result_quality_csv.flush();
        }
        break;
      }
    }
    System.out.println("done");

  }
  
  public static double[] compute(XLog log, EfficientTree tree)
      throws ProjectedMeasuresFailedException, AutomatonFailedException,
      InterruptedException {
    CompareParameters parameters = new CompareParameters();
    parameters.setDebugEvery(10000);
    ProMCanceller canceller = new ProMCanceller() {
      public boolean isCancelled() {
        return false;
      }
    };

    EfficientLog eLog = new EfficientLog(log, new XEventNameClassifier());
    ProjectedRecallPrecisionResult result = CompareLog2EfficientTreePlugin
        .measure(eLog, tree, parameters, canceller);

    return new double[] { result.getRecall(), result.getPrecision() };
  }
  
  private static BufferedWriter result_quality_csv;
  
  public static void main(String[] args) throws Exception {
    
    if (args.length < 3) {
      System.out.println("Run Miners. Usage:");
      System.out.println("  java org.processmining.models.processtree.analysis.RunMiners <logRoot> <treeInputPath> <outputPath>");
      System.out.println("  <logRoot>         root directory for all event logs files to analyze,");
      System.out.println("                    see README for expected structure");
      System.out.println("  <treeOutputPath>  directory to write mining results to (as process trees),");
      System.out.println("                    results can be analyzed with AnalyzeTrees and CompareMiningResultsPCC");
      return;
    }
    
    String logRoot = args[0];
    String outputPath = args[1];
    
    XUniversalParser parser = new XUniversalParser();
    
    long time = System.currentTimeMillis()/1000;
    result_quality_csv = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath+"/"+"model_quality"+time+".csv")));
    result_quality_csv.append("log,miner,precision,recall\n");
    
    
    run (parser, logRoot, logRoot+"/BPIC11/", "hospital_log.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/BPIC12/", "financial_log.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/BPIC13/", "BPI_Challenge_2013_incidents.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/BPIC13/", "BPI_Challenge_2013_closed_problems.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/BPIC14/", "Detail Incident Activity.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/BPIC14/", "Detail Incident Activity_complete_cases.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/BPIC15/", "BPIC15_1.xes", outputPath);
    run (parser, logRoot, logRoot+"/BPIC15/", "BPIC15_2.xes", outputPath);
    run (parser, logRoot, logRoot+"/BPIC15/", "BPIC15_3.xes", outputPath);
    run (parser, logRoot, logRoot+"/BPIC15/", "BPIC15_4.xes", outputPath);
    run (parser, logRoot, logRoot+"/BPIC15/", "BPIC15_5.xes", outputPath);
    run (parser, logRoot, logRoot+"/BPIC16/", "BPI2016_Clicks_Logged_In.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/BPIC17/", "BPI_Challenge_2017.xes.gz", outputPath);
//    run (parser, logRoot, logRoot+"/BPIC18/", "BPI Challenge 2018.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/CoSeLoG_WABO_released/", "Receipt phase of an environmental permit application process ( WABO ) CoSeLoG project.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/CoSeLoG_WABO_released/", "CoSeLoG WABO 1.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/CoSeLoG_WABO_released/", "CoSeLoG WABO 2.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/CoSeLoG_WABO_released/", "CoSeLoG WABO 3.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/CoSeLoG_WABO_released/", "CoSeLoG WABO 4.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/CoSeLoG_WABO_released/", "CoSeLoG WABO 5.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/Roadfines/", "Road_Traffic_Fine_Management_Process.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/Sepsis/", "Sepsis Cases - Event Log.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC12.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC13_cp.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC13_i.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC14_f.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC15_1f.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC15_2f.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC15_3f.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC15_4f.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC15_5f.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC17_f.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "RTFMP.xes.gz", outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "SEPSIS.xes.gz", outputPath);
    
    result_quality_csv.flush();
    result_quality_csv.close();
  }
}
