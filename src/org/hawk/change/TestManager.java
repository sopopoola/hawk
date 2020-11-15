package org.hawk.change;

import java.util.Collections;

import org.eclipse.hawk.core.graph.IGraphIterable;
import org.eclipse.hawk.core.graph.IGraphNode;
import org.eclipse.hawk.core.graph.timeaware.ITimeAwareGraphDatabase;
import org.eclipse.hawk.core.graph.timeaware.ITimeAwareGraphNode;
import org.eclipse.hawk.core.graph.timeaware.ITimeAwareGraphNodeIndex;

/**
 * Keeps track of information in the graph about the various VCS. This class
 * does not handle transactions: users are expected to do it.
 */
public class TestManager {
	private static final String URI_PROPERTY = "uri";

	private final ITimeAwareGraphNodeIndex idx;
	private final ITimeAwareGraphDatabase db;

	/**
	 * Type-safe wrapper for the node we keep in the graph about a repository.
	 */
	public class RepoNode {
		private static final String LASTREV_PROPERTY = "lastRevision";
		private static final String MESSAGE_PROPERTY = "message";

		private final ITimeAwareGraphNode node;

		public RepoNode(ITimeAwareGraphNode n) {
			this.node = n;
		}

		public String getURI() {
			return node.getProperty(URI_PROPERTY) + "";
		}

		/**
		 * Returns the revision indexed for this VCS at its current timepoint, or
		 * <code>null</code> if it has not been indexed yet.
		 */
		public String getRevision() {
			final Object lastRev = node.getProperty(LASTREV_PROPERTY);
			if (lastRev == null) {
				return null;
			} else {
				return lastRev.toString();
			}
		}

		/**
		 * Changes the last revision indexed for this VCS.
		 */
		public void setRevision(String lastRev) {
			node.setProperty(LASTREV_PROPERTY, lastRev);
		}

		/**
		 * Returns the latest version of this repository node.
		 *
		 * @throws Exception Error while fetching the latest version.
		 */
		public RepoNode getLatest() throws Exception {
			return new RepoNode(node.getLatest());
		}

		/**
		 * Returns the node at a different point in time.
		 */
		public RepoNode travelInTime(long instant) throws Exception {
			return new RepoNode(node.travelInTime(instant));
		}

		/**
		 * Returns the commit message associated with this revision.
		 */
		public String getMessage() {
			final Object message = node.getProperty(MESSAGE_PROPERTY);
			return message == null ? null : message.toString();
		}

		/**
		 * Stores the commit message associated with this revision.
		 */
		public void setMessage(String message) {
			node.setProperty(MESSAGE_PROPERTY, message);
		}

		/**
		 * Returns the underlying node ID.
		 */
		public Object getId() {
			return node.getId();
		}
		public ITimeAwareGraphNode getNode() {
			return node;
		}
		
	}

	/**
	 * Creates a new instance. Retrieves the existing node index in the graph,
	 * or creates a new one if it does not exist. 
	 */
	public TestManager(ITimeAwareGraphDatabase db) {
		this.db = db;
		this.idx = db.getOrCreateNodeIndex("_hawkVCSIndexer");
	}

	/**
	 * Retrieves the {@link RepositoryNode} associated with a URI, creating
	 * it if it does not exist already.
	 */
	public RepoNode getOrCreateRepositoryNode(String repoURI) {
		IGraphIterable<? extends IGraphNode> iNode = idx.get(URI_PROPERTY, repoURI);
		if (iNode.size() > 0) {
			System.out.println("now existing");
			return new RepoNode((ITimeAwareGraphNode) iNode.getSingle());
		} else {
			System.out.println("not exists");
			final ITimeAwareGraphNode node = db.createNode(
				Collections.singletonMap(URI_PROPERTY, repoURI), "_hawkRepo");
			idx.add(node, URI_PROPERTY, repoURI);
			return new RepoNode(node);
		}
	}

	/**
	 * Deletes the {@link RepositoryNode} associated with a URI, if it exists.
	 */
	public void removeRepositoryNode(String repoURI) {
		IGraphIterable<? extends IGraphNode> iNode = idx.get(URI_PROPERTY, repoURI);
		if (iNode.size() > 0) {
			IGraphNode node = iNode.getSingle();
			idx.remove(node);
			node.delete();
		}
	}

}
