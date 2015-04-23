package org.eclipse.viatra.dse.merge.operations;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.viatra.dse.merge.DSEMergeStrategy;
import org.eclipse.viatra.dse.merge.model.Change;
import org.eclipse.viatra.dse.merge.model.Delete;
import org.eclipse.viatra.dse.merge.model.Kind;
import org.eclipse.viatra.dse.merge.model.Reference;
import org.eclipse.viatra.dse.merge.scope.DSEMergeScope;

public class DefaultAddReferenceOperation {

	public static void process(EObject pSrc, EObject pTrg, Reference pChange, DSEMergeScope pScope) {
		@SuppressWarnings("unchecked")
		EList<EObject> list = (EList<EObject>) pSrc.eGet(pChange.getFeature());
		list.add(pTrg);
		
		update(pScope, pChange);
		
		EcoreUtil.delete(pChange);
	}
	
	private static void update(DSEMergeScope pScope, Reference pChange) {
		for(Delete d : DSEMergeStrategy.deleteDependencies.get(pChange.getSrc())) {
			d.setExecutable(false);
		}
		
		for(Delete d : DSEMergeStrategy.deleteDependencies.get(pChange.getTrg())) {
			d.setExecutable(false);
		}
		
		if (pScope.getRemote().getChanges().contains(pChange)) {
			for (Change change : pScope.getLocal().getChanges()) {
				setToFalse(pChange, change);
			}
		}

		if (pScope.getLocal().getChanges().contains(pChange)) {
			for (Change change : pScope.getRemote().getChanges()) {
				setToFalse(pChange, change);
			}
		}
	}

	private static void setToFalse(Reference pChange, Change change) {
		if(change instanceof Reference) {
			Reference _change = (Reference) change;
			if(_change.getKind().equals(Kind.ADD)) {
				if(((EReference)_change.getFeature()).isContainment() && ((EReference)pChange.getFeature()).isContainment()) {
					if(_change.getTrg() == pChange.getTrg()) {
						_change.setExecutable(false);
					}	
				} 
//				else if (_change.getTrg() == pChange.getTrg() && _change.getFeature() == pChange.getFeature() && _change.getSrc() == pChange.getSrc()) {
//					_change.setExecutable(false);
//				}
			}
		}
	}
}
