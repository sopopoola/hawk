package org.hawk.time;

import java.io.File;
import java.util.List;

import org.eclipse.hawk.core.IConsole;
import org.eclipse.hawk.core.ICredentialsStore;
import org.eclipse.hawk.core.IHawk;
import org.eclipse.hawk.timeaware.factory.TimeAwareHawkFactory;

public class TimeHawkFactory extends TimeAwareHawkFactory {
	@Override
	public IHawk create(String name, File storageFolder, String location, ICredentialsStore credStore, IConsole console,
			List<String> enabledPlugins) throws Exception {
		return new TimeHawk(name, storageFolder, credStore, console);
	}
	@Override
	public String getHumanReadableName() {
		return "Time local Hawk";
	}
}
