package org.eclipse.viatra.dse.merge.diff_merge;

import java.util.Collection;

import org.eclipse.emf.diffmerge.api.Role;
import org.eclipse.emf.diffmerge.api.diff.IAttributeValuePresence;
import org.eclipse.emf.diffmerge.api.diff.IDifference;
import org.eclipse.emf.diffmerge.api.diff.IElementPresence;
import org.eclipse.emf.diffmerge.api.diff.IReferenceValuePresence;
import org.eclipse.emf.diffmerge.diffdata.EComparison;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.viatra.dse.merge.model.Attribute;
import org.eclipse.viatra.dse.merge.model.ChangeSet;
import org.eclipse.viatra.dse.merge.model.Id;
import org.eclipse.viatra.dse.merge.model.Kind;
import org.eclipse.viatra.dse.merge.model.ModelFactory;

public class EMFDiffMergeTranslator {

	public static ChangeSet translate(EComparison comparison) {
		ChangeSet changeSet = ModelFactory.eINSTANCE.createChangeSet();
		processTargetDifferences(comparison.getDifferences(Role.TARGET), changeSet);
//		processReferenceDifferences(comparison.getDifferences(Role.REFERENCE), changeSet);

		return changeSet;
	}

//	private static void processReferenceDifferences(List<IDifference> differences, ChangeSet changeSet) {
//		for (IDifference iDifference : differences) {
//			if(iDifference instanceof IElementPresence) {
//				
//			}
//			
//			else if(iDifference instanceof IReferenceValuePresence) {
//				
//			}
//			
//			else if(iDifference instanceof IAttributeValuePresence) {
//				
//			}
//		}
//	}

	private static void processTargetDifferences(Collection<IDifference> differences, ChangeSet changeSet) {
		for (IDifference iDifference : differences) {
			if(iDifference instanceof IElementPresence) {
				
			}
			
			else if(iDifference instanceof IReferenceValuePresence) {
				
			}
			
			else if(iDifference instanceof IAttributeValuePresence) {
				IAttributeValuePresence diff = (IAttributeValuePresence) iDifference;
				
				EObject object = diff.getElementMatch().get(Role.TARGET);
				EStructuralFeature feature = object.eClass().getEStructuralFeature("id");

				Attribute attribute = ModelFactory.eINSTANCE.createAttribute();
				attribute.setFeature(diff.getFeature());
				attribute.setSrc(create(object.eGet(feature)));
				attribute.setValue(diff.getValue());
				attribute.setExecutable(true);
				if(!diff.getFeature().isMany()) {
					attribute.setKind(Kind.SET);
				} else {
					attribute.setKind(Kind.ADD);
				}
				changeSet.getChanges().add(attribute);
			}
		}
	}
	private static Id create(int value) {
		Id id = ModelFactory.eINSTANCE.createId();
		id.setEInt(value);
		return id;
	}
	
	private static Id create(long value) {
		Id id = ModelFactory.eINSTANCE.createId();
		id.setELong(value);
		return id;
	}
	
	private static Id create(String value) {
		Id id = ModelFactory.eINSTANCE.createId();
		id.setEString(value);
		return id;
	}
	
	private static Id create(Object value) {
		if(value instanceof Integer) {
			return create((int)value);
		}
		if(value instanceof Long) {
			return create((long)value);
		}
		if(value instanceof String) {
			return create((String)value);
		}
		return null;
	}
	
}
