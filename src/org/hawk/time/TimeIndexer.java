package org.hawk.time;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.hawk.core.IConsole;
import org.eclipse.hawk.core.ICredentialsStore;
import org.eclipse.hawk.core.IFileImporter;
import org.eclipse.hawk.core.IModelResourceFactory;
import org.eclipse.hawk.core.IModelUpdater;
import org.eclipse.hawk.core.IVcsManager;
import org.eclipse.hawk.core.VcsChangeType;
import org.eclipse.hawk.core.VcsCommit;
import org.eclipse.hawk.core.VcsCommitItem;
import org.eclipse.hawk.core.VcsRepositoryDelta;
import org.eclipse.hawk.core.graph.IGraphNode;
import org.eclipse.hawk.core.graph.IGraphTransaction;
import org.eclipse.hawk.core.graph.timeaware.ITimeAwareGraphDatabase;
import org.eclipse.hawk.core.model.IHawkModelResource;
import org.eclipse.hawk.core.runtime.BaseModelIndexer.DefaultFileImporter;
import org.eclipse.hawk.core.util.FileOperations;
import org.eclipse.hawk.emf.model.EMFModelResource;
import org.eclipse.hawk.graph.FileNode;
import org.hawk.change.ChangeManager;
import org.hawk.change.ChangeManager.ChangeNode;
import org.hawk.change.DiffChange;
import org.hawk.change.TestManager;
import org.hawk.change.TestManager.RepoNode;
import org.hawk.emfcompare.SimCompare;
import org.hawk.simulink.MatlabModelResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.hawk.timeaware.graph.TimeAwareIndexer;
import org.eclipse.hawk.timeaware.graph.VCSManagerIndex;
import org.eclipse.hawk.timeaware.graph.VCSManagerIndex.RepositoryNode;

/*
 * *This class works exactly like org.hawk.timeaware indexer
 * The only exception is to ensure that the files are closed in Matlab
 * When there is any error in deleting temporary files
 * Hence this indexer should be used only with a simulink parser
 */
public class TimeIndexer extends TimeAwareIndexer{
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeIndexer.class);
	Set<VcsCommitItem> deleteditem;
	Set<DiffChange> changes;
	private static int num =0;

	public TimeIndexer(String name, File parentFolder, ICredentialsStore credStore, IConsole c) {
		super(name, parentFolder, credStore, c);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void importFiles(IFileImporter importer, final Set<VcsCommitItem> changedItems, final Map<String, File> pathToImported) {
		int iImported = 0;

		for (VcsCommitItem s : changedItems) {
			final String commitPath = s.getPath();
			//System.out.println("committed paths  "+commitPath);
			
			if (VERBOSE) {
				console.println("-->" + commitPath + " HAS CHANGED (" + s.getChangeType()
						+ "), PROPAGATING CHANGES");
			}
	
			if (!pathToImported.containsKey(commitPath)) {
				final File imported = importer.importFile(commitPath);
				pathToImported.put(commitPath, imported);
			}

			++iImported;
			if (iImported % 10 == 0) {
				stateListener.info(String.format("Imported %d/%d files from %s",
					iImported, changedItems.size(), s.getCommit().getDelta().getManager().getLocation()));
			}
		}
	}
	
	@Override
	protected boolean synchronise(IVcsManager vcsManager) {
		if (!(graph instanceof ITimeAwareGraphDatabase)) {
			LOGGER.error("This indexer requires a time-aware backend, aborting");
			return false;
		}
		ITimeAwareGraphDatabase taGraph = (ITimeAwareGraphDatabase)graph;

		String lastRev;
		try {
			taGraph.setTime(0);
			lastRev = getLastIndexedRevision(vcsManager);
		} catch (Exception e) {
			LOGGER.error("Could not fetch the last indexed revision", e);
			return false;
		}

		try {
			final String currentRevision = vcsManager.getCurrentRevision();
			if (!currentRevision.equals(lastRev)) {
				latestUpdateFoundChanges = true;

				VcsRepositoryDelta delta = vcsManager.getDelta(lastRev, currentRevision);
				for (VcsCommit commit : delta.getCommits()) {
					// store information about changes to the models
					changes = new HashSet<DiffChange>();
					
					// Commits might be only milliseconds apart in automated processes
					final Instant instant = commit.getJavaDate().toInstant();
					final long epochMillis = instant.getEpochSecond() * 1000 + instant.getNano() / 1_000_000;
					
					//get information about deleted files
					//deleteditem = new HashSet<VcsCommitItem>();

					// Do not allow anyone else to change the time while we are manipulating the graph
					synchronized(taGraph) {
						taGraph.setTime(epochMillis);

						/*
						 * TODO: allow for fixing unresolved proxies in previous versions? Might make
						 * sense if we forgot to add a metamodel in a previous version.
						 */
						final boolean successfulRevision = synchroniseFiles(commit.getRevision(), vcsManager, commit.getItems());
						if (successfulRevision) {
							console.println(String.format("Indexed successfully revision %s (timepoint %d) of %s",
								commit.getRevision(), epochMillis, commit.getDelta().getManager().getLocation()));
						} else {
							console.printerrln(String.format("Failed to index revision %s (timepoint %d) of %s",
								commit.getRevision(), epochMillis, commit.getDelta().getManager().getLocation()));
							return false;
						}
						
						//setIndexedChanges(vcsManager,changes,commit);
						setIndexedRevision(vcsManager, commit);
						
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to synchronise repository " + vcsManager.getLocation(), e);
			return false;
		} finally {
			synchronized (taGraph) {
				taGraph.setTime(0);
			}
		}

		return true;
	}
	
	@Override
	protected boolean internalSynchronise(final String currentRevision, final IVcsManager m,
			final IModelUpdater u, final Set<VcsCommitItem> deletedItems, final Set<VcsCommitItem> interestingfiles,
			final String monitorTempDir) {
		boolean success = true;

		// enters transaction mode!
		Set<VcsCommitItem> currReposChangedItems = u.compareWithLocalFiles(interestingfiles);

		// metadata about synchronise
		final int totalFiles = currReposChangedItems.size();
		currchangeditems = currchangeditems + totalFiles;

		// create temp files with changed repos files
		final Map<String, File> pathToImported = new HashMap<>();
		final IFileImporter importer = new DefaultFileImporter(m, currentRevision, new File(monitorTempDir));
		importFiles(importer, currReposChangedItems, pathToImported);

		// delete all removed files
		success = deleteRemovedModels(success, u, deletedItems);

		stateListener.info("Updating models to the new version...");

		// prepare for mass inserts if needed
		graph.enterBatchMode();

		final boolean fileCountProgress = totalFiles > FILECOUNT_PROGRESS_THRESHOLD;
		final long millisSinceStart = System.currentTimeMillis();
		int totalProcessedFiles = 0, filesProcessedSinceLastPrint = 0;
		long millisSinceLastPrint = millisSinceStart;

		for (VcsCommitItem v : currReposChangedItems) {
			try {
				// Place before the actual update so we print the 0/X message as well
				if (fileCountProgress && (totalProcessedFiles == 0 && filesProcessedSinceLastPrint == 0
						|| filesProcessedSinceLastPrint == FILECOUNT_PROGRESS_THRESHOLD)) {
					totalProcessedFiles += filesProcessedSinceLastPrint;

					final long millisPrint = System.currentTimeMillis();
					stateListener.info(String.format("Processed %d/%d files in repo %s (%s sec, %s sec total)",
							totalProcessedFiles, totalFiles, m.getLocation(),
							(millisPrint - millisSinceLastPrint) / 1000, (millisPrint - millisSinceStart) / 1000));

					filesProcessedSinceLastPrint = 0;
					millisSinceLastPrint = millisPrint;
				}

				IHawkModelResource r = null;
				if (u.caresAboutResources()) {
					final File file = pathToImported.get(v.getPath());
					if (file == null || !file.exists()) {
						console.printerrln("warning, cannot find file: " + file + ", ignoring changes");
					} else {
						IModelResourceFactory mrf = getModelParserFromFilename(file.getName().toLowerCase());
						if (mrf.canParse(file)) {
							r = mrf.parse(importer, file);
						}
					}
				}
				//add changes
				//changes.add(getModel(v,r));
				
				
				success = u.updateStore(v, r) && success;
				
				
				

				if (r != null) {
					if (!isSyncMetricsEnabled) {
						r.unload();
					} else {
						fileToResourceMap.put(v, r);
					}
					loadedResources++;
				}

				filesProcessedSinceLastPrint++;

			} catch (Exception e) {
				console.printerrln("updater: " + u + "failed to update store");
				console.printerrln(e);
				success = false;
			}
		}

		// Print the final message
		if (fileCountProgress) {
			totalProcessedFiles += filesProcessedSinceLastPrint;
			final long millisPrint = System.currentTimeMillis();
			stateListener.info(String.format("Processed %d/%d files in repo %s (%s sec, %s sec total)",
					totalProcessedFiles, totalFiles, m.getLocation(), (millisPrint - millisSinceLastPrint) / 1000,
					(millisPrint - millisSinceStart) / 1000));
		}

		stateListener.info("Updating proxies...");

		// update proxies
		u.updateProxies();

		// leave batch mode
		graph.exitBatchMode();

		stateListener.info("Updated proxies.");

		return success;
	}
	
	@Override
	protected boolean synchroniseFiles(String revision, IVcsManager vcsManager, final Collection<VcsCommitItem> files) {
		final Set<VcsCommitItem> deleteditems = new HashSet<VcsCommitItem>();
		final Set<VcsCommitItem> interestingfiles = new HashSet<VcsCommitItem>();
		inspectChanges(files, deleteditems, interestingfiles);
		deletedFiles = deletedFiles + deleteditems.size();
		interestingFiles = interestingFiles + interestingfiles.size();
		
		deleteditem=new HashSet<VcsCommitItem>(deleteditems);
		
		//changes.addAll(getDeletedModels(deleteditem));
		

		final String monitorTempDir = graph.getTempDir();
		File temp = new File(monitorTempDir);
		temp.mkdir();

		// for each registered updater
		boolean updatersOK = true;
		for (IModelUpdater updater : getModelUpdaters()) {
			updatersOK = updatersOK && internalSynchronise(revision, vcsManager, updater, deleteditems, interestingfiles, monitorTempDir);
		}

		// delete temporary files
		if (!FileOperations.deleteFiles(new File(monitorTempDir), true)) {
			/*
			String fName= (new File(monitorTempDir)).getName();
			IModelResourceFactory mrf = getModelParserFromFilename("test.slx");
			//System.out.println("delete temporary   "+fName +"   "+temp.getName());
			if(mrf instanceof MatlabModelResourceFactory) {
				//System.out.println("close all files");
				((MatlabModelResourceFactory)mrf).closeFiles();
				((MatlabModelResourceFactory)mrf).clear();
			} 
			*/
			console.printerrln("error in deleting temporary temporary local vcs files");
		}
		else {
			/*
			IModelResourceFactory mrf = getModelParserFromFilename("test.slx");
			System.out.println("file deltion successful   "+temp.getName());
			if(mrf instanceof MatlabModelResourceFactory) {
				//System.out.println("close all files");
				((MatlabModelResourceFactory)mrf).closeFiles();
				((MatlabModelResourceFactory)mrf).clear();
			} 
			*/
		}
		return updatersOK;
	}
	
	public Set<DiffChange> getDeletedModels(Set<VcsCommitItem> deleteditems) {
		IGraphNode fileNode;
		Set<DiffChange> result= new HashSet<DiffChange>();
		try {
		File folder = new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/temp/"+getName());
		if(!folder.exists())
			folder.mkdirs();
		
		PrintWriter writer;
		for (VcsCommitItem c: deleteditems) {
			fileNode = new TimeUtil().getFileNodeFromVCSCommitItem(graph, c);
			SimCompare compare = new SimCompare();
			
			if (fileNode != null) {
				File f=new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/temp/"+getName()+"/testd"+(num++)+".localhawkmodel");
				FileNode fn= new FileNode(fileNode);
				writer = new PrintWriter(f, "UTF-8");
				writer.print("repos="+fn.getRepositoryURL()+"\r\n");
				writer.print("files="+fn.getFilePath()+"\r\n");
				writer.close();
				
				ResourceSet rs1 = new ResourceSetImpl();
				Resource rSource = rs1.createResource(URI.createFileURI(f.getPath()));
				//System.out.println(rSource.);
				rSource.load(null);
				
				Resource rTarget = new XMIResourceImpl(URI.createFileURI(f.getAbsolutePath()+".xmi"));
				for (Resource r : new ArrayList<>(rs1.getResources())) {
					rTarget.getContents().addAll(new ArrayList<>(r.getContents()));
				}
				rTarget.save(null);
				result.add(compare.getSummary(new File(f.getAbsolutePath()+".xmi"), "delete"));
				
			}
			
		}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return result;
		
	}
	public DiffChange getModel(VcsCommitItem v, IHawkModelResource u) {
		IGraphNode fileNode;
		DiffChange result = null;
		try {
			File folder = new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/temp/"+getName());
			if(!folder.exists())
				folder.mkdirs();
		SimCompare compare = new SimCompare();
		fileNode = new TimeUtil().getFileNodeFromVCSCommitItem(graph, v);
		File f=new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/temp/"+getName()+"/test"+num+".localhawkmodel");
		File f1=new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/temp/"+getName()+"/testA"+num+".xmi");
		File f2=new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/temp/"+getName()+"/testB"+num+".xmi");
		
		num++;
		
		if(!f.exists()) {
			f.createNewFile();
		}
		Resource rTarget1 = new XMIResourceImpl(URI.createFileURI(f1.getAbsolutePath()));
		//Resource
		
		if(u instanceof EMFModelResource) {
			Resource ref = ((EMFModelResource) u).getResource();
			//rTarget2.getContents().addAll(new ArrayList<>(ref.getContents()));
			Copier copier = new Copier();
			 Collection<EObject> results = copier.copyAll(ref.getContents());
			  copier.copyReferences();
			  rTarget1.getContents().addAll(new ArrayList<>(results));
			
		}
		//rTarget2.save(null);
		
		rTarget1.save(null);
		System.out.println("size of rTarget   "+rTarget1.getContents().size());
		if (fileNode != null) {
			FileNode fn= new FileNode(fileNode);
			PrintWriter writer = new PrintWriter(f, "UTF-8");
			writer.println(getName());
			writer.print("repos="+fn.getRepositoryURL()+"\r\n");
			writer.print("files="+fn.getFilePath()+"\r\n");
			writer.close();
			ResourceSet rs1 = new ResourceSetImpl();
			//resource.
			Resource rSource = rs1.createResource(URI.createFileURI(f.getPath()));
			//System.out.println(rSource.);
			rSource.load(null);
			System.out.println("size of rSource   "+rSource.getContents().size());
			
			//rSource.
			Resource rTarget = new XMIResourceImpl(URI.createFileURI(f2.getAbsolutePath()));
			for (Resource r : new ArrayList<>(rs1.getResources())) {
				rTarget.getContents().addAll(new ArrayList<>(r.getContents()));
			}
			rTarget.save(null);
			//rTarget.getAllContents();
			//fn.getModelElements();
			if (rTarget.isLoaded()) {
				rTarget.unload();
			}
			
			if (rSource.isLoaded()) {
				rSource.unload();
			}
			result= compare.getSummary(f1, f2);
			
		}
		else {
			result= compare.getSummary(f1, "add");
		}
		
		
		if (rTarget1.isLoaded()) {
			rTarget1.unload();
		}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public void setIndexedChanges() throws Exception {
		try (IGraphTransaction tx = graph.beginTransaction()) {
			TestManager manager = new TestManager((ITimeAwareGraphDatabase) graph);
			//System.out.println("initial timing   "+((ITimeAwareGraphDatabase) graph).getTime());
			RepoNode repoNode = manager.getOrCreateRepositoryNode("https://github.com/ktalke12/Matlab_MiP");
			repoNode.setMessage("this is a test");
			//repoNode.setDelete(del);
			//repoNode.setMove(mov);
			//repoNode.setEdit(chn);
			//repoNode.setModelAdd(madd);
			//repoNode.setModelDelete(mdel);
			//repoNode.setModelEdit(mmod);
			tx.success();
		}
	}
	public void setIndexedChanges(IVcsManager vcsManager,Collection<DiffChange> change, VcsCommit commit) throws Exception {
		//if(change.isEmpty())
		//	return;
		//System.out.println("repourl  "+ vcsManager.getLocation());
		int add=0, del=0, mov=0, chn=0, madd=0,mdel=0,mmod=0;

		System.out.println("this is a new indexing change");
		for(DiffChange d: change) {
			add=add+d.getAdd();
			del=del+d.getDelete();
			chn=chn+d.getChange();
			mov=mov+d.getMove();
			if(d.getType().equals("add"))
				madd++;
			else if (d.getType().equals("delete"))
				mdel++;
			else
				mmod++;
			System.out.println("new diff");
			if(d==null)
				continue;
			System.out.println("add  "+ d.getAdd() );
			System.out.println("delete  "+d.getDelete());
			System.out.println("change  "+d.getChange());
			System.out.println("move  "+d.getMove());
			System.out.println("type  "+d.getType());
		}
		File f=new File("C:/Users/student/Desktop/eclipse/runtime-EclipseApplication/temp/"+getName()+"/summary"+(num++)+".txt");
		
		PrintWriter write= new PrintWriter(f);
		write.println("Add  "+ add);
		write.println("Delete  "+ del);
		write.println("Mov  "+ mov);
		write.println("Change  "+ chn);
		write.println("Model Add  "+ madd);
		write.println("Model Delete  "+ mdel);
		write.println("Model Modify  "+ mmod);
		write.println("message  "+ commit.getMessage());
		write.println("date  "+ commit.getJavaDate());
		write.close();
		//ChangeNode repNode = new ChangeManager((ITimeAwareGraphDatabase) graph).getOrCreateChangeNode(add);
		//System.out.println("confirm savess  "+ repNode.getMessage() + " id  "+ repNode.getId());
		
		try (IGraphTransaction tx = graph.beginTransaction()) {
			ChangeManager manager = new ChangeManager((ITimeAwareGraphDatabase) graph);
			ChangeNode repoNode = manager.getOrCreateChangeNode(vcsManager.getLocation());
			//repoNode.setMessage("this is a test");
			repoNode.setAdd(add);
			repoNode.setDelete(del);
			repoNode.setMove(mov);
			repoNode.setEdit(chn);
			repoNode.setModelAdd(madd);
			repoNode.setModelDelete(mdel);
			repoNode.setModelEdit(mmod);
			tx.success();
		}
		
		//int add,de
		
	}

}
