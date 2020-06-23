package org.hawk.time;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
import org.eclipse.hawk.core.graph.IGraphDatabase;
import org.eclipse.hawk.core.graph.IGraphNode;
import org.eclipse.hawk.core.model.IHawkModelResource;
import org.eclipse.hawk.emf.model.EMFModelResource;
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
	private IHawkModelResource resource;

	private Map<String, IGraphNode> nodes = new HashMap<>();
	private TypeCache typeCache;
	private Supplier<DeletionUtils> deletionUtils;
	private static int num=0;

	public TimeModelInserter(IModelIndexer hawk, Supplier<DeletionUtils> deletionUtils, TypeCache typeCache) {
		super(hawk, deletionUtils, typeCache);
		this.indexer = hawk;
		this.graph = indexer.getGraph();
		this.typeCache = typeCache;
		this.deletionUtils = deletionUtils;
		// TODO Auto-generated constructor stub
	}
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
		double res = super.calculateModelDeltaRatio(verbose);
		if (res<0) {
			test2();
		}
		//System.out.println("single modelta ratio callled  "+res);
		return res;
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
	}
	
}
