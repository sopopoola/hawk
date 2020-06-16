package org.hawk.time;

import org.eclipse.hawk.core.VcsCommitItem;
import org.eclipse.hawk.core.graph.IGraphDatabase;
import org.eclipse.hawk.core.graph.IGraphNode;
import org.eclipse.hawk.graph.updater.Utils;

public class TimeUtil extends Utils{
	public IGraphNode getFileNodeFromVCSCommitItem(IGraphDatabase graph, VcsCommitItem s) {
		return super.getFileNodeFromVCSCommitItem(graph, s);
		
	}
}
