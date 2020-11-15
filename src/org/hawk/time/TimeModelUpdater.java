package org.hawk.time;

import org.eclipse.hawk.core.graph.IGraphNode;
import org.eclipse.hawk.graph.updater.GraphModelInserter;
import org.eclipse.hawk.timeaware.graph.TimeAwareModelUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeModelUpdater extends TimeAwareModelUpdater {
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeModelUpdater.class);
	@Override
	public GraphModelInserter createInserter() {
		return new TimeModelInserter(indexer, this::createDeletionUtils, typeCache) {
			//@Override
			protected double calculateModelDeltaRatio(IGraphNode fileNode, boolean verbose) throws Exception {
				super.calculateModelDeltaRatio(fileNode, verbose);

				/*
				 * We want to always do a transactional update - batch update starts by removing
				 * every existing node, so we would lose track of the various versions of each
				 * model element.
				 */
				return 0;
			}
		};
	}
	@Override
	public String getHumanReadableName() {
		return "Time Model Updater";
	}
}
