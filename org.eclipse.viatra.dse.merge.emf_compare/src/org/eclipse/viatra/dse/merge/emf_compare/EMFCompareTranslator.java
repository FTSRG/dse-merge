package org.eclipse.viatra.dse.merge.emf_compare;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.DifferenceKind;
import org.eclipse.emf.compare.internal.spec.AttributeChangeSpec;
import org.eclipse.emf.compare.internal.spec.ReferenceChangeSpec;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.viatra.dse.merge.model.Attribute;
import org.eclipse.viatra.dse.merge.model.ChangeSet;
import org.eclipse.viatra.dse.merge.model.Create;
import org.eclipse.viatra.dse.merge.model.Delete;
import org.eclipse.viatra.dse.merge.model.Kind;
import org.eclipse.viatra.dse.merge.model.ModelFactory;
import org.eclipse.viatra.dse.merge.model.Reference;

public class EMFCompareTranslator {

	public static ChangeSet translate(Comparison compare) {
		ChangeSet changeSet = ModelFactory.eINSTANCE.createChangeSet();
		processFeatureChangeSpec(compare, changeSet);

		return changeSet;
	}

	private static void processFeatureChangeSpec(Comparison compare,
			ChangeSet changeSet) {
		EList<Diff> list = compare.getDifferences();
		for (Diff diff : list) {
			if (diff instanceof ReferenceChangeSpec) {
				processReferenceChangeSpec((ReferenceChangeSpec) diff,
						changeSet);
			}
			if (diff instanceof AttributeChangeSpec) {
				processAttributeChangeSpec((AttributeChangeSpec) diff,
						changeSet);
			}
		}
	}

	private static void processAttributeChangeSpec(AttributeChangeSpec diff,
			ChangeSet changeSet) {
		
		EObject object = diff.getMatch().getLeft();
		if(object == null)
			return; //A delete operation will come...
		
		EStructuralFeature feature = object.eClass().getEStructuralFeature("id");

		Attribute attribute = ModelFactory.eINSTANCE.createAttribute();
		attribute.setFeature(diff.getAttribute());
		attribute.setSrc((long) object.eGet(feature));
		attribute.setValue(diff.getValue());
		attribute.setExecutable(true);
		
		switch (diff.getKind()) {
		case ADD:
			attribute.setKind(Kind.ADD);
			break;
		case CHANGE: 
			attribute.setKind(Kind.SET);
			break;
		case DELETE:
			attribute.setKind(Kind.REMOVE);
			break;
		default:
			break;
		}
	
		changeSet.getChanges().add(attribute);		
	}

	private static void processReferenceChangeSpec(ReferenceChangeSpec diff, ChangeSet changeSet) {

		if(processIfDelete(diff, changeSet)) return;
		
		if(processIfCreate(diff, changeSet)) return;
		
		EObject object = diff.getMatch().getLeft();
		if (object == null) {
			return; // A delete operation will come
		}
		EStructuralFeature feature = object.eClass().getEStructuralFeature("id");
		
		Reference reference = ModelFactory.eINSTANCE.createReference();
		reference.setFeature(diff.getReference());
		reference.setSrc((long) object.eGet(feature));
		reference.setTrg((long) diff.getValue().eGet(feature));
		reference.setExecutable(true);
		
		if(diff.getMatch().getComparison().getMatch(diff.getValue()).getLeft() == null) {
			reference.setKind(Kind.UNSET);
		} else {
			switch (diff.getKind()) {
			case ADD:
				reference.setKind(Kind.ADD);
				break;
			case CHANGE:
				reference.setKind(Kind.SET);
				break;
			case DELETE:
				reference.setKind(Kind.REMOVE);
				break;
			case MOVE:
				if(diff.getReference().isMany())
					reference. setKind(Kind.ADD);
				else
					reference.setKind(Kind.SET);
				break;
			default:
				break;
			}
		}
		changeSet.getChanges().add(reference);		
	}

	private static boolean processIfCreate(ReferenceChangeSpec diff, ChangeSet changeSet) {
		if(diff.getReference().isContainment() && diff.getKind() != DifferenceKind.DELETE) {
			
			EObject object = diff.getValue();
			EStructuralFeature feature = object.eClass().getEStructuralFeature("id");
			
			Create create = ModelFactory.eINSTANCE.createCreate();
			create.setExecutable(true);
			create.setSrc((long) object.eGet(feature));
			create.setFeature(diff.getReference());
			changeSet.getChanges().add(create);
			
			for(EStructuralFeature f : object.eClass().getEAllStructuralFeatures()) {
				if(f == feature) { 
					continue; 
				}
				if(f instanceof EReference) {
					Object result = object.eGet(f);
					if(result instanceof EList<?>) {
						for(EObject trg : (EList<EObject>) result) {
							EStructuralFeature feature2 = trg.eClass().getEStructuralFeature("id");
							insertReference(f, (long) object.eGet(feature), (long)trg.eGet(feature2), changeSet, Kind.ADD);
						}
					} else if (result != null) {
						EObject trg = (EObject) result;
						EStructuralFeature feature2 = trg.eClass().getEStructuralFeature("id");
						insertReference(f, (long) object.eGet(feature), (long)trg.eGet(feature2), changeSet, Kind.SET);
					}
					
				}
				if(f instanceof EAttribute) {
					Object result = object.eGet(f);
					if(result instanceof EList<?>) {
						for(Object trg : (EList<Object>) result) {
							insertAttribute(f, (long) object.eGet(feature), trg, changeSet, Kind.ADD);
						}
					} else {
						Object trg = (Object) result;
						insertAttribute(f, (long) object.eGet(feature), trg, changeSet, Kind.SET);
					}
				}
			}			
			return true;
		}
		return false;
	}

	private static boolean processIfDelete(ReferenceChangeSpec diff, ChangeSet changeSet) {
		if(diff.getKind() == DifferenceKind.DELETE && diff.getReference().isContainment()) {
			
			EObject object = diff.getValue();
			EStructuralFeature feature = object.eClass().getEStructuralFeature("id");
			
			Delete delete = ModelFactory.eINSTANCE.createDelete();
			delete.setExecutable(true);
			delete.setSrc((long) object.eGet(feature));
			changeSet.getChanges().add(delete);
			return true;
		}
		return false;
	}

	private static void insertReference(EStructuralFeature feature, long src, long trg, ChangeSet set, Kind kind) {
		Reference reference = ModelFactory.eINSTANCE.createReference();
		reference.setFeature(feature);
		reference.setSrc(src);
		reference.setTrg(trg);
		reference.setExecutable(true);
		reference.setKind(kind);
		set.getChanges().add(reference);
	}
	
	private static void insertAttribute(EStructuralFeature feature, long src, Object trg, ChangeSet set, Kind kind) {
		Attribute reference = ModelFactory.eINSTANCE.createAttribute();
		reference.setFeature(feature);
		reference.setSrc(src);
		reference.setValue(trg);
		reference.setExecutable(true);
		reference.setKind(kind);
		set.getChanges().add(reference);
	}

}