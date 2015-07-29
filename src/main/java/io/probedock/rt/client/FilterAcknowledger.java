package io.probedock.rt.client;

import com.github.nkzawa.socketio.client.Ack;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import io.probedock.client.core.filters.FilterDefinition;
import io.probedock.client.core.filters.FilterDefinitionImpl;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Acknowledger to get the filters from the agent
 * 
 * @author Laurent Prevost <laurent.prevost@probedock.io>
 */
public class FilterAcknowledger implements Ack {
	private static final Logger LOGGER = Logger.getLogger(FilterAcknowledger.class.getCanonicalName());

	/**
	 * List of filters from the agent
	 */
	private List<FilterDefinition> filters = new ArrayList<>();
	
	@Override
	public void call(Object... os) {
		try {
			JSONArray jsonFilters = ((JSONObject) os[0]).getJSONArray("filters");

			for (int i = 0; i < jsonFilters.length(); i++) {
				JSONObject tuple = jsonFilters.getJSONObject(i);
				filters.add(new FilterDefinitionImpl(tuple.getString("type"), tuple.getString("text")));
			}
		}
		catch (Exception e) {
			LOGGER.info("Unable to parse the filters");
		}
		
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * @return Retrieve the filters or null if none is defined
	 */
	public List<FilterDefinition> getFilters() {
		return filters;
	}
	
	public boolean hasFilters() {
		return !filters.isEmpty();
	}
}
