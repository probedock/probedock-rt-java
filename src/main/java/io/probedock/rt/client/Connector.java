package io.probedock.rt.client;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.probedock.client.common.model.v1.TestResult;
import io.probedock.client.common.model.v1.TestRun;
import io.probedock.client.core.filters.FilterDefinition;
import io.probedock.client.core.serializer.json.JsonSerializer;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Send a payload to agent
 * 
 * @author Laurent Prevost <laurent.prevost@probedock.io>
 */
public class Connector implements Serializable {
	public static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = Logger.getLogger(Connector.class.getCanonicalName());

	private final Socket socket;
	
	private static final Connector instance = new Connector();
	
	/**
	 * Constructor
	 */
	private Connector() {
		this.socket = createConnectedSocket(Configuration.getInstance().getUrl());
	}
	
	/**
	 * @return Instance of a connector to the agent
	 */
	public static Connector getInstance() {
		return instance;
	}
	
	/**
	 * @return True if the agent is connected
	 */
	private boolean isStarted() {
		return socket != null && socket.connected();
	}
	
	/**
	 * Send a starting notification to the agent
	 * 
	 * @param projectName The project name
	 * @param projectVersion The project version
	 * @param category The category
	 */
	public void notifyStart(String projectName, String projectVersion, String category) {
		try {
			if (isStarted()) {
				JSONObject startNotification = new JSONObject().
					put("project", new JSONObject().
						put("name", projectName).
						put("version", projectVersion)
					).
					put("category", category);
				
				socket.emit("run:start", startNotification);
			}
			else {
				LOGGER.log(Level.WARNING, "Probe Dock RT is not available to send the start notification");
			}
		}
		catch (Exception e) {
			LOGGER.log(Level.INFO, "Unable to send the start notification to the agent. Cause: " + e.getMessage());
			
			if (LOGGER.getLevel() == Level.FINEST) {
				LOGGER.log(Level.FINEST, "Exception: ", e);
			}
		}
	}
	
	/**
	 * Send a test result notification to the agent
	 * 
	 * @param result The test result to get the data
	 * @param projectName The default project name if none provided by the test result
	 * @param projectVersion The default project version if none provided by the test result
	 * @param category The default category if none provided by the test result
	 */
	public void notifyTestResult(TestResult result, String projectName, String projectVersion, String category) {
		try {
			if (isStarted()) {
				JSONObject testResult = new JSONObject().
					put("k", result.getKey()).
					put("f", result.getFingerprint()).
					put("n", result.getName()).
					put("j", projectName).
					put("v", projectVersion).
					put("e", result.isActive()).
					put("p", result.isPassed()).
					put("d", result.getDuration()).
					put("m", result.getMessage()).
					put("c", (result.getCategory() != null && !result.getCategory().isEmpty() ? result.getCategory() : category)).
					put("g", new JSONArray(result.getTags())).
					put("t", new JSONArray(result.getTickets())).
					put("a", new JSONObject(result.getData()));

				socket.emit("run:test:result", testResult);
			}
		}
		catch (Exception e) {
			if (LOGGER.getLevel() == Level.FINEST) {
				LOGGER.log(Level.FINEST, "Unable to send the test result notification to the agent. Cause: " + e.getMessage());
				LOGGER.log(Level.FINEST, "Exception: ", e);
			}
		}
	}
	
	/**
	 * Send a ending notification to the agent
	 * 
	 * @param projectName The project name
	 * @param projectVersion The project version
	 * @param category The category
	 * @param duration The duration of the test run
	 */
	public void notifyEnd(String projectName, String projectVersion, String category, long duration) {
		try {
			if (isStarted()) {
				JSONObject endNotification = new JSONObject().
					put("project", new JSONObject().
						put("name", projectName).
						put("version", projectVersion)
					).
					put("category", category).
					put("duration", duration);
				
				socket.emit("run:end", endNotification);
			}
			else {
				LOGGER.log(Level.WARNING, "Probe Dock RT is not available to send the end notification");
			}
		}
		catch (Exception e) {
			LOGGER.log(Level.INFO, "Unable to send the end notification to the agent. Cause: " + e.getMessage());
			
			if (LOGGER.getLevel() == Level.FINEST) {
				LOGGER.log(Level.FINEST, "Exception:", e);
			}
		}
	}
	
	/**
	 * Send a test result to the agent. This is a best effort and when the request failed, there is no crash
	 * 
	 * @param testRun The result to send
	 */
	public void send(TestRun testRun) {
		try {
			if (isStarted()) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				new JsonSerializer().serializePayload(new OutputStreamWriter(baos), testRun, false);

				socket.emit("payload", new String(baos.toByteArray()));
			}
			else {
				LOGGER.log(Level.WARNING, "Probe Dock RT is not available to send the test results");
			}
		}
		catch (Exception e) {
			LOGGER.log(Level.WARNING, "Unable to send the result to the agent. Cause: " + e.getMessage());
			
			if (LOGGER.getLevel() == Level.FINEST) {
				LOGGER.log(Level.FINEST, "Exception: ", e);
			}
		}
	}
	
	/**
	 * Try to get a filter list from the agent
	 * 
	 * @return The list of filters or null if there is none or the agent is not accessible
	 */
	public List<FilterDefinition> getFilters() {
		try {
			if (isStarted()) {
				final FilterAcknowledger acknowledger = new FilterAcknowledger();
				
				// Be sure that the emit/ack is synchronous to get the filters before the test are run
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							LOGGER.finest("Retrieve filters");
							socket.emit("filters:get", acknowledger);
						}
						catch (Exception e) {
							LOGGER.finest("Unable to get the filters: " + e.getMessage());
							synchronized (acknowledger) {
								acknowledger.notify();
							}
						}
					}
				}).start();

				synchronized (acknowledger) {
					acknowledger.wait();
				}

				if (!acknowledger.hasFilters()) {
					for (FilterDefinition filter : acknowledger.getFilters()) {
						LOGGER.info("Filter element: " + filter);
					}
				}

				return acknowledger.getFilters();
			}
		}
		catch (Exception e) {
			LOGGER.warning("Unable to retrieve the filters from the agent. Cause: " + e.getMessage());
			e.printStackTrace();
			if (LOGGER.getLevel() == Level.FINEST) {
				LOGGER.log(Level.FINEST, "Exception: ", e);
			}
		}
		
		return null;
	}
	
	/**
	 * Create a new connection to the agent
	 * 
	 * @param url The URL
	 * @return The socket connected to the agent or null if the connection is not possible
	 */
	private Socket createConnectedSocket(final String url) {
		try {
			final Socket initSocket = IO.socket(url);

			final Callback callback = new Callback();
			
			initSocket.on(Socket.EVENT_CONNECT, callback);
			initSocket.on(Socket.EVENT_CONNECT_ERROR, callback);
			initSocket.on(Socket.EVENT_CONNECT_TIMEOUT, callback);
			initSocket.on(Socket.EVENT_CONNECT_ERROR, callback);
			initSocket.on(Socket.EVENT_DISCONNECT, callback);
			initSocket.on(Socket.EVENT_ERROR, callback);
			
			// Be sure that the emit/ack is synchronous to get the filters before the test are run
			LOGGER.finest("Connection to Probe Dock RT"); // Unable to use the logger into the run method
			new Thread(new Runnable() {
				@Override
				public void run() {
					initSocket.connect();
				}
			}).start();
			
			synchronized (callback) {
				callback.wait();
			}

			if (!initSocket.connected()) {
				LOGGER.warning("Probe Dock RT is not available");
				return null;
			}
			
			return initSocket;
		}
		catch (URISyntaxException | InterruptedException e) {
			LOGGER.log(Level.WARNING, "Unknown error", e);
		}
		
		return null;
	}
}
