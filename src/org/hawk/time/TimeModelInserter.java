package org.hawk.time;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.hawk.core.IModelIndexer;
import org.eclipse.hawk.core.VcsCommitItem;
import org.eclipse.hawk.core.graph.IGraphDatabase;
import org.eclipse.hawk.core.graph.IGraphNode;
import org.eclipse.hawk.core.model.IHawkModelResource;
import org.eclipse.hawk.emfresource.impl.LocalHawkResourceImpl;
import org.eclipse.hawk.graph.FileNode;
import org.eclipse.hawk.graph.updater.DeletionUtils;
import org.eclipse.hawk.graph.updater.GraphModelBatchInjector;
import org.eclipse.hawk.graph.updater.GraphModelInserter;
import org.eclipse.hawk.graph.updater.TypeCache;

public class TimeModelInserter extends GraphModelInserter{
	private IModelIndexer indexer;
	private IGraphDatabase graph;
	private GraphModelBatchInjector inj;
	private VcsCommitItem commitItem;

	private Map<String, IGraphNode> nodes = new HashMap<>();
	private TypeCache typeCache;
	private Supplier<DeletionUtils> deletionUtils;

	public TimeModelInserter(IModelIndexer hawk, Supplier<DeletionUtils> deletionUtils, TypeCache typeCache) {
		super(hawk, deletionUtils, typeCache);
		this.indexer = hawk;
		this.graph = indexer.getGraph();
		this.typeCache = typeCache;
		this.deletionUtils = deletionUtils;
		// TODO Auto-generated constructor stub
	}
	public boolean run(IHawkModelResource res, VcsCommitItem s, final boolean verbose) throws Exception {
		boolean result= super.run(res, s, verbose);
		test();
		return result;
	}
	public void test() throws Exception {
		ResourceSet rs1 = new ResourceSetImpl();
		Resource rSource = null;
		File f=new File("test.emf");
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
		Resource rTarget = new XMIResourceImpl(URI.createFileURI(f.getAbsolutePath()+".xmi"));
		for (Resource r : new ArrayList<>(rs1.getResources())) {
			rTarget.getContents().addAll(new ArrayList<>(r.getContents()));
		}
		System.out.println("size of rSource   "+rSource.getContents().size());
		rTarget.save(null);
		if (rTarget.isLoaded()) {
			rTarget.unload();
		}
		if (rSource.isLoaded()) {
			rSource.unload();
		}
		if (fileNode != null) {
			FileNode fn= new FileNode(fileNode);
			hawkre=  new LocalHawkResourceImpl(URI.createFileURI(f.getAbsolutePath()), indexer,
					true, Arrays.asList(fn.getRepositoryURL()), Arrays.asList(fn.getFilePath()));
			System.out.println("size of local hawk resource    "+hawkre.getContents().size());
		}
	}
}
