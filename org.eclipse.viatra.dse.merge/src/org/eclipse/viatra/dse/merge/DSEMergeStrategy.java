package org.eclipse.viatra.dse.merge;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.api.IQuerySpecification;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.IncQueryMatcher;
import org.eclipse.viatra.dse.api.strategy.interfaces.IStrategy;
import org.eclipse.viatra.dse.base.DesignSpaceManager;
import org.eclipse.viatra.dse.base.ThreadContext;
import org.eclipse.viatra.dse.designspace.api.ITransition;
import org.eclipse.viatra.dse.merge.model.Attribute;
import org.eclipse.viatra.dse.merge.model.Change;
import org.eclipse.viatra.dse.merge.model.ChangeSet;
import org.eclipse.viatra.dse.merge.model.Create;
import org.eclipse.viatra.dse.merge.model.Delete;
import org.eclipse.viatra.dse.merge.model.Id;
import org.eclipse.viatra.dse.merge.model.Reference;
import org.eclipse.viatra.dse.merge.scope.DSEMergeScope;
import org.eclipse.viatra.dse.objectives.Fitness;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class DSEMergeStrategy implements IStrategy {

	private ThreadContext context;
	private boolean isInterrupted = false;
	public static String MUST_PREFIX = "MUST_";
	public static String MAY_PREFIX = "MAY_";
	private Logger logger = Logger.getLogger(IStrategy.class);
	private Random random = new Random();
	private DesignSpaceManager.FilterOptions filterOptions;
	private boolean backtracked = false;
	private IQuerySpecification<IncQueryMatcher<IPatternMatch>> id2eobject;
	
	public static Multimap<Object, Delete> deleteDependencies = HashMultimap.create();
	
	@Override
	public void init(ThreadContext context) {
		this.context = context;
		filterOptions = new DesignSpaceManager.FilterOptions();
		filterOptions.nothingIfCut().nothingIfGoal().untraversedOnly();
		try {
			initializeDeleteDependencies();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initializeDeleteDependencies() throws Exception {
		DSEMergeScope scope = (DSEMergeScope) context.getEditingDomain().getResourceSet().getResources().get(0).getContents().get(0);
		buildDeleteDependencies(scope.getLocal(), scope.getRemote());
		buildDeleteDependencies(scope.getRemote(), scope.getLocal());
	}

	private void buildDeleteDependencies(ChangeSet from, ChangeSet to) throws Exception {
		Multimap<Object,Object> idsNotToDelete = ArrayListMultimap.create();
		
		for (Change change : from.getChanges()) {
			if(change instanceof Create) {
				findParents(getId(((Create)change).getContainer()), getId(((Create)change).getContainer()), idsNotToDelete);
			}
			else if(change instanceof Attribute) {
				findParents(getId(((Attribute)change).getSrc()), getId(((Attribute)change).getSrc()), idsNotToDelete);
			}
			else if(change instanceof Reference) {
				findParents(getId(((Reference)change).getSrc()), getId(((Reference)change).getSrc()), idsNotToDelete);
				findParents(getId(((Reference)change).getTrg()), getId(((Reference)change).getTrg()), idsNotToDelete);
			}
		}
		
		for (Change change : to.getChanges()) {
			if(change instanceof Delete) {
				Object toDeleteObject = getId(change.getSrc());
				for(Object id : idsNotToDelete.get(toDeleteObject)) {
					deleteDependencies.put(id, (Delete)change);
				}
			}
		}
	}

	private void findParents(Object current, Object original, Multimap<Object, Object> idsNotToDelete) throws Exception {
		idsNotToDelete.put(current, original);
		
		IncQueryEngine engine = context.getIncqueryEngine();
		IncQueryMatcher<IPatternMatch> matcherForCurrent = id2eobject.getMatcher(engine);
		IPatternMatch partialMatchForCurrent = matcherForCurrent.newMatch(null, current);
		Collection<IPatternMatch> matchesForCurrent = matcherForCurrent.getAllMatches(partialMatchForCurrent);
		
		if(matchesForCurrent.isEmpty()) {
			return; // may create related...
		}
		
		if(matchesForCurrent.size() > 1) {
			throw new Exception(); // id has to be unique or not found
		}
		EObject eobject = (EObject) matchesForCurrent.iterator().next().get("eobject");
		if (eobject.eContainer() == null) {
			return; // no more parent...
		}
		EObject parent = eobject.eContainer();
		EStructuralFeature feature = parent.eClass().getEStructuralFeature("id");
		if(feature == null) return;
		findParents(parent.eGet(feature), original, idsNotToDelete);				
	}

	@Override
	public ITransition getNextTransition(boolean lastWasSuccessful) {
		if (isInterrupted) {
			return null;
		}

		DesignSpaceManager dsm = context.getDesignSpaceManager();
		Collection<? extends ITransition> transitions = dsm.getTransitionsFromCurrentState(filterOptions).stream().filter(x -> !x.getId().toString().equals("")).collect(Collectors.toList());
		boolean hasMust = transitions.stream().anyMatch(x -> x.getId().toString().startsWith(MUST_PREFIX));
		
		if(!hasMust && backtracked) {
			return null;
		}

		backtracked = false;
		
		if (hasMust) {
			transitions = transitions.stream().filter(x -> x.getId().toString().startsWith(MUST_PREFIX)).collect(Collectors.toList());
		} 
//		else {
//			transitions = transitions.stream().filter(x -> x.getId().toString().startsWith(MAY_PREFIX)).collect(Collectors.toList());
//		}
		
		// backtrack
		while (transitions == null || transitions.isEmpty()) {
			boolean didUndo = dsm.undoLastTransformation();
			if (!didUndo) {
				return null;
			}

			logger.debug("Backtracking as there aren't anymore transitions from this state: "
					+ dsm.getCurrentState().getId());

			transitions = dsm.getTransitionsFromCurrentState(filterOptions);
			boolean hasMust2 = transitions.stream().anyMatch(x -> x.getId().toString().startsWith(MUST_PREFIX));
			if(hasMust2) {
				transitions = transitions.stream().filter(x -> x.getId().toString().startsWith(MUST_PREFIX)).collect(Collectors.toList());				
			}
//			else {
//				transitions = transitions.stream().filter(x -> x.getId().toString().startsWith(MAY_PREFIX)).collect(Collectors.toList());
//			}
			
		}

//		if (hasMust && transitions.size() > 1 && context.getGlobalContext().canStartNewThread()) {
//			context.getGlobalContext().tryStartNewThread(context, new DSEMergeStrategy());
//		}

		int index = random.nextInt(transitions.size());
		Iterator<? extends ITransition> iterator = transitions.iterator();
		while (iterator.hasNext() && index != 0) {
			index--;
			iterator.next();
		}
		ITransition transition = iterator.next();

		logger.debug("Depth: "
				+ dsm.getTrajectoryInfo().getDepthFromCrawlerRoot()
				+ " Next transition: " + transition.getId() + " From state: "
				+ transition.getFiredFrom().getId());

		return transition;
	}

	@Override
	public void newStateIsProcessed(boolean isAlreadyTraversed,	Fitness fitness, boolean constraintsNotSatisfied) {
		if (isAlreadyTraversed || constraintsNotSatisfied || (fitness.isSatisifiesHardObjectives())) {
			
			boolean hasSolution = fitness.isSatisifiesHardObjectives();
			
			logger.debug("Backtrack. Already traversed: " + isAlreadyTraversed
					+ ". Goal state: " + (fitness.isSatisifiesHardObjectives())
					+ ". Constraints not satisfied: " + constraintsNotSatisfied);

			boolean hasMust = false;
			backtracked = true;
			DesignSpaceManager dsm = context.getDesignSpaceManager();
			if(hasSolution) {
				do {
					if(!dsm.undoLastTransformation())
						return;
					
					Collection<? extends ITransition> transitions = dsm.getTransitionsFromCurrentState(filterOptions);
					hasMust = transitions.stream().anyMatch(x -> x.getId().toString().startsWith(MUST_PREFIX));
				} while (!hasMust && dsm.getTrajectoryInfo().getDepthFromRoot() > 0);
			}
			else {
			if(!dsm.undoLastTransformation())
				return;
			}
		}
	}

	@Override
	public void interrupted() {
		isInterrupted = true;
	}

	public void setId2EObject(IQuerySpecification<IncQueryMatcher<IPatternMatch>> querySpecification) {
		this.id2eobject = querySpecification;
	}

	public static Object getId(Id id) {
		if(id == null)
			return null;
		switch (id.getType()) {
		case EINT: return id.getEInt();
		case ELONG: return id.getELong();
		case ESTRING: return id.getEString();
		default:
			return null;
		}
	}
	
}
