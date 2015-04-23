package hu.bme.mit.model.compare.test;

import hu.bme.mit.trainbenchmark.railway.RailwayPackage;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.match.DefaultComparisonFactory;
import org.eclipse.emf.compare.match.DefaultEqualityHelperFactory;
import org.eclipse.emf.compare.match.DefaultMatchEngine;
import org.eclipse.emf.compare.match.IComparisonFactory;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryRegistryImpl;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.incquery.runtime.api.IQuerySpecification;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import org.eclipse.viatra.dse.api.Solution;
import org.eclipse.viatra.dse.api.SolutionTrajectory;
import org.eclipse.viatra.dse.api.TransformationRule;
import org.eclipse.viatra.dse.merge.DSEMergeManager;
import org.eclipse.viatra.dse.merge.emf_compare.EMFCompareTranslator;
import org.eclipse.viatra.dse.merge.model.ChangeSet;
import org.eclipse.viatra.dse.merge.model.ModelPackage;
import org.eclipse.viatra.dse.merge.model.Priority;
import org.eclipse.viatra.dse.merge.scope.DSEMergeInputScope;
import org.eclipse.viatra.dse.merge.scope.ScopePackage;
import org.eclipse.viatra.dse.merge.train.AddAttributeMatch;
import org.eclipse.viatra.dse.merge.train.AddReferenceMatch;
import org.eclipse.viatra.dse.merge.train.CreateMatch;
import org.eclipse.viatra.dse.merge.train.DeleteMatch;
import org.eclipse.viatra.dse.merge.train.RemoveAttributeMatch;
import org.eclipse.viatra.dse.merge.train.RemoveReferenceMatch;
import org.eclipse.viatra.dse.merge.train.SetAttributeMatch;
import org.eclipse.viatra.dse.merge.train.SetReferenceMatch;
import org.eclipse.viatra.dse.merge.train.operations.AddAttributeOperation;
import org.eclipse.viatra.dse.merge.train.operations.AddReferenceOperation;
import org.eclipse.viatra.dse.merge.train.operations.CreateOperation;
import org.eclipse.viatra.dse.merge.train.operations.DeleteOperation;
import org.eclipse.viatra.dse.merge.train.operations.RemoveAttributeOperation;
import org.eclipse.viatra.dse.merge.train.operations.RemoveReferenceOperation;
import org.eclipse.viatra.dse.merge.train.operations.SetAttributeOperation;
import org.eclipse.viatra.dse.merge.train.operations.SetReferenceOperation;
import org.eclipse.viatra.dse.merge.train.util.AddAttributeQuerySpecification;
import org.eclipse.viatra.dse.merge.train.util.AddReferenceQuerySpecification;
import org.eclipse.viatra.dse.merge.train.util.CreateQuerySpecification;
import org.eclipse.viatra.dse.merge.train.util.DeleteQuerySpecification;
import org.eclipse.viatra.dse.merge.train.util.GoalPatternQuerySpecification;
import org.eclipse.viatra.dse.merge.train.util.Id2objectQuerySpecification;
import org.eclipse.viatra.dse.merge.train.util.RemoveAttributeQuerySpecification;
import org.eclipse.viatra.dse.merge.train.util.RemoveReferenceQuerySpecification;
import org.eclipse.viatra.dse.merge.train.util.SetAttributeQuerySpecification;
import org.eclipse.viatra.dse.merge.train.util.SetReferenceQuerySpecification;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class EMFCompareTest {

	private static String PREFIX = "C:\\Git\\trainbenchmark\\models\\";
	
	private static String LOCAL_MODEL = PREFIX + "railway-test-1-a.emf.railway";
	private static String REMOTE_MODEL = PREFIX + "railway-test-1-b.emf.railway";
	private static String ORIGINAL_MODEL = PREFIX + "railway-test-1-orig.emf";

	private ChangeSet changeSetOL;
	private ChangeSet changeSetOR;
	private EObject originalModel;

	private ArrayList<IQuerySpecification<?>> goals;

	private ArrayList<TransformationRule<?>> rules;

	private DSEMergeManager manager;
	
	@Before
	public void setUp() throws IncQueryException {
		RailwayPackage p = RailwayPackage.eINSTANCE;
		ModelPackage pp = ModelPackage.eINSTANCE;
		ScopePackage ppp = ScopePackage.eINSTANCE;
		
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("railway", new XMIResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("emf", new XMIResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("dummy", new XMIResourceFactoryImpl());
		
		ResourceSet original = new ResourceSetImpl();
		ResourceSet local = new ResourceSetImpl();
		ResourceSet remote = new ResourceSetImpl();
		
		System.out.println("start model loading");
		// Loading models to resource set
		original.getResource(URI.createFileURI(ORIGINAL_MODEL), true);
		local.getResource(URI.createFileURI(LOCAL_MODEL), true);
		remote.getResource(URI.createFileURI(REMOTE_MODEL), true);
		
		System.out.println("end model loading");
		
		// Configure EMF Compare
		IEObjectMatcher matcher = DefaultMatchEngine.createDefaultEObjectMatcher(UseIdentifiers.WHEN_AVAILABLE);
		IComparisonFactory comparisonFactory = new DefaultComparisonFactory(new DefaultEqualityHelperFactory());
		IMatchEngine.Factory matchEngineFactory = new MatchEngineFactoryImpl(matcher, comparisonFactory);
		matchEngineFactory.setRanking(20);
		IMatchEngine.Factory.Registry matchEngineRegistry = new MatchEngineFactoryRegistryImpl();
		matchEngineRegistry.add(matchEngineFactory);
		EMFCompare comparator = EMFCompare.builder().setMatchEngineFactoryRegistry(matchEngineRegistry).build();

		System.out.println("start comparison");
		// Compare the two models
		IComparisonScope scopeOL = EMFCompare.createDefaultScope(local, original);
		Comparison comparisonOL = comparator.compare(scopeOL);
		IComparisonScope scopeOR = EMFCompare.createDefaultScope(remote, original);
		Comparison comparisonOR = comparator.compare(scopeOR);
		System.out.println("end comparison");

		System.out.println("start input transformation");
		changeSetOL = EMFCompareTranslator.translate(comparisonOL);
		changeSetOR = EMFCompareTranslator.translate(comparisonOR);
		originalModel = original.getResources().get(0).getContents().get(0);
		System.out.println("end input transformation");
		
		System.out.println("start configuration");
		rules = Lists.<TransformationRule<?>>newArrayList(
				new TransformationRule<CreateMatch>(CreateQuerySpecification.instance(), new CreateOperation()),
				new TransformationRule<DeleteMatch>(DeleteQuerySpecification.instance(), new DeleteOperation()),
				new TransformationRule<SetReferenceMatch>(SetReferenceQuerySpecification.instance(), new SetReferenceOperation()),
				new TransformationRule<AddReferenceMatch>(AddReferenceQuerySpecification.instance(), new AddReferenceOperation()),
				new TransformationRule<RemoveReferenceMatch>(RemoveReferenceQuerySpecification.instance(), new RemoveReferenceOperation()),
				new TransformationRule<SetAttributeMatch>(SetAttributeQuerySpecification.instance(), new SetAttributeOperation()),
				new TransformationRule<AddAttributeMatch>(AddAttributeQuerySpecification.instance(), new AddAttributeOperation()),
				new TransformationRule<RemoveAttributeMatch>(RemoveAttributeQuerySpecification.instance(), new RemoveAttributeOperation()));
		
		goals = Lists.<IQuerySpecification<?>>newArrayList(
				GoalPatternQuerySpecification.instance());		

		//set priority for the first 4 changes on local side
		for(int i = 0; i < 4; i++) {
			changeSetOL.getChanges().get(i).setPriority(Priority.MUST);
		}		
		
		//set priority for the first 2 changes on local side
		for(int i = 0; i < 2; i++) {
			changeSetOR.getChanges().get(i).setPriority(Priority.MUST);
		}
		
		manager = DSEMergeManager.create(originalModel, changeSetOL, changeSetOR);
		manager.setMetamodel(RailwayPackage.eINSTANCE);
		manager.setId2EObject(Id2objectQuerySpecification.instance());
		manager.setRules(rules);
		manager.setObjectives(goals);
		System.out.println("end configuration");		
	}
	
	@Test
	public void test() throws IncQueryException {
		System.out.println("start merge calculation");		
		Collection<Solution> solutions = manager.start();
		System.out.println("end merge calculation");
		
		System.out.println("Found solutions:" + solutions.size());
		
		for (Solution solution : solutions) {
			SolutionTrajectory trajectory = solution.getShortestTrajectory();
			DSEMergeInputScope mergedModel = manager.applyMerge(trajectory);
			System.out.println("a solution is applied");
			break;
		}
		
		System.out.println("done");
	}

}
