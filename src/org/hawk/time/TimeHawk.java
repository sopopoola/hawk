package org.hawk.time;

import java.io.File;

import org.eclipse.hawk.core.IConsole;
import org.eclipse.hawk.core.ICredentialsStore;
import org.eclipse.hawk.core.IHawk;
import org.eclipse.hawk.core.IModelIndexer;

public class TimeHawk implements IHawk {
	private File location;
	private IModelIndexer indexer;
	private String dbType;
	public TimeHawk(String name, File storageFolder, ICredentialsStore credStore, IConsole c) throws Exception {
		location = storageFolder;
		indexer = new TimeIndexer(name, location, credStore, c);
	}
	@Override
	public IModelIndexer getModelIndexer() {
		return indexer;
	}

	@Override
	public String getDatabaseType() {
		return dbType;
	}

	@Override
	public void setDatabaseType(String dbType) {
		this.dbType = dbType;
	}

	@Override
	public boolean exists() {
		return location.exists();
	}

}
