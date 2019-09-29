package org.hawk.versionaware.queries;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
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
import org.hawk.core.IModelIndexer;
import org.hawk.core.IStateListener.HawkState;
import org.hawk.core.graph.IGraphNode;
import org.hawk.core.graph.IGraphNodeIndex;
import org.hawk.core.graph.IGraphNodeReference;
import org.hawk.core.graph.IGraphTransaction;
import org.hawk.core.graph.timeaware.ITimeAwareGraphDatabase;
import org.hawk.core.graph.timeaware.ITimeAwareGraphNode;
import org.hawk.core.query.InvalidQueryException;
import org.hawk.core.query.QueryExecutionException;
import org.hawk.core.runtime.BaseModelIndexer;
import org.hawk.emfresource.impl.LocalHawkResourceImpl;
import org.hawk.epsilon.emc.EOLQueryEngine;
import org.hawk.epsilon.emc.wrappers.GraphNodeWrapper;
import org.hawk.graph.GraphWrapper;
import org.hawk.graph.MetamodelNode;
import org.hawk.graph.ModelElementNode;
import org.hawk.graph.TypeNode;
import org.hawk.service.api.Hawk.Client;
import org.hawk.service.emc.RemoteHawkModel;
import org.hawk.service.emf.HawkModelDescriptor;
import org.hawk.service.emf.impl.HawkResourceFactoryImpl;
//import org.hawk.service.api.Users$Client;
import org.hawk.timeaware.queries.TimeAwareEOLOperationFactory;
import org.hawk.timeaware.queries.TimeAwareEOLQueryEngine;
import org.hawk.timeaware.queries.operations.reflective.TimeAwareNodeHistoryOperationContributor;
import org.hawk.timeaware.queries.operations.reflective.TypeHistoryOperationContributor;
import org.hawk.ui.emfresource.LocalHawkResourceFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;

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
	public void saveModel(Collection elements) {
		//Client client= new Client();
		//Client.Factory.class.
		HawkModelDescriptor descriptor= new HawkModelDescriptor();
		//this.indexer.getModelParsers().
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
	public Collection<Long> getAllInstants() {
		final Set<Long> instants = new TreeSet<>();
		final ITimeAwareGraphDatabase taDB = (ITimeAwareGraphDatabase) graph;
		try (IGraphTransaction tx = taDB.beginTransaction()) {
			GraphWrapper gW = new GraphWrapper(taDB);
			for (MetamodelNode mm : gW.getMetamodelNodes()) {
				for (TypeNode tn : mm.getTypes()) {
					ITimeAwareGraphNode taTypeNode = (ITimeAwareGraphNode) tn.getNode();
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
	
	
	
	@Override
	public String getHumanReadableName() {
		return "Version Aware EOL Query Engine";
	}
}
