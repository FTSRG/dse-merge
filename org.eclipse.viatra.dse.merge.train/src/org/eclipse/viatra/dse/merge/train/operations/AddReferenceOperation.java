package org.eclipse.viatra.dse.merge.train.operations;

import hu.bme.mit.trainbenchmark.railway.RailwayElement;

import org.eclipse.viatra.dse.merge.model.Reference;
import org.eclipse.viatra.dse.merge.operations.DefaultAddReferenceOperation;
import org.eclipse.viatra.dse.merge.scope.DSEMergeInputScope;
import org.eclipse.viatra.dse.merge.train.util.AddReferenceProcessor;

public class AddReferenceOperation extends AddReferenceProcessor {

	@Override
	public void process(RailwayElement pSrc, RailwayElement pTrg, Reference pChange, DSEMergeInputScope pScope) {
		DefaultAddReferenceOperation.process(pSrc, pTrg, pChange, pScope);
	}
	
}
