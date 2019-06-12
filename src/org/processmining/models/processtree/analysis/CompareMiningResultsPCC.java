package org.processmining.models.processtree.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.processtree.analysis.imbasic.MiningParametersIMFlower;
import org.processmining.models.processtree.analysis.imbasic.MiningParametersIMOptionality;
import org.processmining.models.processtree.analysis.imbasic.MiningParametersIMOptionalityPartialConcurrentFallthrough;
import org.processmining.models.processtree.analysis.imbasic.MiningParametersIMTrueBasic;
import org.processmining.models.processtree.analysis.imbasic.MiningParametersIMfOptionality;
import org.processmining.models.processtree.analysis.imbasic.MiningParametersIMfOptionalityPartialConcurrentFallthrough;
import org.processmining.models.processtree.analysis.imbasic.MiningParametersIMfTrueBasic;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2AcceptingPetriNet;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2HumanReadableString;
import org.processmining.plugins.InductiveMiner.efficienttree.ProcessTree2EfficientTree;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMa;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMf;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMfa;
import org.processmining.plugins.InductiveMiner.plugins.EfficientTreeImportPlugin;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.plugins.InductiveMiner.reduceacceptingpetrinet.ReduceAcceptingPetriNetKeepLanguage;
import org.processmining.plugins.pnml.base.Pnml;
import org.processmining.plugins.pnml.base.Pnml.PnmlType;
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

public class CompareMiningResultsPCC {
  
  public static void writeNet(String file, AcceptingPetriNet net) throws Exception {
    GraphLayoutConnection layout = new GraphLayoutConnection(net.getNet());
    
    HashMap<PetrinetGraph, Marking> markedNets = new HashMap<PetrinetGraph, Marking>();
    markedNets.put(net.getNet(), net.getInitialMarking());
    Pnml pnml = new Pnml().convertFromNet(markedNets, layout);
    pnml.setType(PnmlType.PNML);
    String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + pnml.exportElement(pnml);
    text = text.replaceAll("& ", "&amp; ");

    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
    bw.write(text);
    bw.close();
  }
  
  static String names[] = new String[] { 
//      "im",
//      "im-opt", 
//      "im-opt-pc",
//      "im-basic", 
//      "ima", 
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
//      "flower"
  };

  public static void run(XUniversalParser parser, String logRoot, String logPath, String logName, String inputPath, String outputPath) throws Exception {

    String logDir = logPath.substring((logRoot+"/").length(),logPath.length()-1);
    String inputFile = inputPath+logDir+"-"+logName;
    String outputFile = outputPath+logDir+"-"+logName;
    
    System.out.println("reading log "+logName);
    
    File inFile = new File(logPath+logName);
    if (parser.canParse(inFile)) {
      
      Collection<XLog> logs = parser.parse(inFile);
      for (XLog log : logs) {
        
        for (int i=0;i<names.length; i++) {
          System.out.println("reading model "+names[i]);
          
          File model = new File(inputFile+"-"+names[i]+".tree");
          EfficientTree tree = EfficientTreeImportPlugin.importFromFile(model);
          
          System.out.println("Writing PNML");
          AcceptingPetriNet pn_m = EfficientTree2AcceptingPetriNet.convert(tree);
          writeNet(outputFile+"-"+names[i]+".pnml", pn_m);

          System.out.println("checking "+names[i]);
          long start = System.currentTimeMillis();
          double[] p_and_r = compute(log, tree);
          long end = System.currentTimeMillis();
          String result = p_and_r[0]+","+p_and_r[1]+","+(end-start);
          System.out.println(result);
          result_quality_csv.append(logName+","+names[i]+","+result+"\n");
          
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
    parameters.setK(2);
    parameters.setDebugEvery(100);
    parameters.setDebug(true);
    parameters.setMultiThreading(true);
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
      System.out.println("Compare Mining Results PCC. Usage:");
      System.out.println("  java org.processmining.models.processtree.analysis.CompareMiningResultsPCC <logRoot> <treeInputPath> <outputPath>");
      System.out.println("  <logRoot>         root directory for all event logs files to analyze,");
      System.out.println("                    see README for expected structure");
      System.out.println("  <treeInputPath>   directory containing process trees discovered with RunMiners");
      System.out.println("  <outputPath>      directory to write results to (PNML files and statistics)");
      return;
    }
    
    String logRoot = args[0];
    String treeInputPath = args[1];
    String outputPath = args[2];
    
    XUniversalParser parser = new XUniversalParser();
    
    long time = System.currentTimeMillis()/1000;
    result_quality_csv = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath+"model_quality"+time+".csv")));
    result_quality_csv.append("log,miner,precision,recall\n");

    run (parser, logRoot, logRoot+"/BPIC11/", "hospital_log.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/BPIC12/", "financial_log.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/BPIC13/", "BPI_Challenge_2013_incidents.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/BPIC13/", "BPI_Challenge_2013_closed_problems.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/BPIC14/", "Detail Incident Activity.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/BPIC14/", "Detail Incident Activity_complete_cases.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/BPIC15/", "BPIC15_1.xes", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/BPIC15/", "BPIC15_2.xes", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/BPIC15/", "BPIC15_3.xes", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/BPIC15/", "BPIC15_4.xes", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/BPIC15/", "BPIC15_5.xes", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/BPIC16/", "BPI2016_Clicks_Logged_In.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/BPIC17/", "BPI_Challenge_2017.xes.gz", treeInputPath, outputPath);
//    run (parser, logRoot, logRoot+"/BPIC18/", "BPI Challenge 2018.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/CoSeLoG_WABO_released/", "Receipt phase of an environmental permit application process ( WABO ) CoSeLoG project.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/CoSeLoG_WABO_released/", "CoSeLoG WABO 1.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/CoSeLoG_WABO_released/", "CoSeLoG WABO 2.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/CoSeLoG_WABO_released/", "CoSeLoG WABO 3.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/CoSeLoG_WABO_released/", "CoSeLoG WABO 4.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/CoSeLoG_WABO_released/", "CoSeLoG WABO 5.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/Roadfines/", "Road_Traffic_Fine_Management_Process.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/Sepsis/", "Sepsis Cases - Event Log.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC12.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC13_cp.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC13_i.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC14_f.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC15_1f.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC15_2f.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC15_3f.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC15_4f.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC15_5f.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "BPIC17_f.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "RTFMP.xes.gz", treeInputPath, outputPath);
    run (parser, logRoot, logRoot+"/TKDE_Benchmark/", "SEPSIS.xes.gz", treeInputPath, outputPath);
    
    result_quality_csv.flush();
    result_quality_csv.close();
  }
}
