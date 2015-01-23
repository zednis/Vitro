/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex;

import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event.Type.PROGRESS;
import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event.Type.STOP_PROCESSING_URIS;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineNotRespondingException;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event.Type;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Listener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.search.controller.IndexController;
import edu.cornell.mannlib.vitro.webapp.search.controller.IndexHistory;
import edu.cornell.mannlib.vitro.webapp.startup.ComponentStartupStatusImpl;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;
import edu.cornell.mannlib.vitro.webapp.utils.developer.listeners.DeveloperDisabledChangeListener;

/**
 * Start the SearchIndexer. Create a listener on the RDFService and link it to
 * the indexer.
 * 
 * Create a history object as a listener and make it available to the
 * IndexController.
 * 
 * Create a listener that will call commit() on the SearchEngine every time it
 * hears a progress or completion event.
 */
public class SearchIndexerSetup implements ServletContextListener {
	private static final Log log = LogFactory.getLog(SearchIndexerSetup.class);

	private ServletContext ctx;
	private Application app;
	private SearchIndexer searchIndexer;
	private IndexingChangeListener listener;
	private DeveloperDisabledChangeListener listenerWrapper;
	private IndexHistory history;
	private Committer committer;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ctx = sce.getServletContext();
		app = ApplicationUtils.instance();

		StartupStatus ss = StartupStatus.getBean(ctx);

		try {
			searchIndexer = app.getSearchIndexer();

			listener = new IndexingChangeListener(searchIndexer);
			
			// Wrap it so it can be disabled by a developer flag.
			listenerWrapper = new DeveloperDisabledChangeListener(listener,
					Key.SEARCH_INDEX_SUPPRESS_MODEL_CHANGE_LISTENER);

			RDFServiceUtils.getRDFServiceFactory(ctx).registerListener(
					listenerWrapper);

			this.history = new IndexHistory();
			searchIndexer.addListener(this.history);
			IndexController.setHistory(this.history);

			this.committer = new Committer();
			searchIndexer.addListener(this.committer);

			searchIndexer
					.startup(app, new ComponentStartupStatusImpl(this, ss));
			searchIndexer.unpause();

			ss.info(this, "Setup of search indexer completed.");
		} catch (RDFServiceException e) {
			ss.fatal(this, "Failed to register the model changed listener.", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		searchIndexer.shutdown(app);

		searchIndexer.removeListener(this.committer);
		searchIndexer.removeListener(this.history);

		try {
			RDFServiceUtils.getRDFServiceFactory(ctx).unregisterListener(
					listenerWrapper);
		} catch (RDFServiceException e) {
			log.warn("Failed to unregister the indexing listener.");
		}
		listener.shutdown();
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class Committer implements Listener {
		private final SearchEngine searchEngine = ApplicationUtils.instance()
				.getSearchEngine();

		@Override
		public void receiveSearchIndexerEvent(Event event) {
			Type type = event.getType();
			if (type == PROGRESS || type == STOP_PROCESSING_URIS) {
				commitChanges();
			}
		}

		private void commitChanges() {
			try {
				searchEngine.commit();
			} catch (SearchEngineNotRespondingException e) {
				log.error("Failed to commit the changes: "
						+ "the search engine is not responding.");
			} catch (Exception e) {
				log.error("Failed to commit the changes.", e);
			}
		}
	}
}
