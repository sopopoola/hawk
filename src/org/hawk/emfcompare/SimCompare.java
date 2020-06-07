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
import java.util.HashMap;
//import javafx.util.*;
//import java.util.function.Function;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import org.eclipse.emf.compare.AttributeChange;
import org.eclipse.emf.compare.CompareFactory;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.ReferenceChange;
import org.eclipse.emf.compare.impl.ComparisonImpl;
import org.eclipse.emf.compare.internal.spec.ComparisonSpec;
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
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.hawk.graph.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SimCompare {
	private Resource metamodel;
	private Resource model1;
	private Resource model2;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SimCompare object = new SimCompare();
		File file1 = new File("C:/Users/student/Desktop/eclipse/test1.xmi");
		File file2 = new File("C:/Users/student/Desktop/eclipse/test2.xmi");
		//File file1 = new File("local4.xmi");
		//File file2 = new File("local3.xmi");
		File file3 = new File("modelP0.xmi");
		File file4 = new File("modelPb0.xmi");
		Comparison compare = object.compare(file1, file2);
		System.out.println("resource  "+compare.eResource());
		try {
			File f= new File("C:/Users/student/Desktop/eclipse/summary.txt");
			if(!f.exists())
				f.createNewFile();
			PrintWriter writer = new PrintWriter(f, "UTF-8");
			object.getSummary(compare,writer);
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		//object.getSummary(compare);
		/*
		for(Diff d: compare.getDifferences()) {
			if(d instanceof ReferenceChange)
				System.out.println("Diff  "+d.getKind() + "  " + ((ReferenceChange) d).getReference().getName()+"  "+d.eCrossReferences());
			else
				System.out.println("Diff  "+d.getKind() + "            " + d.eCrossReferences());
		}
		*/
		//System.out.println(compare);
		//compare.getDifferences().
		//compare.
		/*
		//EPackage p= object.getPackage();
		//Comparison compare= new ComparisonSpec();
		Comparison compare= CompareFactory.eINSTANCE.createComparison();
		EList<Diff> diff= compare.getDifferences();
		
		try {
			//System.out.println("start");
			ArrayList<Pair<String, String>> list = object.splitFile(file1,file2);
			File fileA,fileB;
			Comparison compare2;
			for(Pair l:list) {
				fileA= new File((String) l.getT());
				fileB= new File((String) l.getU());
				if(!fileA.exists()) {
					fileA.createNewFile();
				}
				if(!fileB.exists()) {
					fileB.createNewFile();
				}
				System.out.println("file a"+ l.getT());
				System.out.println("file b"+ l.getU());
				compare2= object.compare(fileA, fileB);
				System.out.println("compare "+compare2.getDifferences().size());
				//diff.addAll(compare2.getDifferences());
				//System.out.println(l);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		System.out.println("original compare "+diff.size());
		
		for (Diff d:diff) {
			if(d instanceof ReferenceChange)
				System.out.println("Diff  "+d.getKind() + "  " + ((ReferenceChange) d).getReference().getName());
			else
				System.out.println("Diff  "+d.getKind() + "  " + d);
		}
		*/
		//for (Match m:compare.getMatches()) {
			//System.out.println("Match  "+ m.getLeft() +"   "+ m.getRight() + "   "+m);
			
		//}

	}
	public Comparison compare(File file1, File file2) {
		// Load the two input models
		System.out.println(file1.getAbsolutePath());
		System.out.println(file2.getAbsolutePath());
		ResourceSet resourceSet1 = new ResourceSetImpl();
		ResourceSet resourceSet2 = new ResourceSetImpl();
		//resourceSet1.
		String xmi1 = file1.getAbsolutePath();
		String xmi2 = file2.getAbsolutePath();
		// change to metamodel for simulink
		Resource metamodel =loadMetamodel();
		Resource model1 = load(xmi1, resourceSet1);
		Resource model2= load(xmi2, resourceSet2);
		setResourceMetamodel(metamodel);
		setResourceModel1(model1);
		setResourceModel2(model2);
		/*
		for (EObject obj: model1.getContents()) {
			//System.out.println("te" + obj.eContents());
			for(EObject ob: obj.eContents()) {
				//System.out.println("te" + ob.eContents());
			}
		}
		for (EObject obj: model2.getContents()) {
			for(EObject ob: obj.eContents()) {
				//System.out.println("teggg" + ob.eContents());
			}
		}
		*/
		//EObject obj = model1.getContents().get(0);
		//System.out.println(obj.eContents().get(0).eContents());
		//System.out.println(resourceSet1);

		// Configure EMF Compare
		/*
		Function<EObject, String> idFunction = new Function<EObject, String>() {
			public String apply(EObject input) {
				//System.out.println("input  "+input.eClass());
				
				//System.out.println(input.eResource());
				if (input.eClass() instanceof EClass) {
					//System.out.println("input  "+input.eClass().getName());
					//return input.eClass().getName();
				}
				else {
					//System.out.println("goal");
				}
				// a null return here tells the match engine to fall back to the other matchers
				return null;
				//return "BlockDiagram";
			}
		};
		*/
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
		IEObjectMatcher customIDMatcher = new IdentifierEObjectMatcher(fallBackMatcher);
		 
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
		URI uri2 = URI.createFileURI("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/MyHawk/simulink.ecore");

		Resource r = resourceSet.getResource(uri2, true);
		EObject eObject = r.getContents().get(0);
		
		EPackage.Registry.INSTANCE.put("http://hu.bme.mit.massif/simulink/1.0", eObject);
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
	
	public ArrayList<Pair<String, String>> splitFile(File file, File file2) throws ParserConfigurationException, IOException, SAXException, TransformerException {
		ArrayList <Pair <String,String>> pair= new ArrayList <Pair <String,String>> ();
		File f= new File(file.getAbsoluteFile().getParent()+"/modelP/"+"empty.xmi");
		if(!f.exists()) {
			f.createNewFile();
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setIgnoringElementContentWhitespace(true);
	    factory.setNamespaceAware(true);
	    //factory.
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(file);
	    Document doc2 = builder.parse(file2);
	    
	    NodeList listA= doc.getElementsByTagName("BlockDiagram");
	    NodeList listB= doc2.getElementsByTagName("BlockDiagram");
	    Node node,node2;
	    //List notFound =new ArrayList<Integer>();
	   createEmptyFile(f);
	   List notFound= IntStream.range(0,listB.getLength()).boxed().collect(Collectors.toList());
	   String empty= f.getAbsolutePath();
	    for(int i=0;i<listA.getLength();i++) {
	    	node = (Node) listA.item(i);
	    	//System.out.println(node.getLocalName());
	    	boolean found=false;
	    	File fi= new File(f.getParent()+i+".xmi");
	    	PrintWriter writer = new PrintWriter(fi, "UTF-8");
	    	//writer.print("<?xml version=\"1.0\" encoding=\"ASCII\"?>\r\n");
			//writer.println("<xmi:XMI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\"/>");
	    	//writer.println("<?xml version=\"1.0\" encoding=\"ASCII\"?>\r\n" + 
	    			//"<xmi:XMI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.ni.com/LabVIEW.VI\">\r\n" + 
	    			//"");
	    	writer.println(nodeToString(node));
	    	//writer.println("</xmi:XMI>");
	    	writer.close();
	    	for (int j=0;j<listB.getLength();j++) {
	    		node2 = (Node) listB.item(j);
	    		File fi2= new File(f.getParent()+"b"+j+".xmi");
	    		PrintWriter writer2 = new PrintWriter(fi2, "UTF-8");
	    		//writer2.println("<xmi:XMI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\"/>");
    			////writer2.println("<?xml version=\"1.0\" encoding=\"ASCII\"?>\r\n" + 
    					//"<xmi:XMI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.ni.com/LabVIEW.VI\">\r\n" + 
    					//"");
	    		writer2.println(nodeToString(node2));
	    		//writer2.println("</xmi:XMI>");
    			writer2.close();
	    		if(((Element)node).getAttribute("file").equals(((Element)node2).getAttribute("file"))) {
	    			
		    		
	    			pair.add(new Pair<String, String>(fi.getAbsolutePath(),fi2.getAbsolutePath()));
	    			found=true;
	    			notFound.remove(Integer.valueOf(j));
	    			break;
	    			
	    		}
	    	}
	    	if(!found) {
	    		
	    		pair.add(new Pair<String, String>(fi.getAbsolutePath(),empty));
	    	}
	    	
		    //System.out.println("node  "+ node);
		    String fname= ((Element)node).getAttribute("");
	    }
	    for(Object obj:notFound) {
    		pair.add(new Pair<String, String>(empty,f.getParent()+"b"+obj+".xmi"));
    	}
	   
	    return pair;
	}
	
	private String nodeToString(Node node)
			throws TransformerException
			{
			    StringWriter buf = new StringWriter();
			    Transformer xform = TransformerFactory.newInstance().newTransformer();
			    xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			   //xform.setOutputProperty(OutputKeys.INDENT, "no");
			    //xform.
			    xform.transform(new DOMSource(node), new StreamResult(buf));
			    return(buf.toString());
			}

	
	public File createEmptyFile(File n) {
		//File n= new File("empty.xmi");
		 try {
				//System.out.println(nodeToString(node));
				PrintWriter writer = new PrintWriter(n, "UTF-8");
				//System.out.println(f.getAbsolutePath());
				
				writer.print("<?xml version=\"1.0\" encoding=\"ASCII\"?>\r\n");
				writer.print("<xmi:XMI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\"/>");
				
				writer.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
		return n;
	}
	public void getSummary(Comparison compare, PrintWriter writer) {
		Map<String, Integer> pair = new HashMap();
		int add,move,change,delete,others;
		ResourceSet resourceSet = new ResourceSetImpl();
		//File f = new File("text.xmi");
		/*
		if(!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		//createEmptyFile(f);
		//Resource r =load(f.getAbsolutePath(), resourceSet);
		//System.out.println("new resource" + r.getContents().size());
		add=move=change=delete=others=0;
		String name;
		//compare.
		//for (Match m:compare.getMatches()) {
			//System.out.println("m  ");
		//}
		for(Diff d: compare.getDifferences()) {
			//r.getContents().add(d);
			//System.out.println("diff  "+ d.);
			//System.out.println("  "+d.getClass().getName());
			if(d instanceof ReferenceChange) {
				//System.out.println(d.eContainer());
				//System.out.println(12);
				ReferenceChange s= ((ReferenceChange) d);
				////System.out.println("ghh");
				EObject val = s.getValue();
				EReference ref= s.getReference();
				//System.out.println("mmmm   "+ ref.getEType().eCrossReferences());
				for(EStructuralFeature attr: ref.eClass().getEAllStructuralFeatures()) {
					//System.out.println(attr);
					//System.out.println("twist  "+ref.eClass().eGet(attr));
					//if(attr.getName().equals("name"))
						//System.out.println("test  "+ref.eGet(attr));
					
				}
				
				//System.out.println("reference  " + s.getReference().getEReferenceType());
				//System.out.println("value  "+ s.getValue().eAllContents());
				//System.out.println("s  "+s);
				name=d.getKind()+ " "+((ReferenceChange) d).getReference().getName();
				//System.out.println(name);
				if(pair.containsKey(name))
					pair.replace(name, (pair.get(name)+1));
				else
					pair.put(name, 1);
				
			}
			else if(d instanceof AttributeChange) {
				AttributeChange s= (AttributeChange)d;
				//System.out.println(s);
				//System.out.println(s.getAttribute().getName());
				
			}
			else {
				//System.out.println(d.eContainer());
				//System.out.println(d.eCrossReferences().size());
				name=d.getKind()+" others";
				//System.out.println(d.getKind()+"  "+ d);
				if(pair.containsKey(name))
					pair.replace(name, (pair.get(name)+1));
				else
					pair.put(name, 1);
			}
			if(d.getKind().getName().equals("ADD"))
				add++;
			else if (d.getKind().getName().equals("DELETE"))
				delete++;
			else if (d.getKind().getName().equals("CHANGE"))
				change++;
			else if (d.getKind().getName().equals("MOVE"))
				move++;
			else 
				others++;		
		}
		//System.out.println("new resource2" + r.getContents().size());
		writer.println("ADD   "+ add);
		writer.println("CHANGE   "+ change);
		writer.println("DELETE   "+ delete);
		writer.println("MOVE   "+ move);
		writer.println("OTHERS   "+ others);
		writer.println(pair);
	}
}
