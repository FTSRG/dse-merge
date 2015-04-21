package org.eclipse.viatra.dse.merge;

import java.util.Collection;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.incquery.runtime.api.IMatchProcessor;
import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.api.IQuerySpecification;
import org.eclipse.incquery.runtime.api.IncQueryMatcher;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import org.eclipse.viatra.dse.api.DesignSpaceExplorer;
import org.eclipse.viatra.dse.api.Solution;
import org.eclipse.viatra.dse.api.SolutionTrajectory;
import org.eclipse.viatra.dse.api.TransformationRule;
import org.eclipse.viatra.dse.base.DesignSpaceManager;
import org.eclipse.viatra.dse.merge.model.ChangeSet;
import org.eclipse.viatra.dse.merge.model.ModelPackage;
import org.eclipse.viatra.dse.merge.queries.ExecutableDeleteChangeMatch;
import org.eclipse.viatra.dse.merge.queries.util.ExecutableDeleteChangeQuerySpecification;
import org.eclipse.viatra.dse.merge.scope.DSEMergeInputScope;
import org.eclipse.viatra.dse.merge.scope.ScopeFactory;
import org.eclipse.viatra.dse.merge.scope.ScopePackage;
import org.eclipse.viatra.dse.objectives.impl.ModelQueriesHardObjective;
import org.eclipse.viatra.dse.objectives.impl.ModelQueryType;

public class DSEMergeManager {

	private ChangeSet local;
	private ChangeSet remote;
	private EObject original; 
	private DSEMergeInputScope scope;
	private DesignSpaceExplorer dse;
	private EPackage metamodel;
	
	private Collection<TransformationRule<?>> rules;
	private Collection<IQuerySpecification<?>> objectives;
	
	private IQuerySpecification<IncQueryMatcher<IPatternMatch>> id2eobject;
	
	public ChangeSet getLocal() {
		return local;
	}
	public ChangeSet getRemote() {
		return remote;
	}
	public EObject getOriginal() {
		return original;
	}
	
	public void setMetamodel(EPackage metamodel) {
		this.metamodel = metamodel;
	}
	
	public void setRules(Collection<TransformationRule<?>> rules) {
		this.rules = rules;
	}
	
	public void setObjectives(Collection<IQuerySpecification<?>> objectives) {
		this.objectives = objectives;
	}
	
	public void setId2EObject(IQuerySpecification<?> querySpecification) {
		this.id2eobject = (IQuerySpecification<IncQueryMatcher<IPatternMatch>>) querySpecification;
	}
	
	private DSEMergeManager(EObject original, ChangeSet local, ChangeSet remote) {
		this.original = original;
		this.local = local;
		this.remote = remote;
		
		scope = ScopeFactory.eINSTANCE.createDSEMergeInputScope();
		scope.setRemote(remote);
		scope.setLocal(local);
		scope.setOrigin(original);	
		
		dse = new DesignSpaceExplorer();
	}
	
	public static DSEMergeManager create(EObject original, ChangeSet local, ChangeSet remote) {
		return new DSEMergeManager(original, local, remote);
	}
	
	public Collection<Solution> start() {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ERROR);
		Logger.getLogger(DSEMergeStrategy.class).setLevel(Level.DEBUG);
		Logger.getLogger(DesignSpaceManager.class).setLevel(Level.DEBUG);	
		
		dse.addMetaModelPackage(metamodel);
		dse.addMetaModelPackage(ScopePackage.eINSTANCE);
		dse.addMetaModelPackage(ModelPackage.eINSTANCE);
		
		dse.setInitialModel(scope);
		dse.setSerializerFactory(new DSEMergeSerializerFactory());
		
		ModelQueriesHardObjective modelQueriesHardObjective = new ModelQueriesHardObjective();
		for (IQuerySpecification<?> objective : objectives) {
			modelQueriesHardObjective.withConstraint(objective);
		}
		dse.addObjective(modelQueriesHardObjective.withType(ModelQueryType.NO_MATCH));
		
		for (TransformationRule<?> rule : rules) {
			dse.addTransformationRule(rule);
		}
		
		try {
			dse.addTransformationRule(new TransformationRule<ExecutableDeleteChangeMatch>(ExecutableDeleteChangeQuerySpecification.instance(), new DefaultMatchProcessor<ExecutableDeleteChangeMatch>()));
			dse.addTransformationRule(new TransformationRule<IPatternMatch>(id2eobject, new DefaultMatchProcessor<IPatternMatch>()));
		} catch (IncQueryException e) {
			e.printStackTrace();
		}
		
		DSEMergeStrategy strategy = new DSEMergeStrategy();
		strategy.setId2EObject(id2eobject);
		dse.setMaxNumberOfThreads(4);
		dse.startExploration(strategy);
		dse.prettyPrintSolutions();
		
		return dse.getSolutions();
	}
	
	public DSEMergeInputScope applyMerge(SolutionTrajectory trajectory) {
		try {
			trajectory.setModel(scope);		
			trajectory.doNextTransformation();
		} catch (IncQueryException e) {
			e.printStackTrace();
		}
		return scope;
	}
	
	private class DefaultMatchProcessor<T extends IPatternMatch> implements IMatchProcessor<T> {

		@Override
		public void process(T match) {
		}
		
	}
}