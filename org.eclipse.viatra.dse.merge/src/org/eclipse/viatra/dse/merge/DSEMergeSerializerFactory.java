package org.eclipse.viatra.dse.merge;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.viatra.dse.merge.scope.DSEMergeInputScope;
import org.eclipse.viatra.dse.statecode.IStateSerializer;
import org.eclipse.viatra.dse.statecode.IStateSerializerFactory;

public class DSEMergeSerializerFactory implements IStateSerializerFactory {

	@Override
	public IStateSerializer createStateSerializer(Notifier modelRoot)
			throws UnsupportedMetaModel {
		return new DSEMergeSerializer((DSEMergeInputScope) modelRoot);
	}

}
