package org.eclipse.viatra.dse.merge.train.operations;

import hu.bme.mit.trainbenchmark.railway.RailwayElement;

import org.eclipse.viatra.dse.merge.model.Create;
import org.eclipse.viatra.dse.merge.operations.DefaultCreateOperation;
import org.eclipse.viatra.dse.merge.scope.DSEMergeInputScope;
import org.eclipse.viatra.dse.merge.train.util.CreateProcessor;

public class CreateOperation extends CreateProcessor {

	@Override
	public void process(RailwayElement pContainer, Create pChange, DSEMergeInputScope pScope) {
		DefaultCreateOperation.process(pContainer, pChange, pScope);
	}

}
