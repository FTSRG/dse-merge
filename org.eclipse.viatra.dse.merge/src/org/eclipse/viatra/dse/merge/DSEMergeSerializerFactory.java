package org.eclipse.viatra.dse.merge;

import org.eclipse.viatra.dse.statecode.IStateCoder;
import org.eclipse.viatra.dse.statecode.IStateCoderFactory;

public class DSEMergeSerializerFactory implements IStateCoderFactory {

	@Override
	public IStateCoder createStateCoder() {
		return new DSEMergeSerializer();
	}

}
