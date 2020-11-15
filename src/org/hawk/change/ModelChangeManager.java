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
public class ModelChangeManager {
	private static final String URI_PROPERTY = "uri";

	private final ITimeAwareGraphNodeIndex idx;
	private final ITimeAwareGraphDatabase db;

	/**
	 * Type-safe wrapper for the node we keep in the graph about a change set.
	 */
	public class ModelChangeNode {
		private static final String LASTREV_PROPERTY = "lastRevision";
		private static final String MESSAGE_PROPERTY= "messages";
		private static final String CHANGE_TYPE= "changeType";
		private static final String MODEL_TYPE= "modelType";
		private static final String PREVIOUS_VALUE= "previous";
		private static final String NEW_VALUE= "newValue";
		private static final String Id= "uriFragment";
		

		private final ITimeAwareGraphNode node;

		public ModelChangeNode(ITimeAwareGraphNode n) {
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
		public ModelChangeNode getLatest() throws Exception {
			return new ModelChangeNode(node.getLatest());
		}

		/**
		 * Returns the node at a different point in time.
		 */
		public ModelChangeNode travelInTime(long instant) throws Exception {
			return new ModelChangeNode(node.travelInTime(instant));
		}

		/**
		 * Returns the commit message associated with this revision.
		 */
		public String getChangeType() {
			final Object edit = node.getProperty(CHANGE_TYPE);
			return edit == null ? "" : (String)edit;
		}
		/**
		 * Stores the commit message associated with this revision.
		 */
		public void setChangeType(String edit) {
			node.setProperty(CHANGE_TYPE, edit);
		}
		
		/**
		 * Returns the commit message associated with this revision.
		 */
		public String getModelType() {
			final Object edit = node.getProperty(MODEL_TYPE);
			return edit == null ? "" : (String)edit;
		}
		
		/**
		 * Stores the commit message associated with this revision.
		 */
		public void setModelType(String edit) {
			node.setProperty(MODEL_TYPE, edit);
		}
		/**
		 * Returns the commit message associated with this revision.
		 */
		public String getPrevious() {
			final Object edit = node.getProperty(PREVIOUS_VALUE);
			return edit == null ? "" : (String)edit;
		}
		
		/**
		 * Stores the commit message associated with this revision.
		 */
		public void setPrevious(String edit) {
			node.setProperty(PREVIOUS_VALUE, edit);
		}
		/**
		 * Returns the commit message associated with this revision.
		 */
		public String getCurrent() {
			final Object edit = node.getProperty(NEW_VALUE);
			return edit == null ? "" : (String)edit;
		}
		
		/**
		 * Stores the commit message associated with this revision.
		 */
		public void setURIFragment(String uri) {
			node.setProperty(Id, uri);
		}
		public String getURIFragment() {
			final Object edit = node.getProperty(Id);
			return edit == null ? "" : (String)edit;
		}
		
		/**
		 * Stores the commit message associated with this revision.
		 */
		public void setCurrent(String edit) {
			node.setProperty(NEW_VALUE, edit);
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
	public ModelChangeManager(ITimeAwareGraphDatabase db) {
		this.db = db;
		this.idx = db.getOrCreateNodeIndex("_hawkVCSIndex");
		//db.createNode(props, label)
		//System.out.println("index  "+idx.);
	}

	/**
	 * Retrieves the {@link RepositoryNode} associated with a URI, creating
	 * it if it does not exist already.
	 */
	public ModelChangeNode getOrCreateChangeNode(String repoURI) {
		//String repoURI ="https://github.com/ktalke12/Matlab_MiP";
		IGraphIterable<? extends IGraphNode> iNode = idx.get(URI_PROPERTY, repoURI);
		if (iNode.size() > 0) {
			System.out.println("new node for model change");
			return new ModelChangeNode((ITimeAwareGraphNode) iNode.getSingle());
		} else {
			System.out.println("existing node for model change");
			final ITimeAwareGraphNode node = db.createNode(
				Collections.singletonMap(URI_PROPERTY, repoURI), "_hawkRepo");
			idx.add(node, URI_PROPERTY, repoURI);
			return new ModelChangeNode(node);
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
