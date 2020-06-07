package org.hawk.time;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.hawk.core.IConsole;
import org.eclipse.hawk.core.ICredentialsStore;
import org.eclipse.hawk.core.IFileImporter;
import org.eclipse.hawk.core.IModelResourceFactory;
import org.eclipse.hawk.core.IModelUpdater;
import org.eclipse.hawk.core.IVcsManager;
import org.eclipse.hawk.core.VcsCommitItem;
import org.eclipse.hawk.core.util.FileOperations;
import org.hawk.simulink.MatlabModelResourceFactory;
import org.eclipse.hawk.timeaware.graph.TimeAwareIndexer;

/*
 * *This class works exactly like org.hawk.timeaware indexer
 * The only exception is to ensure that the files are closed in Matlab
 * When there is any error in deleting temporary files
 * Hence this indexer should be used only with a simulink parser
 */
public class TimeIndexer extends TimeAwareIndexer{

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
	protected boolean synchroniseFiles(String revision, IVcsManager vcsManager, final Collection<VcsCommitItem> files) {
		final Set<VcsCommitItem> deleteditems = new HashSet<VcsCommitItem>();
		final Set<VcsCommitItem> interestingfiles = new HashSet<VcsCommitItem>();
		inspectChanges(files, deleteditems, interestingfiles);
		deletedFiles = deletedFiles + deleteditems.size();
		interestingFiles = interestingFiles + interestingfiles.size();

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
			
			String fName= (new File(monitorTempDir)).getName();
			IModelResourceFactory mrf = getModelParserFromFilename("test.slx");
			//System.out.println("delete temporary   "+fName +"   "+temp.getName());
			if(mrf instanceof MatlabModelResourceFactory) {
				//System.out.println("close all files");
				((MatlabModelResourceFactory)mrf).closeFiles();
				((MatlabModelResourceFactory)mrf).clear();
			} 
			
			console.printerrln("error in deleting temporary temporary local vcs files");
		}
		else {
			IModelResourceFactory mrf = getModelParserFromFilename("test.slx");
			System.out.println("file deltion successful   "+temp.getName());
			if(mrf instanceof MatlabModelResourceFactory) {
				//System.out.println("close all files");
				((MatlabModelResourceFactory)mrf).closeFiles();
				((MatlabModelResourceFactory)mrf).clear();
			} 
		}
		return updatersOK;
	}

}
