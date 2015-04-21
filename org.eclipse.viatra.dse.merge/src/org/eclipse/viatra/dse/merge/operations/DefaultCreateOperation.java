package org.eclipse.viatra.dse.merge.operations;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.viatra.dse.merge.DSEMergeStrategy;
import org.eclipse.viatra.dse.merge.model.Create;
import org.eclipse.viatra.dse.merge.model.Delete;
import org.eclipse.viatra.dse.merge.scope.DSEMergeInputScope;

public class DefaultCreateOperation {

	public static void process(EObject pContainer, Create pChange, DSEMergeInputScope pScope) {
		EObject element = (EObject) EcoreUtil.create(pChange.getClazz());
		EStructuralFeature feature = element.eClass().getEStructuralFeature("id");
		element.eSet(feature, pChange.getSrc());
		
		if(pChange.getFeature().isMany()) {
			@SuppressWarnings("unchecked")
			EList<EObject> list = (EList<EObject>) pContainer.eGet(pChange.getFeature());
			list.add(element);
		} else {
			pContainer.eSet(pChange.getFeature(), element);
		}
		
		update(pScope, pChange);
		
		EcoreUtil.delete(pChange);
	}

	private static void update(DSEMergeInputScope pScope, Create pChange) {
		for(Delete d : DSEMergeStrategy.deleteDependencies.get(pChange.getSrc())) {
			d.setExecutable(false);;
		}
	}
	
}
