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
import org.eclipse.incquery.runtime.exception.IncQueryException;
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
import org.eclipse.viatra.dse.merge.queries.ExecutableDeleteChangeMatch;
import org.eclipse.viatra.dse.merge.queries.ExecutableDeleteChangeMatcher;
import org.eclipse.viatra.dse.merge.queries.util.ExecutableDeleteChangeQuerySpecification;
import org.eclipse.viatra.dse.merge.scope.DSEMergeScope;
import org.eclipse.viatra.dse.objectives.Fitness;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

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
	
	public static Multimap<Object, Delete> deleteDependencies = ArrayListMultimap.create();
	
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

	private void buildDeleteDependencies(ChangeSet from, ChangeSet to)
			throws Exception {
		
		Collection<Object> checkedSet = Sets.newHashSet();
		Multimap<Object,Delete> foundSet = ArrayListMultimap.create();
		
		for (Change change : from.getChanges()) {
			if(change instanceof Create) {
				searchDeleteDependency(getId(((Create)change).getContainer()), checkedSet, foundSet, to);
			}
			else if(change instanceof Delete) {
				searchDeleteDependency(getId(((Delete)change).getSrc()), checkedSet, foundSet, to);
			}
			else if(change instanceof Attribute) {
				searchDeleteDependency(getId(((Attribute)change).getSrc()), checkedSet, foundSet, to);
			}
			else if(change instanceof Reference) {
				searchDeleteDependency(getId(((Reference)change).getSrc()), checkedSet, foundSet, to);
				searchDeleteDependency(getId(((Reference)change).getTrg()), checkedSet, foundSet, to);
			}
		}		
		for(Object key : foundSet.keySet()) {
			deleteDependencies.putAll(key, foundSet.get(key));
		}
	}
	
	private void searchDeleteDependency(Object original, Collection<Object> checkedSet, Multimap<Object,Delete> foundSet, ChangeSet changeSet) throws Exception {
		searchDeleteDependency(original, original, checkedSet, foundSet, changeSet);
	}
	
	private void searchDeleteDependency(Object current, Object original, Collection<Object> checkedSet, Multimap<Object,Delete> foundSet, ChangeSet changeSet) throws Exception {
		if(current.equals(-1)) { // Not identified object
			checkedSet.add(-1L);
		} else if(foundSet.containsKey(current) && current != original) {
			foundSet.putAll(original, foundSet.get(current));
		} else if(!checkedSet.contains(current)) {
			
			IncQueryEngine engine = context.getIncqueryEngine();
			ExecutableDeleteChangeMatcher matcher = ExecutableDeleteChangeQuerySpecification.instance().getMatcher(engine);
			ExecutableDeleteChangeMatch partialMatch = matcher.newMatch(current, null, changeSet);
			Collection<ExecutableDeleteChangeMatch> matches = matcher.getAllMatches(partialMatch);
			
			for (ExecutableDeleteChangeMatch match : matches) {
				foundSet.put(current, match.getChange());
				foundSet.put(original, match.getChange());
			}
			
			passForwardToParent(current, original, checkedSet, foundSet, changeSet);
		}
		checkedSet.add(current);
		return;
	}

	private void passForwardToParent(Object current, Object original, Collection<Object> checkedSet, Multimap<Object, Delete> foundSet, ChangeSet changeSet)
			throws IncQueryException, Exception {
		IncQueryEngine engine = context.getIncqueryEngine();
		
		IncQueryMatcher<IPatternMatch> matcherForCurrent = id2eobject.getMatcher(engine);
		IPatternMatch partialMatchForCurrent = matcherForCurrent.newMatch(null, current);
		Collection<IPatternMatch> matchesForCurrent = matcherForCurrent.getAllMatches(partialMatchForCurrent);
		
		if(matchesForCurrent.size() > 1) {
			throw new Exception(); // id has to be unique
		}
		if(!matchesForCurrent.isEmpty()) { // this is an identified object
			EObject eobject = (EObject) matchesForCurrent.iterator().next().get("eobject");
			if (eobject.eContainer() == null) {
				return; // if no more parent...
			}
			EObject parent = eobject.eContainer();
			if(parent != null) {
				EStructuralFeature feature = parent.eClass().getEStructuralFeature("id");
				if(feature == null) return;
				searchDeleteDependency((Object) parent.eGet(feature), original, checkedSet, foundSet, changeSet);
			}
			
		}
	}
	
	@Override
	public ITransition getNextTransition(boolean lastWasSuccessful) {
		if (isInterrupted) {
			return null;
		}

		DesignSpaceManager dsm = context.getDesignSpaceManager();
		Collection<? extends ITransition> transitions = dsm.getTransitionsFromCurrentState(filterOptions);
		boolean hasMust = transitions.stream().anyMatch(x -> x.getId().toString().startsWith(MUST_PREFIX));
		
		if(!hasMust && backtracked) {
			return null;
		}

		backtracked = false;
		
		if (hasMust) {
			transitions = transitions.stream().filter(x -> x.getId().toString().startsWith(MUST_PREFIX)).collect(Collectors.toList());
		} else {
			transitions = transitions.stream().filter(x -> x.getId().toString().startsWith(MAY_PREFIX)).collect(Collectors.toList());
		}
		
		// backtrack
		while (transitions == null || transitions.isEmpty()) {
			boolean didUndo = dsm.undoLastTransformation();
			if (!didUndo) {
				return null;
			}

			logger.debug("Backtracking as there aren't anymore transitions from this state: "
					+ dsm.getCurrentState().getId());

			transitions = dsm.getTransitionsFromCurrentState(filterOptions);
			transitions = transitions.stream()
					.filter(x -> x.getId().toString().startsWith(MUST_PREFIX))
					.collect(Collectors.toList());
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
			logger.debug("Backtrack. Already traversed: " + isAlreadyTraversed
					+ ". Goal state: " + (fitness.isSatisifiesHardObjectives())
					+ ". Constraints not satisfied: " + constraintsNotSatisfied);

			boolean hasMust = false;
			DesignSpaceManager dsm = context.getDesignSpaceManager();
			do {
				if(!dsm.undoLastTransformation())
					return;
				
				Collection<? extends ITransition> transitions = dsm.getTransitionsFromCurrentState(filterOptions);
				hasMust = transitions.stream().anyMatch(x -> x.getId().toString().startsWith(MUST_PREFIX));
				backtracked = true;
			} while (!hasMust && dsm.getTrajectoryInfo().getDepthFromRoot() > 0);
		}
	}

	@Override
	public void interrupted() {
		isInterrupted = true;
	}

	public void setId2EObject(IQuerySpecification<IncQueryMatcher<IPatternMatch>> querySpecification) {
		this.id2eobject = querySpecification;
	}

	private Object getId(Id id) {
		switch (id.getType()) {
		case EINT: return id.getEInt();
		case ELONG: return id.getELong();
		case ESTRING: return id.getEString();
		default:
			return null;
		}
	}
	
}
