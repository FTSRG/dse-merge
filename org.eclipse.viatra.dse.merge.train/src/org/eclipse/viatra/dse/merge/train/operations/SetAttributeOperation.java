package org.eclipse.viatra.dse.merge.train.operations;

import hu.bme.mit.trainbenchmark.railway.RailwayElement;

import org.eclipse.viatra.dse.merge.model.Attribute;
import org.eclipse.viatra.dse.merge.operations.DefaultSetAttributeOperation;
import org.eclipse.viatra.dse.merge.scope.DSEMergeScope;
import org.eclipse.viatra.dse.merge.train.util.SetAttributeProcessor;

public class SetAttributeOperation extends SetAttributeProcessor {

	@Override
	public void process(RailwayElement pSrc, Attribute pChange, DSEMergeScope pScope) {
		DefaultSetAttributeOperation.process(pSrc, pChange, pScope);
	}	
}
