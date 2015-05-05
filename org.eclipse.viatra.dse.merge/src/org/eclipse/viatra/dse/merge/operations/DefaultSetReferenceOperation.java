package org.eclipse.viatra.dse.merge.operations;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.viatra.dse.merge.DSEMergeStrategy;
import org.eclipse.viatra.dse.merge.model.Attribute;
import org.eclipse.viatra.dse.merge.model.Change;
import org.eclipse.viatra.dse.merge.model.Delete;
import org.eclipse.viatra.dse.merge.model.Reference;
import org.eclipse.viatra.dse.merge.scope.DSEMergeScope;

public class DefaultSetReferenceOperation {

	public static void process(EObject pSrc, EObject pTrg, Reference pChange, DSEMergeScope pScope) {
		pSrc.eSet(pChange.getFeature(), pTrg);
	
		update(pScope, pChange);
		
		EcoreUtil.delete(pChange);
	}

	private static void update(DSEMergeScope pScope, Reference pChange) {
		for(Delete d : DSEMergeStrategy.deleteDependencies.get(DSEMergeStrategy.getId(pChange.getSrc()))) {
			d.setExecutable(false);;
		}
		
		for(Delete d : DSEMergeStrategy.deleteDependencies.get(DSEMergeStrategy.getId(pChange.getTrg()))) {
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
		if (change instanceof Reference) {
			Reference _change = (Reference) change;
			if (DSEMergeStrategy.getId(_change.getSrc()) == DSEMergeStrategy.getId(pChange.getSrc()) && _change.getFeature() == pChange.getFeature())
				_change.setExecutable(false);
		}
	}
}
