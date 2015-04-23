package org.eclipse.viatra.dse.merge.operations;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.viatra.dse.merge.model.Delete;
import org.eclipse.viatra.dse.merge.scope.DSEMergeInputScope;

public class DefaultDeleteOperation {

	public static void process(EObject pSrc, Delete pChange, DSEMergeInputScope pScope) {
		EcoreUtil.delete(pSrc);
		EcoreUtil.delete(pChange);
		
		pScope.getCemetery().getObjects().add(pSrc);
	}

}
