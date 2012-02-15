package sellstome.solr.servlet

import sellstome.solr.core.CoreContainer


/**
 * A solr entry point. This allows us a fully customizable solr request chain.
 * Also it allows us to plugin our specific logic.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class SolrDispatchFilter extends org.apache.solr.servlet.SolrDispatchFilter {

  /** Method to override to change how CoreContainer initialization is performed. */
  protected override def createInitializer = new CoreContainer.Initializer()

}