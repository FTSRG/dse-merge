package org.eclipse.viatra.dse.merge.operations;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.viatra.dse.merge.DSEMergeStrategy;
import org.eclipse.viatra.dse.merge.model.Attribute;
import org.eclipse.viatra.dse.merge.model.Change;
import org.eclipse.viatra.dse.merge.model.Delete;
import org.eclipse.viatra.dse.merge.scope.DSEMergeScope;

public class DefaultAddAttributeOperation {

	public static void process(EObject pSrc, Attribute pChange,
			DSEMergeScope pScope) {
		
		@SuppressWarnings("unchecked")
		EList<Object> list = (EList<Object>) pSrc.eGet(pChange.getFeature());
		list.add(pChange.getValue());

		update(pScope, pChange);

		EcoreUtil.delete(pChange);
	}

	private static void update(DSEMergeScope pScope, Attribute pChange) {
		for(Delete d : DSEMergeStrategy.deleteDependencies.get(DSEMergeStrategy.getId(pChange.getSrc()))) {
			d.setExecutable(false);;
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

	private static void setToFalse(Attribute pChange, Change change) {
		if (change instanceof Attribute) {
			Attribute _change = (Attribute) change;
			if (DSEMergeStrategy.getId(_change.getSrc()) == DSEMergeStrategy.getId(pChange.getSrc()) && _change.getFeature() == pChange.getFeature() && _change.getValue().equals(pChange.getValue()))
				_change.setExecutable(false);
		}
	}

}
