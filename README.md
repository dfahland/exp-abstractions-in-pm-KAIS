# exp-abstractions-in-pm-KAIS
The experiment conducted for the paper Sander J.J. Leemans, Dirk Fahland "Information-Preserving Abstractions of Event Data in Process Mining" Knowledge and Information Systems, ISSN: 0219-1377 (Print) 0219-3116 (Online), accepted May 2019

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.3243981.svg)](https://doi.org/10.5281/zenodo.3243981)

## Data

The experiment assumes the following input files to be available

1) Unfiltered Public Event Logs from https://data.4tu.nl/repository/collection:event_logs_real
2) Filtered Public Event Logs of the TKDE Benchmark from http://doi.org/10.4121/uuid:adc42403-9a38-48dc-9f0a-a0a49bfb6371

in the following directory structure 

```
<logroot>
  +- /BPIC11/hospital_log.xes.gz
  +- /BPIC12/financial_log.xes.gz
  +- /BPIC13/BPI_Challenge_2013_incidents.xes.gz
  +- /BPIC13/BPI_Challenge_2013_closed_problems.xes.gz
  +- /BPIC14/Detail Incident Activity.xes.gz
  +- /BPIC14/Detail Incident Activity_complete_cases.xes.gz
  +- /BPIC15/BPIC15_1.xes
  +- /BPIC15/BPIC15_2.xes
  +- /BPIC15/BPIC15_3.xes
  +- /BPIC15/BPIC15_4.xes
  +- /BPIC15/BPIC15_5.xes
  +- /BPIC16/BPI2016_Clicks_Logged_In.xes.gz
  +- /BPIC17/BPI_Challenge_2017.xes.gz
  +- /CoSeLoG_WABO_released/Receipt phase of an environmental permit application process ( WABO ) CoSeLoG project.xes.gz
  +- /CoSeLoG_WABO_released/CoSeLoG WABO 1.xes.gz
  +- /CoSeLoG_WABO_released/CoSeLoG WABO 2.xes.gz
  +- /CoSeLoG_WABO_released/CoSeLoG WABO 3.xes.gz
  +- /CoSeLoG_WABO_released/CoSeLoG WABO 4.xes.gz
  +- /CoSeLoG_WABO_released/CoSeLoG WABO 5.xes.gz
  +- /Roadfines/Road_Traffic_Fine_Management_Process.xes.gz
  +- /Sepsis/Sepsis Cases - Event Log.xes.gz
  +- /TKDE_Benchmark/BPIC12.xes.gz
  +- /TKDE_Benchmark/BPIC13_cp.xes.gz
  +- /TKDE_Benchmark/BPIC13_i.xes.gz
  +- /TKDE_Benchmark/BPIC14_f.xes.gz
  +- /TKDE_Benchmark/BPIC15_1f.xes.gz
  +- /TKDE_Benchmark/BPIC15_2f.xes.gz
  +- /TKDE_Benchmark/BPIC15_3f.xes.gz
  +- /TKDE_Benchmark/BPIC15_4f.xes.gz
  +- /TKDE_Benchmark/BPIC15_5f.xes.gz
  +- /TKDE_Benchmark/BPIC17_f.xes.gz
  +- /TKDE_Benchmark/RTFMP.xes.gz
  +- /TKDE_Benchmark/SEPSIS.xes.gz
```
  
## Running the Experiment

Invoke: java -jar exp-abstractions-in-pm-KAIS.jar <logRoot> <outputPath>

Note: the miners invoked and the logs analyzed are configured in the source files of the experiments. 
