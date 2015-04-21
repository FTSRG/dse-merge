package org.eclipse.viatra.dse.merge;

import java.util.Comparator;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.viatra.dse.merge.model.Attribute;
import org.eclipse.viatra.dse.merge.model.Change;
import org.eclipse.viatra.dse.merge.model.ChangeSet;
import org.eclipse.viatra.dse.merge.model.Create;
import org.eclipse.viatra.dse.merge.model.Priority;
import org.eclipse.viatra.dse.merge.model.Reference;
import org.eclipse.viatra.dse.merge.model.Delete;
import org.eclipse.viatra.dse.merge.queries.ExecutableDeleteChangeMatch;
import org.eclipse.viatra.dse.merge.scope.DSEMergeInputScope;
import org.eclipse.viatra.dse.statecode.IStateSerializer;

public class DSEMergeSerializer implements IStateSerializer {

	private DSEMergeInputScope scope;

	public DSEMergeSerializer(DSEMergeInputScope scope) {
		this.scope = scope;
	}
	
	@Override
	public Object serializeContainmentTree() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Local mods: {\n");
		serializeChangeSet(scope.getLocal(), sb);
		sb.append("\n}");
		sb.append("Remote mods: {\n");
		serializeChangeSet(scope.getRemote(), sb);
		sb.append("\n}");
		
		return sb.toString();
	}

	private void serializeChangeSet(ChangeSet changeSet, StringBuilder sb) {
		ECollections.sort(changeSet.getChanges(), new Comparator<Change>() {

			@Override
			public int compare(Change o1, Change o2) {
				int ret = checkType(o1, o2);
				if(ret == 0)
					return ret;
				
				if(o1 instanceof Create && o2 instanceof Create)
					ret = checkCreate((Create)o1, (Create)o2);
				if(o1 instanceof Delete && o2 instanceof Delete)
					ret = checkDelete((Delete)o1, (Delete)o2);
				if(o1 instanceof Reference && o2 instanceof Reference)
					ret = checkReference((Reference)o1, (Reference)o2);
				if(o1 instanceof Attribute && o2 instanceof Attribute)
					ret = checkAttribute((Attribute)o1, (Attribute)o2);
					
				return ret;
			}

			private int checkAttribute(Attribute o1, Attribute o2) {
				int src = String.valueOf(o1.getSrc()).compareTo(String.valueOf(o2.getSrc()));
				int feature = o1.getFeature().getName().compareTo(o2.getFeature().getName());
				int type = o1.getKind().getName().compareTo(o2.getKind().getName());
				int value = o1.getValue().toString().compareTo(o2.getValue().toString());
				
				return src + feature + type + value;
			}

			private int checkReference(Reference o1, Reference o2) {
				int src = String.valueOf(o1.getSrc()).compareTo(String.valueOf(o2.getSrc()));
				int feature = o1.getFeature().getName().compareTo(o2.getFeature().getName());
				int type = o1.getKind().getName().compareTo(o2.getKind().getName());
				int trg = String.valueOf(o1.getTrg()).compareTo(String.valueOf(o2.getTrg()));
				
				return src + feature + type + trg;
			}

			private int checkDelete(Delete o1, Delete o2) {
				return String.valueOf(o1.getSrc()).compareTo(String.valueOf(o2.getSrc()));
			}

			private int checkCreate(Create o1, Create o2) {
				return String.valueOf(o1.getSrc()).compareTo(String.valueOf(o2.getSrc()));
			}

			private int checkType(Change o1, Change o2) {
				return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
			}
		});
		
		for (Change change : changeSet.getChanges()) {
			sb.append(serializeChange(change));
		}
	}

	private String serializeChange(Change change) {
		String ret = "";
		
		if(change instanceof Create) {
			Create _change = (Create) change;
			ret = "Create{executable=" + _change.isExecutable() + ";srcId=" + _change.getSrc() + ";containerId=" + _change.getContainer() + ";feature="+_change.getFeature().getName() + "}";
		}
		if(change instanceof Delete) {
			Delete _change = (Delete) change;
			ret = "Delete{executable=" + _change.isExecutable() + ";srcId=" + _change.getSrc() + "}";
		}
		if(change instanceof Reference) {
			Reference _change = (Reference) change;
			ret = "Reference{executable=" + _change.isExecutable() + ";srcId=" + _change.getSrc() + ";trgId=" + _change.getTrg() + ";feature="+_change.getFeature().getName() + ";kind=" + _change.getKind() + "}";
		}
		if(change instanceof Attribute) {
			Attribute _change = (Attribute) change;
			ret = "Attribute{executable=" + _change.isExecutable() + ";srcId=" + _change.getSrc() + ";value=" + _change.getValue() + ";feature="+_change.getFeature().getName() + ";kind=" + _change.getKind() + "}";
		}
		if(change.getPriority() == Priority.MUST)
			ret = DSEMergeStrategy.MUST_PREFIX + ret;
		else
			ret = DSEMergeStrategy.MAY_PREFIX + ret;
			
		return ret + "\n";
	}

	@Override
	public Object serializePatternMatch(IPatternMatch match) {
		String ret = "";
		if(match instanceof ExecutableDeleteChangeMatch) return ret;
		Change change = (Change) match.get("change");
		if(change == null) return ret;
		if(change.getPriority() == Priority.MUST)
			ret = DSEMergeStrategy.MUST_PREFIX;
		else
			ret = DSEMergeStrategy.MAY_PREFIX;
				
		
		ret += "Match|" + match.patternName() + "|(";
		for (String param : match.parameterNames()) {
			Object p = match.get(param);
			if(p instanceof DSEMergeInputScope) {
				ret += "scope;";
			}
			else if(p instanceof Change) {
				ret += serializeChange((Change) p) + ";";
			}
			else if(p instanceof EObject) {
				EStructuralFeature feature = ((EObject) p).eClass().getEStructuralFeature("id");
				String id = String.valueOf((long)((EObject) p).eGet(feature));
				ret += id + ";";
			} else {
				ret += p.toString() + ";";
			}
		}
		
		return ret;
	}

	@Override
	public void resetCache() {	}

}