package org.eclipse.viatra.dse.merge.train.operations;

import hu.bme.mit.trainbenchmark.railway.RailwayElement;

import org.eclipse.viatra.dse.merge.model.Attribute;
import org.eclipse.viatra.dse.merge.operations.DefaultRemoveAttributeOperation;
import org.eclipse.viatra.dse.merge.scope.DSEMergeInputScope;
import org.eclipse.viatra.dse.merge.train.util.RemoveAttributeProcessor;

public class RemoveAttributeOperation extends RemoveAttributeProcessor {

	@Override
	public void process(RailwayElement pSrc, Attribute pChange, DSEMergeInputScope pScope) {
		DefaultRemoveAttributeOperation.process(pSrc, pChange, pScope);
	}

}
