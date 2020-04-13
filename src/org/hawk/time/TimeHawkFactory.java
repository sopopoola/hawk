package org.hawk.time;

import java.io.File;
import java.util.List;

import org.hawk.core.IConsole;
import org.hawk.core.ICredentialsStore;
import org.hawk.core.IHawk;
import org.hawk.timeaware.factory.TimeAwareHawkFactory;

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
