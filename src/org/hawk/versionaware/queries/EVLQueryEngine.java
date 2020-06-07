package org.hawk.versionaware.queries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

//import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.evl.EvlModule;
import org.eclipse.epsilon.evl.IEvlModule;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.execute.operations.contributors.OperationContributorRegistry;
//import org.eclipse.epsilon.epl.EplModule;
import org.eclipse.hawk.core.IModelIndexer;
import org.eclipse.hawk.core.IStateListener.HawkState;
import org.eclipse.hawk.core.graph.timeaware.ITimeAwareGraphDatabase;
import org.eclipse.hawk.core.query.InvalidQueryException;
import org.eclipse.hawk.core.query.QueryExecutionException;
import org.eclipse.hawk.timeaware.queries.TimeAwareEOLOperationFactory;
import org.eclipse.hawk.timeaware.queries.TimeAwareEOLQueryEngine;
import org.eclipse.hawk.timeaware.queries.operations.reflective.TimeAwareNodeHistoryOperationContributor;
import org.eclipse.hawk.timeaware.queries.operations.reflective.TypeHistoryOperationContributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EVLQueryEngine extends TimeAwareEOLQueryEngine {
	private static final Logger LOGGER = LoggerFactory.getLogger(EVLQueryEngine.class);
	
	public EVLQueryEngine() {
		super();
	}
	@Override
	public Object query(IModelIndexer m, String query, Map<String, Object> context)
			throws InvalidQueryException, QueryExecutionException {
		final HawkState currentState = m.getCompositeStateListener().getCurrentState();
		if (currentState != HawkState.RUNNING) {
			throw new QueryExecutionException(
					String.format("Cannot run the query, as the indexer is not in the RUNNING state: it is %s instead.",
							currentState));
		}
		if (!(m.getGraph() instanceof ITimeAwareGraphDatabase)) {
			throw new QueryExecutionException(getClass().getName() + " can only be used with time-aware backends");
		}

		String defaultnamespaces = null;
		if (context != null) {
			defaultnamespaces = (String) context.get(PROPERTY_DEFAULTNAMESPACES);
		}

		final EVLQueryEngine q = new EVLQueryEngine();
		try {
			q.load(m);
			q.setDefaultNamespaces(defaultnamespaces);
			if (context == null || context.isEmpty()) {
				// nothing to do!
			} else {
				q.setContext(context);
			}
		} catch (EolModelLoadingException e) {
			throw new QueryExecutionException("Loading of EVLQueryEngine failed");
		}

		final IEvlModule module = new EvlModule();
		//module.getContext().
		module.getContext().setOperationFactory(new TimeAwareEOLOperationFactory(q));

		final OperationContributorRegistry opcRegistry = module.getContext().getOperationContributorRegistry();
		opcRegistry.add(new TimeAwareNodeHistoryOperationContributor(q));
		opcRegistry.add(new TypeHistoryOperationContributor(q));
		parseQuery(query, context, q, module);
		return q.runQuery(module);
	}
	@Override
	protected IEolModule createModule() {
		return new EvlModule();
	}
	public Collection test() {
		return new ArrayList();
	}

	@Override
	public String getHumanReadableName() {
		return "EVL Query Engine";
	}
}
