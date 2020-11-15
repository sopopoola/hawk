package org.hawk.versionaware.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.hawk.core.IModelIndexer;
import org.eclipse.hawk.core.IStateListener.HawkState;
import org.eclipse.hawk.core.graph.timeaware.ITimeAwareGraphDatabase;
import org.eclipse.hawk.core.query.InvalidQueryException;
import org.eclipse.hawk.core.query.QueryExecutionException;
import org.eclipse.hawk.epsilon.emc.EOLQueryEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimelineVersionEOL extends VersionEOLQueryEngine {
	private static final Logger LOGGER = LoggerFactory.getLogger(TimelineVersionEOL.class);
	private String defaultNamespaces;
	public TimelineVersionEOL() {
		super();
	}
	
	@Override
	public void setDefaultNamespaces(String defaultNamespaces) {
		this.defaultNamespaces = defaultNamespaces;
	}
	@Override
	public Object query(IModelIndexer m, String query, Map<String, Object> context)
			throws InvalidQueryException, QueryExecutionException
	{
		final HawkState currentState = m.getCompositeStateListener().getCurrentState();
		if (currentState != HawkState.RUNNING) {
			throw new QueryExecutionException(
					String.format("Cannot run the query, as the indexer is not in the RUNNING state: it is %s instead.",
							currentState));
		}
		if (!(m.getGraph() instanceof ITimeAwareGraphDatabase)) {
			throw new QueryExecutionException(getClass().getName() + " can only be used with time-aware databases");
		}
		final ITimeAwareGraphDatabase taDB = (ITimeAwareGraphDatabase) m.getGraph();

		if (defaultNamespaces != null) {
			if (context == null) {
				context = new HashMap<>();
			}
			if (!context.containsKey(PROPERTY_DEFAULTNAMESPACES)) {
				context.put(PROPERTY_DEFAULTNAMESPACES, defaultNamespaces);
			}
		}

		// Collect all relevant instants from the various type nodes
		final Collection<Long> instants = getAllInstants();
		

		// Compute query for each and every instant
		final List<Object> results = new ArrayList<>();
		try {
			int executed = 0;
			for (long instant : instants) {
				if (executed % 10 == 0) {
					LOGGER.info("Ran {}/{} instants", executed, instants.size());
				}
				executed++;

				// Do not allow two timeline queries to take place concurrently
				synchronized (taDB) {
					taDB.setTime(instant);

					final EOLQueryEngine q = new VersionEOLQueryEngine();
					final Object result = q.query(m, query, context);
					results.add(Arrays.asList(instant, result));
					System.out.println(executed+" "+instant+" "+result);
				}
			}
		} finally {
			synchronized (taDB) {
				taDB.setTime(0L);
			}
		}

		return results;
	}

	@Override
	public String getHumanReadableName() {
		return "Timeline Version EOL engine";
	}

}
