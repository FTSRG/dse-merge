package org.eclipse.viatra.dse.merge.operations;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.viatra.dse.merge.model.Delete;
import org.eclipse.viatra.dse.merge.scope.DSEMergeScope;

public class DefaultDeleteOperation {

	public static void process(EObject pSrc, Delete pChange, DSEMergeScope pScope) {
		EcoreUtil.delete(pSrc);
		EcoreUtil.delete(pChange);
		
		pScope.getCemetery().getObjects().add(pSrc);
	}

}
