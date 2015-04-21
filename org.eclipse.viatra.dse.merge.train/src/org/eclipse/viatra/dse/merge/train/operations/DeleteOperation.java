package org.eclipse.viatra.dse.merge.train.operations;

import hu.bme.mit.trainbenchmark.railway.RailwayElement;

import org.eclipse.viatra.dse.merge.model.Delete;
import org.eclipse.viatra.dse.merge.operations.DefaultDeleteOperation;
import org.eclipse.viatra.dse.merge.scope.DSEMergeInputScope;
import org.eclipse.viatra.dse.merge.train.util.DeleteProcessor;

public class DeleteOperation extends DeleteProcessor {

	@Override
	public void process(RailwayElement pSrc, Delete pChange, DSEMergeInputScope pScope) {
		DefaultDeleteOperation.process(pSrc, pChange, pScope);
	}

}
