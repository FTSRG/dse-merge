package hu.bme.mit.model.diffmerge.test;

import hu.bme.mit.trainbenchmark.railway.RailwayPackage;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.diffmerge.api.scopes.IEditableModelScope;
import org.eclipse.emf.diffmerge.diffdata.impl.EComparisonImpl;
import org.eclipse.emf.diffmerge.impl.scopes.FragmentedModelScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.incquery.runtime.api.IQuerySpecification;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import org.eclipse.viatra.dse.api.DSETransformationRule;
import org.eclipse.viatra.dse.merge.DSEMergeManager;
import org.eclipse.viatra.dse.merge.DSEMergeManager.Solution;
import org.eclipse.viatra.dse.merge.diff_merge.EMFDiffMergeTranslator;
import org.eclipse.viatra.dse.merge.model.ChangeSet;
import org.eclipse.viatra.dse.merge.model.ModelPackage;
import org.eclipse.viatra.dse.merge.model.Priority;
import org.eclipse.viatra.dse.merge.scope.ScopePackage;
import org.eclipse.viatra.dse.merge.train.AddAttributeMatch;
import org.eclipse.viatra.dse.merge.train.AddAttributeMatcher;
import org.eclipse.viatra.dse.merge.train.AddReferenceMatch;
import org.eclipse.viatra.dse.merge.train.AddReferenceMatcher;
import org.eclipse.viatra.dse.merge.train.CreateMatch;
import org.eclipse.viatra.dse.merge.train.CreateMatcher;
import org.eclipse.viatra.dse.merge.train.DeleteMatch;
import org.eclipse.viatra.dse.merge.train.DeleteMatcher;
import org.eclipse.viatra.dse.merge.train.RemoveAttributeMatch;
import org.eclipse.viatra.dse.merge.train.RemoveAttributeMatcher;
import org.eclipse.viatra.dse.merge.train.RemoveReferenceMatch;
import org.eclipse.viatra.dse.merge.train.RemoveReferenceMatcher;
import org.eclipse.viatra.dse.merge.train.SetAttributeMatch;
import org.eclipse.viatra.dse.merge.train.SetAttributeMatcher;
import org.eclipse.viatra.dse.merge.train.SetReferenceMatch;
import org.eclipse.viatra.dse.merge.train.SetReferenceMatcher;
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
import org.eclipse.viatra.dse.merge.train.util.RemoveAttributeQuerySpecification;
import org.eclipse.viatra.dse.merge.train.util.RemoveReferenceQuerySpecification;
import org.eclipse.viatra.dse.merge.train.util.SetAttributeQuerySpecification;
import org.eclipse.viatra.dse.merge.train.util.SetReferenceQuerySpecification;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class EMFDiffMergeTest {

private static String PREFIX = "C:\\Git\\trainbenchmark\\models\\";
	
	private static String LOCAL_MODEL = PREFIX + "railway-test-uuid-1-a.emf";
	private static String REMOTE_MODEL = PREFIX + "railway-test-uuid-1-b.emf";
	private static String ORIGINAL_MODEL = PREFIX + "railway-test-uuid-1-orig.emf";

	private ChangeSet changeSetOL;
	private ChangeSet changeSetOR;
	private EObject originalModel;

	private ArrayList<IQuerySpecification<?>> goals;

	private ArrayList<DSETransformationRule<?,?>> rules;
	
	private DSEMergeManager manager;
	
	@Before
	public void setUp() throws IncQueryException {
		RailwayPackage p = RailwayPackage.eINSTANCE;
		ModelPackage pp = ModelPackage.eINSTANCE;
		ScopePackage ppp = ScopePackage.eINSTANCE;
		
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("emf", new XMIResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("dummy", new XMIResourceFactoryImpl());
		
		ResourceSet rSet = new ResourceSetImpl();
		
		// Loading models to resource set
		Resource original = rSet.getResource(URI.createFileURI(ORIGINAL_MODEL), true);
		Resource local = rSet.getResource(URI.createFileURI(LOCAL_MODEL), true);
		Resource remote = rSet.getResource(URI.createFileURI(REMOTE_MODEL), true);

		IEditableModelScope originalScope = new FragmentedModelScope(original,true);
		IEditableModelScope localScope = new FragmentedModelScope(local, true);
		IEditableModelScope remoteScope = new FragmentedModelScope(remote, true);
		
		EComparisonImpl comparisonOL = new EComparisonImpl(localScope, originalScope);
		comparisonOL.compute(null, null, null, null);
		EComparisonImpl comparisonOR = new EComparisonImpl(remoteScope, originalScope);
		comparisonOR.compute(null, null, null, null);
		
		changeSetOL = EMFDiffMergeTranslator.translate(comparisonOL);
		changeSetOR = EMFDiffMergeTranslator.translate(comparisonOR);
		
		rules = Lists.<DSETransformationRule<?,?>>newArrayList(
				new DSETransformationRule<CreateMatch,CreateMatcher>(CreateQuerySpecification.instance(), new CreateOperation()),
				new DSETransformationRule<DeleteMatch,DeleteMatcher>(DeleteQuerySpecification.instance(), new DeleteOperation()),
				new DSETransformationRule<SetReferenceMatch,SetReferenceMatcher>(SetReferenceQuerySpecification.instance(), new SetReferenceOperation()),
				new DSETransformationRule<AddReferenceMatch,AddReferenceMatcher>(AddReferenceQuerySpecification.instance(), new AddReferenceOperation()),
				new DSETransformationRule<RemoveReferenceMatch,RemoveReferenceMatcher>(RemoveReferenceQuerySpecification.instance(), new RemoveReferenceOperation()),
				new DSETransformationRule<SetAttributeMatch,SetAttributeMatcher>(SetAttributeQuerySpecification.instance(), new SetAttributeOperation()),
				new DSETransformationRule<AddAttributeMatch,AddAttributeMatcher>(AddAttributeQuerySpecification.instance(), new AddAttributeOperation()),
				new DSETransformationRule<RemoveAttributeMatch,RemoveAttributeMatcher>(RemoveAttributeQuerySpecification.instance(), new RemoveAttributeOperation()));
		
		goals = Lists.<IQuerySpecification<?>>newArrayList(GoalPatternQuerySpecification.instance());		

		for(int i = 0; i < 4; i++) {
			changeSetOL.getChanges().get(i).setPriority(Priority.MUST);
		}		
		
		for(int i = 0; i < 2; i++) {
			changeSetOR.getChanges().get(i).setPriority(Priority.MUST);
		}
		
		manager = DSEMergeManager.create(originalModel, changeSetOL, changeSetOR);
		manager.setMetamodel(RailwayPackage.eINSTANCE);
		manager.setRules(rules);
		manager.setObjectives(goals);
	}
	
	@Test
	public void test() throws IncQueryException {
		Collection<Solution> solutions = manager.start();
		System.out.println("done");
	}
}
