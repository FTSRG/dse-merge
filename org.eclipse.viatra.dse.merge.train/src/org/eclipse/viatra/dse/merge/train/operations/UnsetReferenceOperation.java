package org.eclipse.viatra.dse.merge.train.operations;

import hu.bme.mit.trainbenchmark.railway.RailwayElement;

import org.eclipse.viatra.dse.merge.model.Reference;
import org.eclipse.viatra.dse.merge.operations.DefaultUnsetReferenceOperation;
import org.eclipse.viatra.dse.merge.scope.DSEMergeScope;
import org.eclipse.viatra.dse.merge.train.util.UnsetReferenceProcessor;

public class UnsetReferenceOperation extends UnsetReferenceProcessor {

	@Override
	public void process(RailwayElement pSrc, Reference pChange, DSEMergeScope pScope) {
		DefaultUnsetReferenceOperation.process(pSrc, pChange, pScope);
	}
}
