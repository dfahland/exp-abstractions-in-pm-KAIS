package org.processmining.models.processtree.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AnalyzeTrees {

  public List<File> listFilesForFolder(final File folder) {
    List<File> files = new LinkedList<>();

    for (final File fileEntry : folder.listFiles()) {
      if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".tree")) {
        files.add(fileEntry);
      }
    }
    return files;
  }
  
  public static String[] stat_categories = { "xor", "optionality", "concurrent", "sequence", "sequence-opt", "loop", "flower", "flower_size", "tau", "interleaved", "or", "or_children", "or_size", "activity" };
  
  public static class TreeStats {
    String name;
    Map<String, Integer> histogram = new HashMap<String, Integer>();
    
    public TreeStats() {
      for (String category : stat_categories) {
        histogram.put(category, 0);
      }
    }
  }
  
  public static class Node {
    String label;
    int depth;
    public ArrayList<Node> children = new ArrayList<Node>();
    public Node parent;
    
    public Node(String label, int depth, Node parent) {
      this.label = label;
      this.depth = depth;
    }
    
    public void addChild(Node n) {
      children.add(n);
      n.parent = this;
    }
    
    public void getStatistics(TreeStats stat) {
      // record operator or "activity"
      if (stat.histogram.containsKey(label)) {
        stat.histogram.put(label, stat.histogram.get(label)+1);
      } else {
        stat.histogram.put("activity", stat.histogram.get("activity")+1);
      }
      
      // record optionality footprints found
      if (this.label.equals("xor")) {
        
        boolean tau_child = false;
        for (Node c : children) if (c.label.equals("tau")) { tau_child = true; break; }
        if (tau_child) {
          
          List<Node> flower_loop_children = new ArrayList<Node>();
          for (Node c : children) if (c.label.equals("loop") && c.children.size() == 1) { flower_loop_children.add(c); }
          if (!flower_loop_children.isEmpty()) {
            stat.histogram.put("flower", stat.histogram.get("flower")+flower_loop_children.size());
            int allchildren = 0;
            LinkedList<Node> queue = new LinkedList<Node>(flower_loop_children);
            while (!queue.isEmpty()) {
              Node n = queue.removeFirst();
              if (n.children.size() == 0 && !n.label.equals("tau")) allchildren++;
              queue.addAll(n.children);
            }
            stat.histogram.put("flower_size",  stat.histogram.get("flower_size")+allchildren);
          } else {
            stat.histogram.put("optionality", stat.histogram.get("optionality")+1);
            
            // record sequence-optionality footprint found
            if (parent != null && parent.label.equals("sequence")) {
              boolean childSequence = false;
              for (Node c : children) { if (c.label.equals("sequence")) { childSequence = true; break; }}
              if (childSequence) stat.histogram.put("sequence-opt", stat.histogram.get("sequence-opt")+1);
            }
          }
        }
      }
      
      // record or-footprint size found
      if (label.equals("or")) {
        stat.histogram.put("or_children",  stat.histogram.get("or_children")+children.size());
        int allchildren = 0;
        LinkedList<Node> queue = new LinkedList<Node>(children);
        while (!queue.isEmpty()) {
          Node n = queue.removeFirst();
          if (n.children.size() == 0 && !n.label.equals("tau")) allchildren++;
          queue.addAll(n.children);
        }
        stat.histogram.put("or_size",  stat.histogram.get("or_size")+allchildren);
      }
      
      for (Node c : children) c.getStatistics(stat);
    }

  }

  public void run(String inputpath) throws Exception {
    
    StringBuffer sb = new StringBuffer();
    sb.append("filename");
    sb.append(",");
    for (String c : stat_categories) {
      sb.append(c);
      sb.append(",");
    }
    sb.append("\n");
    
    List<File> files = listFilesForFolder(new File(inputpath));
    for (File f : files) {
      System.out.println(f);
      
      Node rootNode = null;
      Node[] parents = new Node[1000];
      
      try (BufferedReader br = new BufferedReader(new FileReader(f))) {
        String line;
        int lineNo = 0;
        while ((line = br.readLine()) != null) {
          lineNo++;
          int index = 0;
          while (index < line.length() && line.charAt(index) == '\t') index++;
          
          Node n = new Node(line.substring(index), index, null);
          if (rootNode == null) {
            rootNode = n;
            parents[0] = n;
          } else {
            parents[n.depth-1].addChild(n);
            parents[n.depth] = n;
          }
        }
      }
      
      TreeStats stat = new TreeStats();
      stat.name = f.getName();
      rootNode.getStatistics(stat);
      System.out.println(stat.histogram);
      
      sb.append(stat.name);
      sb.append(",");
      for (String c : stat_categories) {
        sb.append(stat.histogram.get(c));
        sb.append(",");
      }
      sb.append("\n");
      
      
    }
    
    File file = new File(inputpath+"/result.csv");
    BufferedWriter writer = null;
    try {
        writer = new BufferedWriter(new FileWriter(file));
        writer.append(sb);
    } finally {
        if (writer != null) writer.close();
    }
  }

  public static void main(String[] args) throws Exception {
    
    if (args.length < 1) {
      System.out.println("Analyze Trees. Usage:");
      System.out.println("  java org.processmining.models.processtree.analysis.AnalyzeTrees <treeInputPath>");
      System.out.println("  <treeInputPath>   directory containing process trees discovered with RunMiners,");
      System.out.println("                    will write results file to the same directory");
      return;
    }
    
    String inputpath = args[0];
    
    AnalyzeTrees analyze = new AnalyzeTrees();
    analyze.run(inputpath);
  }

}
