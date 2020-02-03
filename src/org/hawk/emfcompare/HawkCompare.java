package org.hawk.emfcompare;

import com.google.common.base.Function;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
//import java.util.function.Function;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.match.DefaultComparisonFactory;
import org.eclipse.emf.compare.match.DefaultEqualityHelperFactory;
import org.eclipse.emf.compare.match.DefaultMatchEngine;
import org.eclipse.emf.compare.match.IComparisonFactory;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.match.eobject.IdentifierEObjectMatcher;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryRegistryImpl;
//import org.eclipse.emf.compare.match.eobject.IdentifierEObjectMatcher;;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class HawkCompare {
	private Resource metamodel;
	private Resource model1;
	private Resource model2;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HawkCompare object = new HawkCompare();
		File file1 = new File("myhawksap52.xmi");
		File file2 = new File("myhawksap51.xmi");
		Comparison compare = object.compare(file1, file2);
		//compare.
		EPackage p= object.getPackage();
		//p.
		//EStructuralFeature feature = p.;
		//feature.
		for (EObject obj:p.eContents()) {
			//System.out.println(compare.getDifferences(object.getEObject()));
			//System.out.println("me  "+object.getEObject().eClass().getName());
			//System.out.println("them  "+ ((EClass)obj).getName());
			//EStructuralFeature feature = ((EClass)obj).getEStructuralFeature("Id");
			//feature.
			//System.out.println("type  "+ ((EClass)obj).getEStructuralFeature("Id").getFeatureID());
			//System.out.println("id  "+ ((EClass)obj).getFeatureID(((EClass)obj).getEStructuralFeature("Id")));
			//if (object.isInstanceOf(obj,object.getEObject()))
				//System.out.println("its out");
		}
		//System.out.println(object.getAllObjects(object.getResourceModel1().getContents().get(0)));
		//for(Diff diff: compare.getDifferences()) {
			//System.out.println(diff.getKind()+"  "+ diff.getMatch().getLeft().eClass().getName()+ "  "+diff.getMatch().getRight().eClass().getName());
		//}
		//for (EObject obj: object.getAllObjects(object.getResourceModel1().getContents().get(0)) ) {
			//for (Diff diff: compare.getDifferences(obj)) {
				//System.out.println(diff.getKind() + "    "+ obj.eClass().getName());
			//}
			//System.out.println(compare.getDifferences(obj));
			//System.out.println();
			//System.out.println();
		//}
		
		for (Diff d:compare.getDifferences()) {
			System.out.println("Diff  "+d.getKind() + "  " + d);
		}
		for (Match m:compare.getMatches()) {
			System.out.println("Match  "+ m.getLeft() +"   "+ m.getRight() + "   "+m);
			
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
		////System.out.println("class  "+ model1.getClass());
		//System.out.println(model1.getAllContents());
		for (EObject obj: model1.getContents()) {
			//System.out.println("test   "+obj);
			for(EObject ob: obj.eContents()) {
				//System.out.println("te" + ob.eContents());
			}
		}
		//EObject obj = model1.getContents().get(0);
		//System.out.println(obj.eContents().get(0).eContents());
		//System.out.println(resourceSet1);

		// Configure EMF Compare
		Function<EObject, String> idFunction = new Function<EObject, String>() {
			public String apply(EObject input) {
				//System.out.println("input  "+input.eClass());
				
				//System.out.println(input.eResource());
				if (input.eClass() instanceof EClass) {
					//System.out.println(input.eClass().getEStructuralFeature("Id"));
					//System.out.println(input.eClass().getClassifierID());
					return input.eClass().getName();
				//System.out.println(((EStructuralFeature)input).getFeatureID());
				//return ((EClass)input).getEStructuralFeature("Id").getName();
				}
				// a null return here tells the match engine to fall back to the other matchers
				return null;
				//return "BlockDiagram";
			}
		};
		
		/**
		 Default matcher
		IEObjectMatcher matcher = DefaultMatchEngine.createDefaultEObjectMatcher(UseIdentifiers.NEVER);
		//matcher.
		IComparisonFactory comparisonFactory = new DefaultComparisonFactory(new DefaultEqualityHelperFactory());
		IMatchEngine.Factory matchEngineFactory = new MatchEngineFactoryImpl(matcher, comparisonFactory);
	        matchEngineFactory.setRanking(20);
	        IMatchEngine.Factory.Registry matchEngineRegistry = new MatchEngineFactoryRegistryImpl();
	        matchEngineRegistry.add(matchEngineFactory);
		EMFCompare comparator = EMFCompare.builder().setMatchEngineFactoryRegistry(matchEngineRegistry).build();

		// Compare the two models
		IComparisonScope scope = EMFCompare.createDefaultScope(resourceSet1, resourceSet2);
		
		
		return comparator.compare(scope);
		***/
		IEObjectMatcher fallBackMatcher = DefaultMatchEngine.createDefaultEObjectMatcher(UseIdentifiers.WHEN_AVAILABLE);
		IEObjectMatcher customIDMatcher = new IdentifierEObjectMatcher(fallBackMatcher, idFunction);
		 
		IComparisonFactory comparisonFactory = new DefaultComparisonFactory(new DefaultEqualityHelperFactory());
		 
		IMatchEngine.Factory.Registry registry = MatchEngineFactoryRegistryImpl.createStandaloneInstance();
		// for OSGi (IDE, RCP) usage
		// IMatchEngine.Factory.Registry registry = EMFCompareRCPPlugin.getMatchEngineFactoryRegistry();
		final MatchEngineFactoryImpl matchEngineFactory = new MatchEngineFactoryImpl(customIDMatcher, comparisonFactory);
		matchEngineFactory.setRanking(20); // default engine ranking is 10, must be higher to override.
		registry.add(matchEngineFactory);
		IComparisonScope scope = EMFCompare.createDefaultScope(resourceSet1, resourceSet2);
		Comparison result = EMFCompare.builder().setMatchEngineFactoryRegistry(registry).build().compare(scope);
		return result;
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
		URI uri2 = URI.createFileURI("C:/Users/student/git/hawk/labview.ecore");

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
	
	public File localParse(File file, File file2) throws ParserConfigurationException, IOException, SAXException {
		File t = new File("");
		File f= new File(t.getAbsoluteFile()+"/newfiles/"+getFileName(file.getName()));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setIgnoringElementContentWhitespace(true);
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(file);
	    Document doc2 = builder.parse(file2);
	    NodeList listA= doc.getElementsByTagName("BlockDiagram");
	    NodeList listB= doc2.getElementsByTagName("BlockDiagram");
	    Node node,node2;
	    for(int i=0;i<listA.getLength();i++) {
	    	node = (Node) listA.item(i);
	    	for (int j=0;j<listB.getLength();j++) {
	    		node2 = (Node) listB.item(j);
	    		if(((Element)node).getAttribute("file").equals(((Element)node2).getAttribute("file"))) {
	    			
	    		}
	    	}
		    //System.out.println("node  "+ node);
		    String fname= ((Element)node).getAttribute("");
	    }
	    node = (Node) listA.item(0);
	    //System.out.println("node  "+ node);
	    String fname= ((Element)node).getAttribute("");
	   ((Element)node).setAttribute("file", file.getName());
	    try {
			//System.out.println(nodeToString(node));
			PrintWriter writer = new PrintWriter(f, "UTF-8");
			//System.out.println(f.getAbsolutePath());
			
			writer.println(nodeToString(node));
			
			//f.
			//System.out.println("test  "+ getFileName(file.getName()));
			//writer.println("The second line");
			writer.close();
		} catch (TransformerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	   // node.
	    //Element element= (Element)node;
	   // NodeList list =element.getChildNodes();
	    //System.out.println();
	   
	    //System.out.println(doc.getElementsByTagName("BlockDiagram"));
	   //// for (int i = 0; i < list.getLength(); i++) {
            //Node nNode = (Node) list.item(i);
	    //}
	    return f;
	    // Do something with the document here.
	}
	
	private String nodeToString(Node node)
			throws TransformerException
			{
			    StringWriter buf = new StringWriter();
			    Transformer xform = TransformerFactory.newInstance().newTransformer();
			    xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			    xform.transform(new DOMSource(node), new StreamResult(buf));
			    return(buf.toString());
			}

	private String getFileName(String name) {
		String[] nameList= name.split("\\.");
		String result= nameList[0] + ".customxml";
		return result;
		
	}
	public File createEmptyFile() {
		File n= new File("empty.xmi");
		 try {
				//System.out.println(nodeToString(node));
				PrintWriter writer = new PrintWriter(n, "UTF-8");
				//System.out.println(f.getAbsolutePath());
				
				writer.println("<?xml version=\"1.0\" encoding=\"ASCII\"?>\r\n");
				
				//f.
				//System.out.println("test  "+ getFileName(file.getName()));
				//writer.println("The second line");
				writer.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
		return n;
	}
}
