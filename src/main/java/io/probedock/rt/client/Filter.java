package io.probedock.rt.client;

import io.probedock.client.core.filters.FilterDefinition;

import java.io.Serializable;
import java.util.List;

/**
 * Probe Dock RT filter to gather filters from the Agent
 * 
 * @author Laurent Prevost <laurent.prevost@probedock.io>
 */
public class Filter implements Serializable {
	public static final long serialVersionUID = 1L;
	
	/**
	 * The connector to communicate with the agent
	 */
	private static final Connector connector = Connector.getInstance();
	
	/**
	 * @return The filters defined in the Agent
	 */
	public List<FilterDefinition> getFilters() {
		return connector.getFilters();
	}
}
