package org.hawk.time;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.hawk.core.IModelIndexer;
import org.eclipse.hawk.core.VcsCommitItem;
import org.eclipse.hawk.core.graph.IGraphChangeListener;
import org.eclipse.hawk.core.graph.IGraphDatabase;
import org.eclipse.hawk.core.graph.IGraphEdge;
import org.eclipse.hawk.core.graph.IGraphNode;
import org.eclipse.hawk.core.graph.IGraphNodeIndex;
import org.eclipse.hawk.core.graph.IGraphTransaction;
import org.eclipse.hawk.core.graph.timeaware.ITimeAwareGraphDatabase;
import org.eclipse.hawk.core.model.IHawkAttribute;
import org.eclipse.hawk.core.model.IHawkClass;
import org.eclipse.hawk.core.model.IHawkModelResource;
import org.eclipse.hawk.core.model.IHawkObject;
import org.eclipse.hawk.core.model.IHawkReference;
import org.eclipse.hawk.emf.EMFWrapperFactory;
import org.eclipse.hawk.emf.model.EMFModelResource;
import org.eclipse.hawk.emfresource.impl.LocalHawkResourceImpl;
import org.eclipse.hawk.graph.FileNode;
import org.eclipse.hawk.graph.ModelElementNode;
import org.eclipse.hawk.graph.updater.DeletionUtils;
import org.eclipse.hawk.graph.updater.GraphModelBatchInjector;
import org.eclipse.hawk.graph.updater.GraphModelInserter;
import org.eclipse.hawk.graph.updater.GraphModelUpdater;
import org.eclipse.hawk.graph.updater.TypeCache;
import org.eclipse.hawk.graph.util.GraphUtil;
import org.hawk.change.ModelChangeManager;
import org.hawk.change.ModelChangeManager.ModelChangeNode;
import org.eclipse.hawk.graph.Slot;

public class TimeModelInserter extends GraphModelInserter{
	private IModelIndexer indexer;
	private IGraphDatabase graph;
	private GraphModelBatchInjector inj;
	private VcsCommitItem commitItem;
	private IHawkModelResource resource;

	private Map<String, IGraphNode> nodes = new HashMap<>();
	private TypeCache typeCache;
	private Supplier<DeletionUtils> deletionUtils;
	private static int num=0;
	private IGraphNode fNode=null;
	protected Map<String, IHawkObject> deleted = new HashMap<>();
	protected ModelChangeManager changeManager;

	public TimeModelInserter(IModelIndexer hawk, Supplier<DeletionUtils> deletionUtils, TypeCache typeCache) {
		super(hawk, deletionUtils, typeCache);
		this.indexer = hawk;
		this.graph = indexer.getGraph();
		this.typeCache = typeCache;
		this.deletionUtils = deletionUtils;
		this.changeManager = new ModelChangeManager((ITimeAwareGraphDatabase) this.graph);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void remove(IGraphNode node, IGraphNode fileNode, final IGraphChangeListener listener) {
		// track change deleted node
		IGraphNode gn = node;
		ModelElementNode men = new ModelElementNode(gn);
		String uriFragment = men.getElementId();
		Map<String, Slot> slots = men.getTypeNode().getSlots();
		//men.
		
		System.out.println("type nodes  "+uriFragment);
		for (Slot s : slots.values()) {
		  System.out.println(s.getName()+"  "+s.getType()+"  "+men.getSlotValue(s));
		}
		final ModelChangeNode nd= changeManager.getOrCreateChangeNode(uriFragment);
		nd.setChangeType("delete");
		nd.setModelType(men.getTypeNode().getTypeName());
		//EMFModelResource resource= getResource(fileNode);
		//resource.
		
		//resource.
		
		super.remove(node, fileNode, listener);
		/*
		for (String key : node.getPropertyKeys()) {
			listener.modelElementAttributeRemoval(this.commitItem, null, key, node,
					ModelElementNode.TRANSIENT_ATTRIBUTES.contains(key));
		}
		for (IGraphEdge e : node.getOutgoing()) {
			if (e.getProperty(DERIVED_FEATURE_EDGEPROP) == null) {
				final boolean isTransient = ModelElementNode.TRANSIENT_EDGE_LABELS.contains(e.getType());
				listener.referenceRemoval(this.commitItem, node, e.getEndNode(), e.getType(), isTransient);
			}
		}
		
		remove(node, repoURL, fileNode, listener);
		
		*/
		
	}
	
	
	@Override
	protected boolean transactionalUpdate(final boolean verbose) throws Exception {
		System.out.println("I am called hgh");
		System.out.println("size of add:  "+ added.size());
		for (final Map.Entry<String, IHawkObject> entry : added.entrySet()) {
			final String uriFragment = entry.getKey();
			final IHawkObject o = entry.getValue();
			System.out.println("type name: "+ o.getType().getName());
			System.out.println("uri fragment:   "+ uriFragment);
			final ModelChangeNode nd= changeManager.getOrCreateChangeNode(uriFragment);
			System.out.println("change type:  "+nd.getChangeType());
			nd.setChangeType("add");
			System.out.println("model  type1:  "+nd.getModelType());
			nd.setModelType(o.getType().getName());
			System.out.println("model type2:  "+nd.getModelType());
			nd.setURIFragment(uriFragment);
		}
		System.out.println("size of add again:  "+ added.size());
		boolean result = super.transactionalUpdate(verbose);
		return result;
		
	}
	
	
	protected void updateNodeProperties(IGraphNode fileNode, IGraphNode node, IHawkObject eObject) {

		List<IHawkAttribute> normalattributes = new LinkedList<IHawkAttribute>();
		List<IHawkAttribute> indexedattributes = new LinkedList<IHawkAttribute>();
		IGraphNode typenode = node.getOutgoingWithType(ModelElementNode.EDGE_LABEL_OFTYPE).iterator().next()
				.getEndNode();
		final IGraphChangeListener listener = indexer.getCompositeGraphChangeListener();
		final ModelChangeNode nd= changeManager.getOrCreateChangeNode(eObject.getUriFragment());
		nd.setModelType(eObject.getType().getName());
		Map<String,String> changes= new HashMap<String,String>();

		for (final IHawkAttribute eAttribute : ((IHawkClass) eObject.getType()).getAllAttributes()) {
			final String attrName = eAttribute.getName();
			if (eObject.isSet(eAttribute)) {
				final String[] propValue = (String[]) typenode.getProperty(attrName);
				if (propValue != null && "t".equals(propValue[5])) {
					indexedattributes.add(eAttribute);
				}

				normalattributes.add(eAttribute);
			} else if (node.getProperty(attrName) != null) {
				node.removeProperty(attrName);
				//changes.p
				
				indexer.getCompositeGraphChangeListener().modelElementAttributeRemoval(commitItem, eObject, eAttribute.getName(),
						node, false);
			}
		}

		for (IHawkAttribute a : normalattributes) {
			final Object oldproperty = node.getProperty(a.getName());
			final Object newproperty = eObject.get(a);

			if (!a.isMany()) {
				Object newValue = newproperty;
				if (newValue instanceof Date) {
					newValue = formatDate((Date)newValue);
				} else if (!GraphUtil.isPrimitiveOrWrapperType(newproperty.getClass())) {
					newValue = newValue.toString();
				}

				if (!newValue.equals(oldproperty)) {
					// track changed property (primitive)
					listener.modelElementAttributeUpdate(this.commitItem, eObject, a.getName(), oldproperty, newValue, node,
							false);
					node.setProperty(a.getName(), newValue);
				}
			} else {
				Collection<Object> collection = null;

				if (a.isUnique())
					collection = new LinkedHashSet<Object>();
				else
					collection = new LinkedList<Object>();

				final Collection<?> srcCollection = (Collection<?>) newproperty;
				Class<?> elemClass = null;
				boolean primitiveOrWrapperClass = false;
				if (!srcCollection.isEmpty()) {
					final Object first = srcCollection.iterator().next();
					elemClass = first.getClass();
					primitiveOrWrapperClass = GraphUtil.isPrimitiveOrWrapperType(elemClass);
					if (primitiveOrWrapperClass) {
						for (Object o : srcCollection) {
							collection.add(o);
						}
					} else if (first instanceof Date) {
						for (Object o : srcCollection) {
							collection.add(formatDate((Date) o));
						}
					} else {
						for (Object o : srcCollection) {
							collection.add(o.toString());
						}
					}
				}

				Object r = null;
				if (primitiveOrWrapperClass && elemClass != null) {
					r = Array.newInstance(elemClass, collection.size());
				} else {
					r = Array.newInstance(String.class, collection.size());
				}
				Object ret = collection.toArray((Object[]) r);

				if (!ret.equals(oldproperty)) {
					listener.modelElementAttributeUpdate(this.commitItem, eObject, a.getName(), oldproperty, ret, node, false);
					node.setProperty(a.getName(), ret);
				}
			}

		}

		for (IHawkAttribute a : indexedattributes) {

			IGraphNodeIndex i = graph.getOrCreateNodeIndex(
					eObject.getType().getPackageNSURI() + "##" + eObject.getType().getName() + "##" + a.getName());

			Object v = eObject.get(a);

			if (!a.isMany()) {
				if (GraphUtil.isPrimitiveOrWrapperType(v.getClass())) {
					i.add(node, a.getName(), v);
				} else if (v instanceof Date) {
					i.add(node, a.getName(), formatDate((Date)v));
				} else {
					i.add(node, a.getName(), v.toString());
				}
			} else {
				Collection<Object> collection = null;

				if (a.isUnique())
					collection = new LinkedHashSet<Object>();
				else
					collection = new LinkedList<Object>();

				final Collection<?> srcCollection = (Collection<?>) v;
				Class<?> elemClass = null;
				boolean primitiveOrWrapperClass = false;
				if (!srcCollection.isEmpty()) {
					final Object first = srcCollection.iterator().next();
					elemClass = first.getClass();
					primitiveOrWrapperClass = GraphUtil.isPrimitiveOrWrapperType(elemClass);
					if (primitiveOrWrapperClass) {
						for (Object o : srcCollection) {
							collection.add(o);
						}
					} else if (first instanceof Date) {
						for (Object o : srcCollection) {
							collection.add(formatDate((Date)o));
						}
					} else {
						for (Object o : srcCollection) {
							collection.add(o.toString());
						}
					}
				}

				Object r = null;
				if (primitiveOrWrapperClass && elemClass != null) {
					r = Array.newInstance(elemClass, 1);
				} else {
					r = Array.newInstance(String.class, 1);
				}
				Object ret = collection.toArray((Object[]) r);

				i.add(node, a.getName(), ret);

			}

		}

		final IGraphNodeIndex rootDictionary = graph.getOrCreateNodeIndex(GraphModelBatchInjector.ROOT_DICT_NAME);
		if (eObject.isRoot()) {
			rootDictionary.add(node, GraphModelBatchInjector.ROOT_DICT_FILE_KEY, fileNode.getId());
		} else {
			rootDictionary.remove(node);
		}
	}
	
	public EMFModelResource getResource(IGraphNode fileNode) {
		try{
		File f=new File("test"+ (num) +"tree.localhawkmodel");
		PrintWriter writer = new PrintWriter(f, "UTF-8");
		writer.println(indexer.getName());
		//fileNode.
		
		//final IGraphNode fileNode = new TimeUtil().getFileNodeFromVCSCommitItem(graph, commitItem);
		if (fileNode != null) {
			FileNode fn= new FileNode(fileNode);
			writer.print("repos="+fn.getRepositoryURL()+"\r\n");
			writer.print("files="+fn.getFilePath()+"\r\n");
			//return calculateModelDeltaRatio(fileNode, verbose);
			
		} 
		writer.close();
		ResourceSet rs1 = new ResourceSetImpl();
		Resource rSource = null;
		rSource = rs1.createResource(URI.createFileURI(f.getPath()));
		rSource.load(null);
		System.out.println("size of new rSource   "+rSource.getContents().size());
		Resource rTarget = new XMIResourceImpl(URI.createFileURI(f.getAbsolutePath()+".xmi"));
		for (Resource r : new ArrayList<>(rs1.getResources())) {
			rTarget.getContents().addAll(new ArrayList<>(r.getContents()));
		}
		rTarget.save(null);
		System.out.println("size of rTarget   "+rTarget.getContents().size());
		if (rTarget.isLoaded()) {
			//rTarget.unload();
		}
		if (rSource.isLoaded()) {
			rSource.unload();
		}
		EMFModelResource res = new EMFModelResource(rTarget, new EMFWrapperFactory(), null);
		return res;
		}
		catch(Exception e) {
			
		}
		return null;
	}
	/*
	public boolean run(IHawkModelResource res, VcsCommitItem s, final boolean verbose) throws Exception {
		System.out.println("run called");
		indexer.getCompositeStateListener().info("time inserter called: ");
		this.commitItem = s;
		this.resource = res;
		//test();
		boolean result= super.run(res, s, verbose);
		//
		num++;
		return result;
	}
	protected double calculateModelDeltaRatio(boolean verbose) throws Exception {	
		//test();
		//double res = super.calculateModelDeltaRatio(verbose);
		//if (res<0) {
	//		test2();
		//}
		//System.out.println("single modelta ratio callled  "+res);
		return 0.0;
	}
	
	protected double calculateModelDeltaRatio(IGraphNode fileNode, boolean verbose) throws Exception {
			super.calculateModelDeltaRatio(fileNode, verbose);
			System.out.println("double calculatemodeldeltaratio callled");
			test1();
			/*
			 * We want to always do a transactional update - batch update starts by removing
			 * every existing node, so we would lose track of the various versions of each
			 * model element.
			 */
		/*
			return 0;
	}
	public void test1() throws Exception{
		ResourceSet rs1 = new ResourceSetImpl();
		//resource.
		Resource rSource = null;
		File f=new File("test"+ (num) +"tree.localhawkmodel");
		indexer.getCompositeStateListener().info("file location    "+f.getAbsolutePath());
		
		PrintWriter writer = new PrintWriter(f, "UTF-8");
		writer.println(indexer.getName());
		
		final IGraphNode fileNode = new TimeUtil().getFileNodeFromVCSCommitItem(graph, commitItem);
		if (fileNode != null) {
			FileNode fn= new FileNode(fileNode);
			writer.print("repos="+fn.getRepositoryURL()+"\r\n");
			writer.print("files="+fn.getFilePath()+"\r\n");
			//return calculateModelDeltaRatio(fileNode, verbose);
			
		} else {
			writer.print("repos=*\r\n" + 
								"files=*\r\n");
		}
		writer.close();
		rSource = rs1.createResource(URI.createFileURI(f.getPath()));
		//System.out.println(rSource.);
		rSource.load(null);
		System.out.println("size of rSource   "+rSource.getContents().size());
		
		//rSource.
		Resource rTarget = new XMIResourceImpl(URI.createFileURI(f.getAbsolutePath()+".xmi"));
		for (Resource r : new ArrayList<>(rs1.getResources())) {
			rTarget.getContents().addAll(new ArrayList<>(r.getContents()));
		}
		rTarget.save(null);
		System.out.println("size of rTarget   "+rTarget.getContents().size());
		if (rTarget.isLoaded()) {
			rTarget.unload();
		}
		
		if (rSource.isLoaded()) {
			rSource.unload();
		}
		
	
	}
	public void test2() throws Exception {
		ResourceSet rs1 = new ResourceSetImpl();
		
		 
		 

		//Resource rs= rs1.createResource(uri);
		File f=new File("testy"+ (num) +".localhawkmodel");
		
		Resource rTarget1 = new XMIResourceImpl(URI.createFileURI(f.getAbsolutePath()+".xmi"));
		//Resource rTarget2 = new XMIResourceImpl(URI.createFileURI(f.getAbsolutePath()+"t.xmi"));
		Resource rTarget3 = new XMIResourceImpl(URI.createFileURI(f.getAbsolutePath()+"z.xmi"));
		
		if(resource instanceof EMFModelResource) {
			Resource ref = ((EMFModelResource) resource).getResource();
			//rTarget2.getContents().addAll(new ArrayList<>(ref.getContents()));
			Copier copier = new Copier();
			 Collection<EObject> results = copier.copyAll(ref.getContents());
			  copier.copyReferences();
			  rTarget1.getContents().addAll(new ArrayList<>(results));
			
		}
		//rTarget2.save(null);
		
		rTarget1.save(null);
		
		System.out.println("size of rTarget 2  "+rTarget1.getContents().size());
		
		//if (rTarget1.isLoaded()) {
			//rTarget1.unload();
		//}
		
		if(resource instanceof EMFModelResource) {
			Resource ref = ((EMFModelResource) resource).getResource();
			Copier copier = new Copier();
			 Collection<EObject> results = copier.copyAll(ref.getContents());
			  copier.copyReferences();
			rTarget3.getContents().addAll(new ArrayList<>(results));
			
		}
		rTarget3.save(null);
		if (rTarget1.isLoaded()) {
			System.out.println("target 1 is looaded");
			rTarget1.unload();
		}
		if (rTarget3.isLoaded()) {
			System.out.println("target 3 is looaded");
			rTarget3.unload();
		}
	
		
	}
	public void test() throws Exception {
		ResourceSet rs1 = new ResourceSetImpl();
		//resource.
		Resource rSource = null;
		File f=new File("test"+ (num) +".localhawkmodel");
		indexer.getCompositeStateListener().info("file location    "+f.getAbsolutePath());
		
		PrintWriter writer = new PrintWriter(f, "UTF-8");
		writer.println(indexer.getName());
		//writer.print("repos=*\r\n" + 
		//			"files=*\r\n");
		//writer.println("timepoint="+instant);	
		//URI n;
		LocalHawkResourceImpl hawkre;
		
		final IGraphNode fileNode = new TimeUtil().getFileNodeFromVCSCommitItem(graph, commitItem);
		//fileNode.
		if (fileNode != null) {
			FileNode fn= new FileNode(fileNode);
			writer.print("repos="+fn.getRepositoryURL()+"\r\n");
			writer.print("files="+fn.getFilePath()+"\r\n");
			//return calculateModelDeltaRatio(fileNode, verbose);
			
		} else {
			writer.print("repos=*\r\n" + 
								"files=*\r\n");
		}
		writer.close();
		rSource = rs1.createResource(URI.createFileURI(f.getPath()));
		//System.out.println(rSource.);
		rSource.load(null);
		System.out.println("size of rSource   "+rSource.getContents().size());
		
		//rSource.
		Resource rTarget = new XMIResourceImpl(URI.createFileURI(f.getAbsolutePath()+".xmi"));
		Resource rTarget2 = new XMIResourceImpl(URI.createFileURI(f.getAbsolutePath()+num+".xmi"));
		for (Resource r : new ArrayList<>(rs1.getResources())) {
			rTarget.getContents().addAll(new ArrayList<>(r.getContents()));
		}
		rTarget.save(null);
		if(resource instanceof EMFModelResource) {
			Resource ref = ((EMFModelResource) resource).getResource();
			rTarget2.getContents().addAll(ref.getContents());
			
		}
		rTarget2.save(null);
		
		System.out.println("size of rTarget   "+rTarget.getContents().size());
		System.out.println("size of rTarget 2  "+rTarget2.getContents().size());
		
		//indexer.getCompositeStateListener().info("size of Rsource    "+rSource.getContents().size());
		
		if (rTarget.isLoaded()) {
			rTarget.unload();
		}
		if (rTarget2.isLoaded()) {
			rTarget2.unload();
		}
		if (rSource.isLoaded()) {
			rSource.unload();
		}
		*/
		/**
		if (fileNode != null) {
			FileNode fn= new FileNode(fileNode);
			hawkre=  new LocalHawkResourceImpl(URI.createFileURI(f.getAbsolutePath()), indexer,
					true, Arrays.asList(fn.getRepositoryURL()), Arrays.asList(fn.getFilePath()));
			indexer.getCompositeStateListener().info("size of local hawk resource    "+hawkre.getContents().size());
			//hawkre.
			System.out.println("size of local hawk resource    "+hawkre.isLoaded()+"   "+hawkre.getContents().size());
			hawkre.load(null);
			System.out.println("size of local hawk resource    "+hawkre.isLoaded()+"   "+hawkre.getContents().size());
			
			if(hawkre.isLoaded()) {
				hawkre.unload();
			}
		}
		
		**/
		/*
	}
	*/
	
}
