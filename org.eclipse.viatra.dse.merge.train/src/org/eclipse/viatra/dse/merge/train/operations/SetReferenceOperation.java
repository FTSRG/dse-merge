package org.eclipse.viatra.dse.merge.train.operations;

import hu.bme.mit.trainbenchmark.railway.RailwayElement;

import org.eclipse.viatra.dse.merge.model.Reference;
import org.eclipse.viatra.dse.merge.operations.DefaultSetReferenceOperation;
import org.eclipse.viatra.dse.merge.scope.DSEMergeScope;
import org.eclipse.viatra.dse.merge.train.util.SetReferenceProcessor;

public class SetReferenceOperation extends SetReferenceProcessor {

	@Override
	public void process(RailwayElement pSrc, RailwayElement pTrg, Reference pChange, DSEMergeScope pScope) {
		DefaultSetReferenceOperation.process(pSrc, pTrg, pChange, pScope);
	}	
}
