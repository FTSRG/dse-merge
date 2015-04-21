package org.eclipse.viatra.dse.merge.train.operations;

import hu.bme.mit.trainbenchmark.railway.RailwayElement;

import org.eclipse.viatra.dse.merge.model.Reference;
import org.eclipse.viatra.dse.merge.operations.DefaultRemoveReferenceOperation;
import org.eclipse.viatra.dse.merge.scope.DSEMergeInputScope;
import org.eclipse.viatra.dse.merge.train.util.RemoveReferenceProcessor;

public class RemoveReferenceOperation extends RemoveReferenceProcessor {

	@Override
	public void process(RailwayElement pSrc, RailwayElement pTrg, Reference pChange, DSEMergeInputScope pScope) {
		DefaultRemoveReferenceOperation.process(pSrc, pTrg, pChange, pScope);
	}

	
}
