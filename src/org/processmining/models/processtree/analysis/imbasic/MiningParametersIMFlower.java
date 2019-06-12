package org.processmining.models.processtree.analysis.imbasic;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoDefault;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIM;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIM;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughIM;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterIMi;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessor;

public class MiningParametersIMFlower extends MiningParameters {
	
	/*
	 * No other parameter, except mentioned in this file, has influence on the mined model
	 */
	
	public MiningParametersIMFlower() {

		setLog2LogInfo(new IMLog2IMLogInfoDefault());
		
		setBaseCaseFinders(new ArrayList<BaseCaseFinder>(Arrays.asList(
				new BaseCaseFinderIMFlower()
				)));
		
		setCutFinder(new ArrayList<CutFinder>(Arrays.asList(
				new CutFinderIMFlower()
				)));
		
		setLogSplitter(new LogSplitterIMi());
		
		setFallThroughs(new ArrayList<FallThrough>(Arrays.asList(
				new FallThroughIMTrueBasic()
				)));
		
		setPostProcessors(new ArrayList<PostProcessor>());

	}
	
}
