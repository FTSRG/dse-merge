package org.eclipse.viatra.dse.merge.train.operations;

import hu.bme.mit.trainbenchmark.railway.RailwayElement;

import org.eclipse.viatra.dse.merge.model.Attribute;
import org.eclipse.viatra.dse.merge.operations.DefaultAddAttributeOperation;
import org.eclipse.viatra.dse.merge.scope.DSEMergeInputScope;
import org.eclipse.viatra.dse.merge.train.util.AddAttributeProcessor;

public class AddAttributeOperation extends AddAttributeProcessor {

	@Override
	public void process(RailwayElement pSrc, Attribute pChange,	DSEMergeInputScope pScope) {
		DefaultAddAttributeOperation.process(pSrc, pChange, pScope);
	}


}
