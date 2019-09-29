package org.hawk.emfcompare;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
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
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class HawkCompare {
	private Resource metamodel;
	private Resource model1;
	private Resource model2;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HawkCompare object = new HawkCompare();
		File file1 = new File("local3.xmi");
		File file2 = new File("local4.xmi");
		Comparison compare = object.compare(file1, file2);
		EPackage p= object.getPackage();
		//EStructuralFeature feature = p.;
		//feature.
		for (EObject obj:p.eContents()) {
			//System.out.println(compare.getDifferences(object.getEObject()));
			//System.out.println(object.getEObject().eClass().getName());
			//System.out.println(((EClass)obj).getName());
			//if (object.isInstanceOf(obj,object.getEObject()))
				//System.out.println("its out");
		}
		//System.out.println(object.getAllObjects(object.getResourceModel1().getContents().get(0)));
		for(Diff diff: compare.getDifferences()) {
			System.out.println(diff.getKind()+"  "+ diff.getMatch().getLeft().eClass().getName()+ "  "+diff.getMatch().getRight().eClass().getName());
		}
		for (EObject obj: object.getAllObjects(object.getResourceModel1().getContents().get(0)) ) {
			for (Diff diff: compare.getDifferences(obj)) {
				System.out.println(diff.getKind() + "    "+ obj.eClass().getName());
			}
			System.out.println(compare.getDifferences(obj));
			System.out.println();
			System.out.println();
		}
		
		for (Diff d:compare.getDifferences()) {
			//System.out.println(d.getKind() + "  " + d.eClass());
		}

	}
	public Comparison compare(File file1, File file2) {
		// Load the two input models
		ResourceSet resourceSet1 = new ResourceSetImpl();
		ResourceSet resourceSet2 = new ResourceSetImpl();
		//resourceSet1.
		String xmi1 = file1.getAbsolutePath();
		String xmi2 = file2.getAbsolutePath();
		Resource metamodel =loadMetamodel();
		Resource model1 = load(xmi1, resourceSet1);
		Resource model2= load(xmi2, resourceSet2);
		setResourceMetamodel(metamodel);
		setResourceModel1(model1);
		setResourceModel2(model2);
		//EObject obj = model1.getContents().get(0);
		//System.out.println(obj.eContents().get(0).eContents());
		//System.out.println(resourceSet1);

		// Configure EMF Compare
		IEObjectMatcher matcher = DefaultMatchEngine.createDefaultEObjectMatcher(UseIdentifiers.NEVER);
		IComparisonFactory comparisonFactory = new DefaultComparisonFactory(new DefaultEqualityHelperFactory());
		IMatchEngine.Factory matchEngineFactory = new MatchEngineFactoryImpl(matcher, comparisonFactory);
	        matchEngineFactory.setRanking(20);
	        IMatchEngine.Factory.Registry matchEngineRegistry = new MatchEngineFactoryRegistryImpl();
	        matchEngineRegistry.add(matchEngineFactory);
		EMFCompare comparator = EMFCompare.builder().setMatchEngineFactoryRegistry(matchEngineRegistry).build();

		// Compare the two models
		IComparisonScope scope = EMFCompare.createDefaultScope(resourceSet1, resourceSet2);
		
		
		return comparator.compare(scope);
	}
	// check if an object is an instance of a clas
	public Boolean isInstanceOf(EObject clas,EObject object) {
		//verify that class is an EClass of a metamodel
		if(clas instanceof EClass)
			return ((EClass)clas).getName().equals(object.eClass().getName());
		return false;
		
	}
	
	
	public void setResourceMetamodel(Resource model) {
		metamodel= model;
	}
	
	public void setResourceModel1(Resource model) {
		model1=model;
	}

	public void setResourceModel2(Resource model) {
		model2= model;
	}
	public Resource getResourceMetamodel() {
		return metamodel;
	}
	
	public Resource getResourceModel1() {
		return model1;
	}

	public Resource getResourceModel2() {
		return model2;
	}
	private Resource load(String absolutePath, ResourceSet resourceSet) {
		
	  URI uri = URI.createFileURI(absolutePath);
	  

	  resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());

	  // Resource will be loaded within the resource set
	  Resource ra=resourceSet.getResource(uri, true);
	  return ra;
	  //System.out.println(ra.getContents());
	}
	private ResourceSet load(String path) {
		ResourceSet resourceSet = new ResourceSetImpl();
		load(path, resourceSet);
		Resource r = resourceSet.getResource(URI.createFileURI(path), true);
		EObject obj = r.getContents().get(0);
		System.out.println(obj.eContents().get(0).eContents());
		
				//System.out.println(resourceSet1.getResources());
		return resourceSet;
	}
	private Resource loadMetamodel() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		URI uri2 = URI.createFileURI("labview.ecore");

		Resource r = resourceSet.getResource(uri2, true);
		//System.out.println(r.getContents());
		EObject eObject = r.getContents().get(0);
		
		EPackage.Registry.INSTANCE.put("http://www.ni.com/LabVIEW.VI", eObject);
		return r;
		
	}
	public EPackage getPackage() {
		EObject object = getResourceMetamodel().getContents().get(0);
		if (object instanceof EPackage)
			return (EPackage)object;
		return null;
	}
	public EList<EObject> getAllObjects(EObject model) {
		EList<EObject> objects= new BasicEList<EObject>();
		if(model.eContents().size()==0) {
			return model.eContents();
		}
		for(EObject obj: model.eContents()) {
			objects.add(obj);
			objects.addAll(getAllObjects(obj));
		}
		
		return objects;
		
	}
	public EObject getEObject() {
		return getResourceModel1().getContents().get(0);
	}

}
