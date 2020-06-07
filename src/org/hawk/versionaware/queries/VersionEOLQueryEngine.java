package org.hawk.versionaware.queries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.ReferenceChange;
import org.eclipse.emf.compare.match.DefaultComparisonFactory;
import org.eclipse.emf.compare.match.DefaultEqualityHelperFactory;
import org.eclipse.emf.compare.match.DefaultMatchEngine;
import org.eclipse.emf.compare.match.IComparisonFactory;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.match.eobject.IdentifierEObjectMatcher;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryRegistryImpl;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.execute.operations.contributors.OperationContributorRegistry;
import org.eclipse.hawk.core.IModelIndexer;
import org.eclipse.hawk.core.IStateListener.HawkState;
import org.eclipse.hawk.core.IVcsManager;
import org.eclipse.hawk.core.graph.IGraphIterable;
import org.eclipse.hawk.core.graph.IGraphNode;
import org.eclipse.hawk.core.graph.IGraphNodeIndex;
import org.eclipse.hawk.core.graph.IGraphNodeReference;
import org.eclipse.hawk.core.graph.IGraphTransaction;
import org.eclipse.hawk.core.graph.timeaware.ITimeAwareGraphDatabase;
import org.eclipse.hawk.core.graph.timeaware.ITimeAwareGraphNode;
import org.eclipse.hawk.core.query.InvalidQueryException;
import org.eclipse.hawk.core.query.QueryExecutionException;
import org.eclipse.hawk.core.runtime.BaseModelIndexer;
import org.hawk.emfcompare.HawkCompare;
import org.hawk.emfcompare.SimCompare;
import org.eclipse.hawk.emfresource.impl.LocalHawkResourceImpl;
import org.eclipse.hawk.epsilon.emc.EOLQueryEngine;
import org.eclipse.hawk.epsilon.emc.wrappers.GraphNodeWrapper;
import org.eclipse.hawk.graph.GraphWrapper;
import org.eclipse.hawk.graph.MetamodelNode;
import org.eclipse.hawk.graph.ModelElementNode;
import org.eclipse.hawk.graph.TypeNode;
import org.eclipse.hawk.service.api.Hawk.Client;
import org.eclipse.hawk.service.emc.RemoteHawkModel;
import org.eclipse.hawk.service.emf.HawkModelDescriptor;
import org.eclipse.hawk.service.emf.impl.HawkResourceFactoryImpl;
import org.eclipse.hawk.timeaware.graph.TimeAwareGraphNodeWrapper;
import org.eclipse.hawk.timeaware.graph.VCSManagerIndex;
import org.eclipse.hawk.timeaware.graph.VCSManagerIndex.RepositoryNode;
//import org.hawk.service.api.Users$Client;
import org.eclipse.hawk.timeaware.queries.TimeAwareEOLOperationFactory;
import org.eclipse.hawk.timeaware.queries.TimeAwareEOLQueryEngine;
import org.eclipse.hawk.timeaware.queries.operations.reflective.TimeAwareNodeHistoryOperationContributor;
import org.eclipse.hawk.timeaware.queries.operations.reflective.TypeHistoryOperationContributor;
import org.eclipse.hawk.ui.emfresource.LocalHawkResourceFactoryImpl;
import org.eclipse.hawk.greycat.AbstractGreycatDatabase;
import org.eclipse.hawk.greycat.GreycatNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;;

public class VersionEOLQueryEngine extends TimeAwareEOLQueryEngine{
	private static final Logger LOGGER = LoggerFactory.getLogger(VersionEOLQueryEngine.class);
	public VersionEOLQueryEngine() {
		super();
	}

	public Collection<?> getAllVersion() {
		final Set<Object> allContents = new HashSet<Object>();
		final Collection<Long> instants = getAllInstants();
		for (long instant : instants) {
			allContents.add(Arrays.asList(instant, allInstancesAt(instant)));
				
		} 
		return allContents;
	}
	public Collection<?> getVersion(int i){
		Collection<Long> collect = getAllInstants();
		if (collect.isEmpty()) {
			return collect;
		}
		int size= collect.size()-1;
		if (i<size) {
			size= i;
		}
		Object[] times = collect.toArray();
		return allInstancesAt((Long)times[size]);
	}
	public String getMessage(long time) throws Exception {
		Runtime.getRuntime().gc();
		String message = "";
		final ITimeAwareGraphDatabase taGraph = (ITimeAwareGraphDatabase) graph;
		if(time>0) {
		Date d = new Date(time );
		//taGraph.allNodes(ModelElementNode.OBJECT_VERTEX_LABEL, time).getSingle();
		for(ITimeAwareGraphNode nd: taGraph.allNodes(ModelElementNode.OBJECT_VERTEX_LABEL, time)) {
			
			if (nd.getOutgoingWithType("_hawkFile").iterator().hasNext()) {
				//System.out.println("this has next  " + nd);
				//GraphNodeWrapper wrapper = new TimeAwareGraphNodeWrapper(nd, this);
				//System.out.println("repo node " + getRepository(wrapper));
				//System.out.println("repo message  "+ getRepository(wrapper).getMessage());
				ModelElementNode me = new ModelElementNode(nd);
				String repoURL = me.getFileNode().getRepositoryURL();
				final RepositoryNode repoNode = new VCSManagerIndex(taGraph).getOrCreateRepositoryNode(repoURL).travelInTime(time);
				try {
					
					//System.out.println("Timepoint:   "+d);
					message= repoNode.getMessage() + "\n ( Index:"+ repoNode.getRevision()+ "\n  ( Date:"+ d;
					//message= message + "\n Date:"+ d;
					//return message;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			else 
				message = "commit not available "+ "\n Date:"+ d;
			break;
				
		}
		}
		return message;
	}
	public void allTest(){
		List m =  new ArrayList(getAllInstants());
		Collections.reverse(m);
		for(Object instant: m.toArray()) {
			try {
				getMessage((Long)instant);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void allNewTest() {
		for(Long instant:getAllInstants() ) {
			try {
				getMessage(instant);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/*
	public String getMessage(long timepoint) throws Exception {
		Runtime.getRuntime().gc();
		final AbstractGreycatDatabase taGraph = (AbstractGreycatDatabase) graph;
		IGraphIterable<GreycatNode> iterate=taGraph.allNodes(ModelElementNode.OBJECT_VERTEX_LABEL, timepoint);
		//taGraph.
		int size= iterate.size();
		//iterate.iterator().
		//iterate.
		System.out.println("size  "+size);
		if(size>0 && timepoint>0) {
		//ITimeAwareGraphNode taNode = ((AbstractGreycatDatabase) taGraph.allNodes(ModelElementNode.OBJECT_VERTEX_LABEL, timepoint));
			//Iterables.getFirst(iterate.iterator(), null);
		//Iterables.getFirst(iterate, null);
		for(IGraphNode nd: iterate) {
			if (nd.getOutgoingWithType("_hawkFile").iterator().hasNext()) {
				System.out.println("this has next  " + nd);
				GraphNodeWrapper wrapper = new TimeAwareGraphNodeWrapper(nd, this);
				System.out.println("repo node " + getRepository(wrapper));
				System.out.println("repo message  "+ getRepository(wrapper).getMessage());
				break;
			}
			else {
				System.out.println("this does not have next  "+ nd);
				GraphNodeWrapper wrapper = new TimeAwareGraphNodeWrapper(nd, this);
				System.out.println("repo node  "+ getRepository(wrapper));
				System.out.println("repo message  "+ getRepository(wrapper).getMessage());
				break;
			}
				
		}
		ITimeAwareGraphNode taNode = Iterables.get(iterate, 0);
		//Iterable
		System.out.println("repositoy" + taNode.getProperty("repository"));
		//taNode.
		//taGraph.getFileIndex().travelInTime(timepoint).
		ModelElementNode me = new ModelElementNode(taNode);
		System.out.println("model element "+me);
		
		System.out.println("file node  "+ me.getFileNode());
		
		String repoURL = me.getFileNode().getRepositoryURL();
		System.out.println("repositorrry" + repoURL);
		final RepositoryNode repoNode = new VCSManagerIndex(taGraph).getOrCreateRepositoryNode(repoURL);
		try {
			Date d = new Date(timepoint );
			System.out.println("Timepoint:   "+d);
			String message= repoNode.travelInTime(taNode.getTime()).getMessage();
			System.out.println("message" + message);
			return message;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		return null;
	}
	*/
	public Collection getAllMessage(){
		Collection<String> message = new ArrayList<String>();
	
		for(Long instant: getAllInstants()) {
			try {
				message.add(getMessage(instant));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		final ITimeAwareGraphDatabase taDB = (ITimeAwareGraphDatabase) graph;
		try (IGraphTransaction tx = taDB.beginTransaction()) {
			GraphWrapper gW = new GraphWrapper(taDB);
			for (MetamodelNode mm : gW.getMetamodelNodes()) {
				for (TypeNode tn : mm.getTypes()) {
					ITimeAwareGraphNode taTypeNode = (ITimeAwareGraphNode) tn.getNode();
					//instants.addAll(taTypeNode.getAllInstants());
				}
			}
			tx.success();
		} catch (Exception e) {
			try {
				throw new QueryExecutionException(e);
			} catch (QueryExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		*/
		return message;
	}
	public void getCommit() {
		File f= new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/HawkSimulink/Model/"+indexer.getName()+"/commit.txt");
		File folder = new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/HawkSimulink/Model/"+indexer.getName());
		if(!folder.exists())
			folder.mkdirs();
		PrintWriter writer= null;
		try {
			
			if(!f.exists()) {
				f.createNewFile();
			}
				
			writer = new PrintWriter(f,"UTF-8");
			for (Long instant: getAllInstants()) {
				writer.println(getMessage(instant));
			}
			writer.close();
		} catch (Exception e) {
			if (!(writer.equals(null)))
				writer.close();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void saveModel() {
		//Client client= new Client();
		//Client.Factory.class.
		//HawkModelDescriptor descriptor= new HawkModelDescriptor();
		//this.indexer.getModelParsers().
		//File hawkModel = new File ("C:/Users/student/Documents/eclipse/runtime-EclipseApplication/Hawk/tweet5.localhawkmodel");
		//File f = new File("C:/Users/student/Documents/eclipse/runtime-EclipseApplication/Hawk/model/tweettest.localhawkmodel");
		File f;
		//File dest = new File(f.getAbsolutePath()+".xmi");
		//System.out.println(indexer.getName());
		int num=0;
		PrintWriter writer;
		ResourceSet rs1 = new ResourceSetImpl();
		ResourceSet rs2 = new ResourceSetImpl();
		Resource rSource = null;
		Resource rSource1 = null;
		File file1 = null;
		File file2=null;
		File fd = new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/HawkSimulink/Model/"+indexer.getName()+"/diff.txt");
		File summary = new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/HawkSimulink/Model/"+indexer.getName()+"/summary.txt");
		File folder = new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/HawkSimulink/Model/"+indexer.getName());
		if(!folder.exists())
			folder.mkdirs();
		
		PrintWriter writer2 = null;
		PrintWriter summaryWriter = null;
		//rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		try {
			//loadMetamodel2();
			
			writer2 = new PrintWriter(fd, "UTF-8");
			summaryWriter = new PrintWriter(summary, "UTF-8");
			for(Long instant: getAllInstants()) {
				num++;
				f = new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/HawkSimulink/Model/"+indexer.getName()+"/version"+num +".localhawkmodel");
				System.out.println("f   "+ f.getAbsolutePath());
				if (!f.exists()) {
					f.createNewFile();
				}
				writer = new PrintWriter(f, "UTF-8");
				writer.println(indexer.getName());
				writer.print("repos=*\r\n" + 
							"files=*\r\n");
				writer.println("timepoint="+instant);	
				writer.close();
				
				if (num%2==0) {
					System.out.println("File   "+f.getAbsolutePath());
					rSource = rs1.createResource(URI.createFileURI(f.getPath()));
					System.out.println("resource  "+ rs1);
					System.out.println("rsource  "+rSource);
					rSource.load(null);
					Resource rTarget = new XMIResourceImpl(URI.createFileURI(f.getAbsolutePath()+".xmi"));
					for (Resource r : new ArrayList<>(rs1.getResources())) {
						rTarget.getContents().addAll(new ArrayList<>(r.getContents()));
					}
					rTarget.save(null);
					file1 = new File(f.getAbsolutePath()+".xmi");
					System.out.println("File   "+file1.getAbsolutePath());
					if (rTarget.isLoaded()) {
						rTarget.unload();
					}
					if (rSource.isLoaded()) {
						rSource.unload();
					}
					System.out.println("FileBB   "+f.getAbsolutePath());
				}
				else {
					System.out.println("File   "+f.getAbsolutePath());
					rSource1 = rs2.createResource(URI.createFileURI(f.getPath()));
					rSource1.load(null);
					//System.out.println("rs2  "+rs2.getResources());
					//System.out.println(rs2);
					Resource rTarget = new XMIResourceImpl(URI.createFileURI(f.getAbsolutePath()+".xmi"));
					for (Resource r : new ArrayList<>(rs2.getResources())) {
						//System.out.println("r  "+ r.getContents());
						rTarget.getContents().addAll(new ArrayList<>(r.getContents()));
					}
					rTarget.save(null);
					file2 = new File(f.getAbsolutePath()+".xmi");
					if (rTarget.isLoaded()) {
						rTarget.unload();
					}
					if (rSource1.isLoaded()) {
						rSource1.unload();
					}
					System.out.println("FileBB   "+f.getAbsolutePath());
				}
				
				if(num>=2) {
					
				if (file1!=null && file2!=null) {
					//writer2.println(file1.getName()+"       "+file2.getName());
					//System.out.println(file1.getName()+"  the "+ file2.getName());
					SimCompare object = new SimCompare();
					Comparison compare = null;
					if(num%2==0) {
						writer2.println(file1.getName()+"       "+file2.getName());
						System.out.println(file1.getName()+"  the  "+ file2.getName());
						
						compare= object.compare(file1, file2);
					}		
					else {
						writer2.println(file2.getName()+"       "+file1.getName());
						System.out.println(file2.getName()+"  the "+ file1.getName());
						
						compare= object.compare(file2, file1);
					}
					System.out.println(file1.getName()+"  weee  "+ file2.getName());
					summaryWriter.println(getMessage(instant));
					object.getSummary(compare, summaryWriter);
					for (Diff d:compare.getDifferences()) {
						if(d instanceof ReferenceChange)
							writer2.println("Diff  "+d.getKind() + "  " + ((ReferenceChange) d).getReference().getName()+"  "+d);
						else
							writer2.println("Diff  "+d.getKind() + "            " + d);
						//writer2.println("Diff2  "+d.getKind() + "  " + d);
					}
					
				}
				
				/*
				if (num%2==0) {
					if (rSource1.isLoaded()) {
						rSource1.unload();
					}
				}
				else {
					if (rSource.isLoaded()) {
						rSource.unload();
					}
				}
				
				*/
				}
				writer2.println("");
				writer2.println("");
				writer2.println("Iteration:  "+num);
				writer2.println("");
				summaryWriter.println("");
				summaryWriter.println("Iteration:  "+num);
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}finally {
			
			if (rSource!= null && rSource.isLoaded()) {
				rSource.unload();
			}
			if (rSource1 != null && rSource1.isLoaded()) {
				rSource1.unload();
			}
			if(writer2!=null) {
				writer2.close();
			}
			if(summaryWriter!=null) {
				summaryWriter.close();
			}
	
		}
		/*
		//File dest =  new File ("C:\\Users\\student\\Documents\\eclipse\\runtime-EclipseApplication\\Hawk\\newtest.xmi2");
		ResourceSet rs = new ResourceSetImpl();
		//rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		Resource rSource2 = rs.createResource(URI.createFileURI(hawkModel.getPath()));
		Resource rTarget = null;
		//System.out.println(rSource.toString());
		try {
			rSource2.load(null);
			rTarget = new XMIResourceImpl(URI.createFileURI(hawkModel.getAbsolutePath()+".xmi"));
			for (Resource r : new ArrayList<>(rs.getResources())) {
				rTarget.getContents().addAll(new ArrayList<>(r.getContents()));
			}
			rTarget.save(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if (rTarget != null && rTarget.isLoaded()) {
				rTarget.unload();
			}
			if (rSource2.isLoaded()) {
				rSource2.unload();
			}
		}
		*/
		System.out.println("completed");
	}
	public Comparison compare(Resource resourceSet1, Resource resourceSet2) {
		Function<EObject, String> idFunction = new Function<EObject, String>() {
			public String apply(EObject input) {
				if (input.eClass() instanceof EClass) {
					return input.eClass().getName();
				}
				// a null return here tells the match engine to fall back to the other matchers
				return null;
			}
		};
		
		
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
	
	private Resource loadMetamodel2() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		URI uri2 = URI.createFileURI("C:/Users/student/Documents/eclipse/runtime-EclipseApplication/Hawk/labview.ecore");

		Resource r = resourceSet.getResource(uri2, true);
		//System.out.println(r.getContents());
		EObject eObject = r.getContents().get(0);
		
		EPackage.Registry.INSTANCE.put("http://www.ni.com/LabVIEW.VI", eObject);
		return r;
		
	}
	public Collection<?> getVersion(String date){
		final List<Object> results = new ArrayList<>();
		DateTimeFormatter dateFormatter
        = DateTimeFormatter.ofPattern("d-MM-uuuu", Locale.ENGLISH);
		long starttime = LocalDate.parse(date, dateFormatter)
	            .atStartOfDay(ZoneOffset.UTC)
	            .toInstant()
	            .toEpochMilli();
		long endtime = LocalDate.parse(date, dateFormatter)
	            .atStartOfDay(ZoneOffset.UTC)
	            .toInstant()
	            .toEpochMilli() + (24*3600*1000);
		Collection<Long> instants = getInstantBetween(starttime,endtime);
		
		for (long instant : instants) {
			results.add(Arrays.asList(instant, allInstancesAt(instant)));
				
		} 
		
		return results;
	}
	
	protected Collection<Long> getInstantBetween(long start,long end){
		Collection<Long> instants = getAllInstants();
		Collection<Long> results=new TreeSet<>();
		for (long instant : instants) {
			if (instant <= start && instant >= end)
				results.add(instant);		
		} 
		return results;
	}
	public String getMessage() {
		String message="";
		for (IVcsManager manager: indexer.getRunningVCSManagers()) {
			//manager.
			
		}
		return message;
	}
	public Collection<Long> getAllInstants() {
		final Set<Long> instants = new TreeSet<>();
		final ITimeAwareGraphDatabase taDB = (ITimeAwareGraphDatabase) graph;
		try (IGraphTransaction tx = taDB.beginTransaction()) {
			GraphWrapper gW = new GraphWrapper(taDB);
			//gW.
			for (MetamodelNode mm : gW.getMetamodelNodes()) {
				for (TypeNode tn : mm.getTypes()) {
					ITimeAwareGraphNode taTypeNode = (ITimeAwareGraphNode) tn.getNode();
					//System.out.println(taTypeNode.getOutgoingWithType("_hawkFile").iterator().hasNext());
					
					//taTypeNode.
					instants.addAll(taTypeNode.getAllInstants());
				}
			}
			tx.success();
		} catch (Exception e) {
			try {
				throw new QueryExecutionException(e);
			} catch (QueryExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return instants;
	}
	@Override
	public Object query(IModelIndexer m, String query, Map<String, Object> context)
			throws InvalidQueryException, QueryExecutionException {
		final HawkState currentState = m.getCompositeStateListener().getCurrentState();
		if (currentState != HawkState.RUNNING) {
			throw new QueryExecutionException(
					String.format("Cannot run the query, as the indexer is not in the RUNNING state: it is %s instead.",
							currentState));
		}
		if (!(m.getGraph() instanceof ITimeAwareGraphDatabase)) {
			throw new QueryExecutionException(getClass().getName() + " can only be used with time-aware backends");
		}

		String defaultnamespaces = null;
		if (context != null) {
			defaultnamespaces = (String) context.get(PROPERTY_DEFAULTNAMESPACES);
		}

		final VersionEOLQueryEngine q = new VersionEOLQueryEngine();
		try {
			q.load(m);
			q.setDefaultNamespaces(defaultnamespaces);
			if (context == null || context.isEmpty()) {
				// nothing to do!
			} else {
				q.setContext(context);
			}
		} catch (EolModelLoadingException e) {
			throw new QueryExecutionException("Loading of EOLQueryEngine failed");
		}

		final IEolModule module = createModule();
		module.getContext().setOperationFactory(new TimeAwareEOLOperationFactory(q));

		final OperationContributorRegistry opcRegistry = module.getContext().getOperationContributorRegistry();
		opcRegistry.add(new TimeAwareNodeHistoryOperationContributor(q));
		opcRegistry.add(new TypeHistoryOperationContributor(q));
		parseQuery(query, context, q, module);
		return q.runQuery(module);
	}
	
	public void save() throws Exception {
		File f = new File("C:\\Users\\student\\Documents\\eclipse\\runtime-EclipseApplication\\Hawk\\model\\test.localhawkmodel");
		File dest = new File(f.getAbsolutePath()+".xmi");
		if (!f.exists()) {
			f.createNewFile();
		}
		PrintWriter writer = new PrintWriter(f, "UTF-8");
		writer.println("myhawk5");
		writer.println("repos=*\r\n" + 
				"files=*\r\n" + 
				"timepoint=1567990714609");
		
		writer.close();
		URI uri = URI.createFileURI(f.getAbsolutePath());
		
		ResourceSet rs = new ResourceSetImpl();
		Resource rSource = rs.createResource(URI.createURI(uri.toString()));
		Resource rTarget = null;
		try {
			rSource.load(null);
		
		rTarget = new XMIResourceImpl(URI.createFileURI(dest.getAbsolutePath()));
		for (Resource r : new ArrayList<>(rs.getResources())) {
			rTarget.getContents().addAll(new ArrayList<>(r.getContents()));
		}
		rTarget.save(null);
		}
		finally {
			if (rTarget != null && rTarget.isLoaded()) {
				rTarget.unload();
			}
			if (rSource.isLoaded()) {
				rSource.unload();
			}
		}
		
	}
	private void loadMetamodel() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		URI uri2 = URI.createFileURI("C:/Users/student/git/hawk/labview.ecore");

		Resource r = resourceSet.getResource(uri2, true);
		//System.out.println(r.getContents());
		EObject eObject = r.getContents().get(0);
		
		EPackage.Registry.INSTANCE.put("http://www.ni.com/LabVIEW.VI", eObject);
		//return r;
	}
	public void saveOld() throws Exception {
		File f = new File("C:\\Users\\student\\Documents\\eclipse\\runtime-EclipseApplication\\Hawk\\model\\test.localhawkmodel");
		File dest = new File(f.getAbsolutePath()+".xmi");
		PrintWriter writer = new PrintWriter(f.getName(), "UTF-8");
		writer.println("myhawk5");
		writer.println("repos=*\r\n" + 
				"files=*\r\n" + 
				"timepoint=1567990714609");
		
		//f.
		//System.out.println("test  "+ getFileName(file.getName()));
		//writer.println("The second line");
		writer.close();
		URI uri = URI.createFileURI(f.getAbsolutePath());
		//graph.getPath()
		//EPackage.Registry.INSTANCE.put(key, uri);
		//System.out.println(uri.toString());
		//HawkResourceFactoryImpl.generateHawkURL(d, true);;
		//graph.getFileIndex().
		//indexer.getRunningVCSManagers().
		//HawkResourceFactoryImpl fact = new HawkResourceFactoryImpl();
		//System.out.println(indexer.getId()+"  test  " + indexer.getName() + "  "+ indexer.getDerivedAttributeExecutionEngine());
		//fact.
		//System.out.println(HawkModelDescriptor.DEFAULT_URL);
		//RemoteHawkModel hawk = new RemoteHawkModel();
		//hawk.loadModel();
		System.out.println("successfully loaded");
		//System.out.println(hawk.getAllOfTypeFromModel("Terminal"));
		LocalHawkResourceImpl localHawk= new LocalHawkResourceImpl(uri,indexer,true,Arrays.asList("*"),Arrays.asList("*"));
		//EPackage.Registry.INSTANCE.put("http://www.example.org/LabView", new EcoreResourceFactoryImpl());
		//Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		//LabView (http://www.example.org/LabView) [resource_from_epackage_http://www.example.org/LabView]
		System.out.println("work");
		System.out.println(uri.toString() + " get " + uri.toFileString() + " hf " + uri.path());
		System.out.println(localHawk.RESOURCE__RESOURCE_SET);
		System.out.println(localHawk.toString());
		ResourceSet rs = new ResourceSetImpl();
		Resource rSource = rs.createResource(URI.createURI(uri.toString()));
		System.out.println("before load");
		rSource.load(null);
		System.out.println(rSource.getContents());
		System.out.println(rs.getResources());
		System.out.println(rs.getPackageRegistry());
		Resource rTarget = null;
		rTarget = new XMIResourceImpl(URI.createFileURI(dest.getAbsolutePath()));
		for (Resource r : new ArrayList<>(rs.getResources())) {
			rTarget.getContents().addAll(new ArrayList<>(r.getContents()));
		}
		rTarget.save(null);
		LocalHawkResourceFactoryImpl fl = new LocalHawkResourceFactoryImpl();
		LocalHawkResourceImpl local = (LocalHawkResourceImpl)fl.createResource(uri);
		System.out.println(local.getResourceSet() + "  from local factory");
		//System.out.println((fl.createResource(uri)).toString());
		final Iterator<? extends IGraphNode> itMetamodelNode = graph.getMetamodelIndex().get("id", "http://www.ni.com/LabVIEW.VI").iterator();
		IGraphNodeIndex mynode =graph.getFileIndex();
		//System.out.println(mynode);
		if (!itMetamodelNode.hasNext()) {
			throw new NoSuchElementException(String.format("Metamodel %s could not be found ", "http://www.ni.com/LabVIEW.VI"));
		}
		
		final IGraphNode epackagenode = itMetamodelNode.next();
		final String s = epackagenode.getProperty(IModelIndexer.METAMODEL_RESOURCE_PROPERTY) + "";
		//System.out.println("new string" + s);
		
		final String ep = epackagenode.getProperty(IModelIndexer.IDENTIFIER_PROPERTY) + "";
		final String type = epackagenode.getProperty(IModelIndexer.METAMODEL_TYPE_PROPERTY) + "";
		
		//System.out.println(localHawk.getRegisteredMetamodels());
		//System.out.println(localHawk.getURI());
		//System.out.println(ResourceSet.);
		//System.out.println(indexer.);
		final List<String> ids = new ArrayList<>();
		Object rawResult= allInstancesNow();
		addAllResults(rawResult, ids);
		System.out.println(ids);
		//localHawk.fetchNodes(ids, true);
		//PrintWriter writer = new PrintWriter("tester.xml", "UTF-8");
		//writer.println(localHawk.fetchNodes(ids, true));
		System.out.println(localHawk.fetchNodes(ids, true));
		writer.close();
		
	}
	private List<String> addAllResults(Object rawResult, List<String> ids) {
		if (rawResult instanceof Iterable) {
			for (Object rawElem : (Iterable<?>)rawResult) {
				addAllResults(rawElem, ids);
			}
		} else if (rawResult instanceof IGraphNodeReference) {
			IGraphNodeReference ref = (IGraphNodeReference)rawResult;
			ids.add(ref.getId());
		}
		return ids;
	}
	
	public void saveModelN() {

		
		File f;
		int num=0;
		PrintWriter writer;
		ResourceSet rs1 = new ResourceSetImpl();
		Resource rSource = null;
		Resource rSource1 = null;
		File file1 = null;
		File file2=null;
		Map<String, Integer> pair = new HashMap();
		int add,move,change,delete,others;
		String name;
		File fd = new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/HawkSimulink/Model/"+indexer.getName()+"/diff.txt");
		File summary = new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/HawkSimulink/Model/"+indexer.getName()+"/summary.txt");
		File folder = new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/HawkSimulink/Model/"+indexer.getName());
		if(!folder.exists())
			folder.mkdirs();
		
		PrintWriter writer2 = null;
		PrintWriter summaryWriter = null;
		int curi=0;
		//rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		try {
			//loadMetamodel2();
			
			writer2 = new PrintWriter(fd, "UTF-8");
			summaryWriter = new PrintWriter(summary, "UTF-8");
			for(Long instant: getAllInstants()) {
				num++;
				f = new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/HawkSimulink/Model/"+indexer.getName()+"/version"+num +".localhawkmodel");
				String fol = "C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/HawkSimulink/Model/"+indexer.getName()+"/version"+num ;
				System.out.println("fdd   "+ f.getAbsolutePath());
				if (!f.exists()) {
					f.createNewFile();
				}
				writer = new PrintWriter(f, "UTF-8");
				writer.println(indexer.getName());
				writer.print("repos=*\r\n" + 
							"files=*\r\n");
				writer.println("timepoint="+instant);	
				writer.close();
				rSource = rs1.createResource(URI.createFileURI(f.getPath()));
				rSource.load(null);
				Resource rTarget = new XMIResourceImpl(URI.createFileURI(f.getAbsolutePath()+".xmi"));
				for (Resource r : new ArrayList<>(rs1.getResources())) {
					rTarget.getContents().addAll(new ArrayList<>(r.getContents()));
				}
				rTarget.save(null);
				File fi = new File(f.getAbsolutePath()+".xmi");
				if (rTarget.isLoaded()) {
					rTarget.unload();
				}
				if (rSource.isLoaded()) {
					rSource.unload();
				}
				localParse(fi,fol);
			}
			//curnum=num;
			System.out.println("number  "+ num);
			for(int i=2; i<= num;i++) {
				add=move=change=delete=others=0;
				file1 = new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/HawkSimulink/Model/"+indexer.getName()+"/version"+(i-1 )+".localhawkmodel.xmi");
				file2 = new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/HawkSimulink/Model/"+indexer.getName()+"/version"+i +".localhawkmodel.xmi");				
				System.out.println(file1.getName()+"  before null  "+ file2.getName());
				if (file1!=null && file2!=null) {
					SimCompare object = new SimCompare();
					Comparison compare = null;
					writer2.println(file1.getName()+"       "+file2.getName());
					System.out.println(file1.getName()+"  the  "+ file2.getName());
						
					compare= object.compare(file2, file1);
					//summaryWriter.println(getMessage(instant));
					//object.getSummary(compare, summaryWriter);
					for (Diff d:compare.getDifferences()) {
						if(d instanceof ReferenceChange) {
							writer2.println("Diff  "+d.getKind() + "  " + ((ReferenceChange) d).getReference().getName()+"  "+d);
							name=d.getKind()+ " "+((ReferenceChange) d).getReference().getName();
							//System.out.println(name);
							if(pair.containsKey(name))
								pair.replace(name, (pair.get(name)+1));
							else
								pair.put(name, 1);
						}
						else {
							writer2.println("Diff  "+d.getKind() + "            " + d);
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
						//writer2.println("Diff2  "+d.getKind() + "  " + d);
					}
				curi=i;
				
				}
				
				writer2.println("");
				writer2.println("");
				writer2.println("Iteration:  "+num);
				writer2.println("");
				summaryWriter.println("ADD   "+ add);
				summaryWriter.println("CHANGE   "+ change);
				summaryWriter.println("DELETE   "+ delete);
				summaryWriter.println("MOVE   "+ move);
				summaryWriter.println("OTHERS   "+ others);
				summaryWriter.println(pair);
				summaryWriter.println("");
				summaryWriter.println("Iteration:  "+i);
			}
			
		}
		catch (Exception e) {
			if((curi+1)<num) {
				for(int i=(curi+2); i<= num;i++) {
					file1 = new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/HawkSimulink/Model/"+indexer.getName()+"/version"+(i-1 )+".localhawkmodel.xmi");
					file2 = new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/HawkSimulink/Model/"+indexer.getName()+"/version"+i +".localhawkmodel.xmi");				
					if (file1!=null && file2!=null) {
						SimCompare object = new SimCompare();
						Comparison compare = null;
						writer2.println(file1.getName()+"       "+file2.getName());
						System.out.println(file1.getName()+"  the  "+ file2.getName());
							
						compare= object.compare(file2, file1);
						//summaryWriter.println(getMessage(instant));
						object.getSummary(compare, summaryWriter);
						for (Diff d:compare.getDifferences()) {
							if(d instanceof ReferenceChange) 
								writer2.println("Diff  "+d.getKind() + "  " + ((ReferenceChange) d).getReference().getName()+"  "+d);
							else
								writer2.println("Diff  "+d.getKind() + "            " + d);
							//writer2.println("Diff2  "+d.getKind() + "  " + d);
						}
					//curi=i;
					
					}
				
				}
			}
			
			e.printStackTrace();
		}
		catch (OutOfMemoryError e) {
			if(writer2!=null) {
				writer2.close();
			}
			if(summaryWriter!=null) {
				summaryWriter.close();
			}
			if (rSource!= null && rSource.isLoaded()) {
				rSource.unload();
			}
			
		}
		
		finally {
			
			if (rSource!= null && rSource.isLoaded()) {
				rSource.unload();
			}
			if (rSource1 != null && rSource1.isLoaded()) {
				rSource1.unload();
			}
			if(writer2!=null) {
				writer2.close();
			}
			if(summaryWriter!=null) {
				summaryWriter.close();
			}
	
		}
		
		System.out.println("completed");
	
	}
	
	public static File localParse(File file, String nm) throws ParserConfigurationException, IOException, SAXException {
		File f = null;
		File folder = new File (nm);
		if (!folder.exists())
			folder.mkdirs();
		//File f= new File(t.getAbsoluteFile()+"/newfiles/"+getFileName(file.getName()));
		//System.out.println("file parser is called");
		//System.out.println(f.getAbsolutePath());
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setIgnoringElementContentWhitespace(true);
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(file);
	    NodeList listA= doc.getElementsByTagName("simulink:SimulinkModel");
	    Node node;
	    for (int i=0; i< listA.getLength();i++) {
	    node = (Node) listA.item(i);
	    
	    try {
			//System.out.println(nodeToString(node));
	    	f = new File (folder.getAbsolutePath()+"/"+file.getName()+i);
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
	
	private static String nodeToString(Node node)
			throws TransformerException
			{
			    StringWriter buf = new StringWriter();
			    Transformer xform = TransformerFactory.newInstance().newTransformer();
			    xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			    xform.transform(new DOMSource(node), new StreamResult(buf));
			    return(buf.toString());
			}
	
	@Override
	public String getHumanReadableName() {
		//loadMetamodel();
		return "Version Aware EOL Query Engine";
	}
}
