package org.hawk.change;

import java.util.Collection;

/*******************************************************************************
 * Copyright (c) 2018 Aston University.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 3.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-3.0
 *
 * Contributors:
 *     Antonio Garcia-Dominguez - initial API and implementation
 ******************************************************************************/

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
public class ChangeManager {
	private static final String URI_PROPERTY = "uri";

	private final ITimeAwareGraphNodeIndex idx;
	private final ITimeAwareGraphDatabase db;

	/**
	 * Type-safe wrapper for the node we keep in the graph about a change set.
	 */
	public class ChangeNode {
		private static final String LASTREV_PROPERTY = "lastRevision";
		private static final String MODEL_ADD_PROPERTY = "modelAdd";
		private static final String MODEL_DELETE_PROPERTY = "modelDelete";
		private static final String MODEL_MODIFIED_PROPERTY = "modelModified";
		private static final String ADD_PROPERTY = "add";
		private static final String DELETE_PROPERTY = "delete";
		private static final String EDIT_PROPERTY = "edit";
		private static final String MOVE_PROPERTY = "move";
		private static final String DIFF_SUMMARY_PROPERTY = "diffSummary";
		private static final String DIFF_PROPERTY = "diff";

		private final ITimeAwareGraphNode node;

		public ChangeNode(ITimeAwareGraphNode n) {
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
		public ChangeNode getLatest() throws Exception {
			return new ChangeNode(node.getLatest());
		}

		/**
		 * Returns the node at a different point in time.
		 */
		public ChangeNode travelInTime(long instant) throws Exception {
			return new ChangeNode(node.travelInTime(instant));
		}

		
		/**
		 * Returns the number of added elements associated with this revision.
		 */
		public int getAdd() {
			final Object add = node.getProperty(ADD_PROPERTY);
			return add == null ? null : (int)add;
		}

		/**
		 * Stores the number of added elements associated with this revision.
		 */
		public void setAdd(int add) {
			node.setProperty(ADD_PROPERTY, add);
		}
		/**
		 * Returns the commit message associated with this revision.
		 */
		public int getDelete() {
			final Object delete = node.getProperty(DELETE_PROPERTY);
			return delete == null ? null : (int)delete;
		}

		/**
		 * Stores the commit message associated with this revision.
		 */
		public void setDelete(int delete) {
			node.setProperty(DELETE_PROPERTY, delete);
		}
		/**
		 * Returns the commit message associated with this revision.
		 */
		public int getEdit() {
			final Object edit = node.getProperty(EDIT_PROPERTY);
			return edit == null ? null : (int)edit;
		}

		/**
		 * Stores the commit message associated with this revision.
		 */
		public void setEdit(int edit) {
			node.setProperty(EDIT_PROPERTY, edit);
		}
		/**
		 * Returns the commit message associated with this revision.
		 */
		public int getMove() {
			final Object move = node.getProperty(MOVE_PROPERTY);
			return move == null ? null : (int)move;
		}

		/**
		 * Stores the commit message associated with this revision.
		 */
		public void setMove(int move) {
			node.setProperty(MOVE_PROPERTY, move);
		}
		
		/**
		 * Returns the number of added elements associated with this revision.
		 */
		public int getModelAdd() {
			final Object add = node.getProperty(MODEL_ADD_PROPERTY);
			return add == null ? null : (int)add;
		}

		/**
		 * Stores the number of added elements associated with this revision.
		 */
		public void setModelAdd(int add) {
			node.setProperty(MODEL_ADD_PROPERTY, add);
		}
		/**
		 * Returns the commit message associated with this revision.
		 */
		public int getModelDelete() {
			final Object delete = node.getProperty(MODEL_DELETE_PROPERTY);
			return delete == null ? null : (int)delete;
		}

		/**
		 * Stores the commit message associated with this revision.
		 */
		public void setModelDelete(int delete) {
			node.setProperty(MODEL_DELETE_PROPERTY, delete);
		}
		/**
		 * Returns the commit message associated with this revision.
		 */
		public int getModelEdit() {
			final Object edit = node.getProperty(MODEL_MODIFIED_PROPERTY);
			return edit == null ? null : (int)edit;
		}

		/**
		 * Stores the commit message associated with this revision.
		 */
		public void setModelEdit(int edit) {
			node.setProperty(MODEL_MODIFIED_PROPERTY, edit);
		}
		/**
		 * Returns the commit message associated with this revision.
		 */
		public String getDiffSummary() {
			final Object edit = node.getProperty(DIFF_SUMMARY_PROPERTY);
			return edit == null ? null : (String)edit;
		}
		/**
		 * Stores the commit message associated with this revision.
		 */
		public void setDiffSummary(String edit) {
			node.setProperty(DIFF_SUMMARY_PROPERTY, edit);
		}
		/**
		 * Stores the commit message associated with this revision.
		 */
		public void setDiff(Collection edit) {
			node.setProperty(DIFF_PROPERTY, edit);
		}
		/**
		 * Returns the commit message associated with this revision.
		 */
		public Collection getDiff() {
			final Object edit = node.getProperty(DIFF_PROPERTY);
			return edit == null ? null : (Collection)edit;
		}

		
		/**
		 * Returns the underlying node ID.
		 */
		public Object getId() {
			return node.getId();
		}
		
	}

	/**
	 * Creates a new instance. Retrieves the existing node index in the graph,
	 * or creates a new one if it does not exist. 
	 */
	public ChangeManager(ITimeAwareGraphDatabase db) {
		this.db = db;
		this.idx = db.getOrCreateNodeIndex("_hawkChangeIndex");
	}

	/**
	 * Retrieves the {@link RepositoryNode} associated with a URI, creating
	 * it if it does not exist already.
	 */
	public ChangeNode getOrCreateChangeNode(String repoURI) {
		IGraphIterable<? extends IGraphNode> iNode = idx.get(URI_PROPERTY, repoURI);
		if (iNode.size() > 0) {
			return new ChangeNode((ITimeAwareGraphNode) iNode.getSingle());
		} else {
			final ITimeAwareGraphNode node = db.createNode(
				Collections.singletonMap(URI_PROPERTY, repoURI), "_hawkRepo");
			idx.add(node, URI_PROPERTY, repoURI);
			return new ChangeNode(node);
		}
	}

	/**
	 * Deletes the {@link RepositoryNode} associated with a URI, if it exists.
	 */
	public void removeChangeNode(String repoURI) {
		IGraphIterable<? extends IGraphNode> iNode = idx.get(URI_PROPERTY, repoURI);
		if (iNode.size() > 0) {
			IGraphNode node = iNode.getSingle();
			idx.remove(node);
			node.delete();
		}
	}

}
