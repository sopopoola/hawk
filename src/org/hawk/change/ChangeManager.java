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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.hawk.core.IModelIndexer;
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
		private static final String MESSAGE_PROPERTY= "messages";

		private final ITimeAwareGraphNode node;

		public ChangeNode(ITimeAwareGraphNode n) {
			this.node = n;
		}

		public String getURI() {
			return node.getProperty(URI_PROPERTY) + "";
		}
		
		public ITimeAwareGraphNode getNode() {
			return node;
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
		public String getAdd() {
			final Object add = node.getProperty(ADD_PROPERTY);
			return add == null ? "0" : add.toString();
		}

		/**
		 * Stores the number of added elements associated with this revision.
		 */
		public void setAdd(int add) {
			String ad= add+"";
			node.setProperty(ADD_PROPERTY, ad);
		}
		/**
		 * Returns the commit message associated with this revision.
		 */
		public int getDelete() {
			final Object delete = node.getProperty(DELETE_PROPERTY);
			return delete == null ? 0 : (int)delete;
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
			return edit == null ? 0 : (int)edit;
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
			return move == null ? 0 : (int)move;
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
			return add == null ? 0 : (int)add;
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
			return delete == null ? 0 : (int)delete;
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
			return edit == null ? 0 : (int)edit;
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
			return edit == null ? "" : (String)edit;
		}
		/**
		 * Stores the commit message associated with this revision.
		 */
		public void setDiffSummary(String edit) {
			node.setProperty(DIFF_SUMMARY_PROPERTY, edit);
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
		this.idx = db.getOrCreateNodeIndex("_hawkVCSIndex");
		//db.createNode(props, label)
		//System.out.println("index  "+idx.);
	}

	/**
	 * Retrieves the {@link RepositoryNode} associated with a URI, creating
	 * it if it does not exist already.
	 */
	public ChangeNode getOrCreateChangeNode(String repoURI) {
		//String repoURI ="https://github.com/ktalke12/Matlab_MiP";
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
	public ChangeNode getOrCreateChangeNode(String uriFragment, String type) {
		//String repoURI ="https://github.com/ktalke12/Matlab_MiP";
		IGraphIterable<? extends IGraphNode> iNode = idx.get(URI_PROPERTY, uriFragment);
		final Map<String, Object> nodeMap = new HashMap<>();
		nodeMap.put("change", type);
		nodeMap.put("uriFragment", uriFragment);
		if (iNode.size() > 0) {
			return new ChangeNode((ITimeAwareGraphNode) iNode.getSingle());
		} else {
			final ITimeAwareGraphNode node = db.createNode(
				Collections.singletonMap(URI_PROPERTY, uriFragment), "_hawkRepo");
			idx.add(node, URI_PROPERTY, uriFragment);
			return new ChangeNode(node);
		}
	}
	public ChangeNode getOrCreateChangeNode(int p) {
		String repoURI ="_hawkChangder";
		ChangeNode result;
		//idx.
		IGraphIterable<? extends IGraphNode> iNode = idx.get(URI_PROPERTY, repoURI);
		//idx.
		if (iNode.size() > 0) {
			System.out.println("existingttt  node");
			result= new ChangeNode((ITimeAwareGraphNode) iNode.getSingle());
		} else {
			Map map= new HashMap <String,Object>();
			
			map.put("add", p);
			map.put(URI_PROPERTY, repoURI);
			//final ITimeAwareGraphNode node = db.createNode(
			//	Collections.singletonMap(URI_PROPERTY, repoURI), "_hawkChanger");
			final ITimeAwareGraphNode node = db.createNode(
					map, "_hawkChanger");
			//idx.add(node, URI_PROPERTY, repoURI);
			idx.add(node, map);
			//System.out.println("new node");
			result =new ChangeNode(node);
		}
		//result.setAdd(p);
		return result;
	}
	public ChangeNode getChangeNode( Long id) {
		IGraphNode nd = db.getNodeById(id);
		if (nd != null)
			return new ChangeNode((ITimeAwareGraphNode) nd);
		return null;
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
