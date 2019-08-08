package org.hawk.versionaware.queries;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.execute.operations.contributors.OperationContributorRegistry;
import org.hawk.core.IModelIndexer;
import org.hawk.core.IStateListener.HawkState;
import org.hawk.core.graph.IGraphNode;
import org.hawk.core.graph.IGraphTransaction;
import org.hawk.core.graph.timeaware.ITimeAwareGraphDatabase;
import org.hawk.core.graph.timeaware.ITimeAwareGraphNode;
import org.hawk.core.query.InvalidQueryException;
import org.hawk.core.query.QueryExecutionException;
import org.hawk.epsilon.emc.EOLQueryEngine;
import org.hawk.epsilon.emc.wrappers.GraphNodeWrapper;
import org.hawk.graph.GraphWrapper;
import org.hawk.graph.MetamodelNode;
import org.hawk.graph.ModelElementNode;
import org.hawk.graph.TypeNode;
import org.hawk.timeaware.queries.TimeAwareEOLOperationFactory;
import org.hawk.timeaware.queries.TimeAwareEOLQueryEngine;
import org.hawk.timeaware.queries.operations.reflective.TimeAwareNodeHistoryOperationContributor;
import org.hawk.timeaware.queries.operations.reflective.TypeHistoryOperationContributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;

public class VersionEOLQueryEngine extends TimeAwareEOLQueryEngine{
	private static final Logger LOGGER = LoggerFactory.getLogger(VersionEOLQueryEngine.class);
	public VersionEOLQueryEngine() {
		super();
	}

	public Collection<?> getAllVersion() {
		final Set<Object> allContents = new HashSet<Object>();
		final Collection<Long> instants = getAllInstants();
		for (long instant : instants) {
			allContents.add(Arrays.asList(instant, allInstancesAt(instant)));
				
		} 
		return allContents;
	}
	public Collection<?> getVersion(int i){
		Collection<Long> collect = getAllInstants();
		if (collect.isEmpty()) {
			return collect;
		}
		int size= collect.size()-1;
		if (i<size) {
			size= i;
		}
		Object[] times = collect.toArray();
		return allInstancesAt((Long)times[size]);
	}
	public Collection<?> getVersion(String date){
		final List<Object> results = new ArrayList<>();
		DateTimeFormatter dateFormatter
        = DateTimeFormatter.ofPattern("d-MM-uuuu", Locale.ENGLISH);
		long starttime = LocalDate.parse(date, dateFormatter)
	            .atStartOfDay(ZoneOffset.UTC)
	            .toInstant()
	            .toEpochMilli();
		long endtime = LocalDate.parse(date, dateFormatter)
	            .atStartOfDay(ZoneOffset.UTC)
	            .toInstant()
	            .toEpochMilli() + (24*3600*1000);
		Collection<Long> instants = getInstantBetween(starttime,endtime);
		
		for (long instant : instants) {
			results.add(Arrays.asList(instant, allInstancesAt(instant)));
				
		} 
		
		return results;
	}
	
	protected Collection<Long> getInstantBetween(long start,long end){
		Collection<Long> instants = getAllInstants();
		Collection<Long> results=new TreeSet<>();
		for (long instant : instants) {
			if (instant <= start && instant >= end)
				results.add(instant);		
		} 
		return results;
	}
	public Collection<Long> getAllInstants() {
		final Set<Long> instants = new TreeSet<>();
		final ITimeAwareGraphDatabase taDB = (ITimeAwareGraphDatabase) graph;
		try (IGraphTransaction tx = taDB.beginTransaction()) {
			GraphWrapper gW = new GraphWrapper(taDB);
			for (MetamodelNode mm : gW.getMetamodelNodes()) {
				for (TypeNode tn : mm.getTypes()) {
					ITimeAwareGraphNode taTypeNode = (ITimeAwareGraphNode) tn.getNode();
					instants.addAll(taTypeNode.getAllInstants());
				}
			}
			tx.success();
		} catch (Exception e) {
			try {
				throw new QueryExecutionException(e);
			} catch (QueryExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return instants;
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

		final VersionEOLQueryEngine q = new VersionEOLQueryEngine();
		try {
			q.load(m);
			q.setDefaultNamespaces(defaultnamespaces);
			if (context == null || context.isEmpty()) {
				// nothing to do!
			} else {
				q.setContext(context);
			}
		} catch (EolModelLoadingException e) {
			throw new QueryExecutionException("Loading of EOLQueryEngine failed");
		}

		final IEolModule module = createModule();
		module.getContext().setOperationFactory(new TimeAwareEOLOperationFactory(q));

		final OperationContributorRegistry opcRegistry = module.getContext().getOperationContributorRegistry();
		opcRegistry.add(new TimeAwareNodeHistoryOperationContributor(q));
		opcRegistry.add(new TypeHistoryOperationContributor(q));
		parseQuery(query, context, q, module);
		return q.runQuery(module);
	}
	
	
	
	@Override
	public String getHumanReadableName() {
		return "Version Aware EOL Query Engine";
	}
}
