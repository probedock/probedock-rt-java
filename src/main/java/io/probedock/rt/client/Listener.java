package io.probedock.rt.client;

import io.probedock.client.common.model.v1.TestResult;

import java.io.Serializable;

/**
 * Probe Dock RT listener to gather information and forward to the agent
 * 
 * @author Laurent Prevost <laurent.prevost@probedock.io>
 */
public class Listener implements Serializable {
	public static final long serialVersionUID = 1L;
	
	/**
	 * The connector to communicate with the agent
	 */
	private static final Connector connector = Connector.getInstance();
	
	/**
	 * Notify that a test run started
	 * 
	 * @param projectApiId Project API ID
	 * @param version Project version
	 * @param category Category of tests
	 */
	public void testRunStart(String projectApiId, String version, String category) {
		connector.notifyStart(projectApiId, version, category);
	}
	
	/**
	 * Notify that a test run ended
	 * 
	 * @param projectApiId Project API ID
	 * @param version Project version
	 * @param category Category of tests
	 * @param duration Duration of the whole test run
	 */
	public void testRunEnd(String projectApiId, String version, String category, Long duration) {
		connector.notifyEnd(projectApiId, version, category, duration);
	}

	/**
	 * Notify a test result
	 * 
	 * @param test The test data
	 * @param projectApidId The project API ID
	 * @param version The project version
	 * @param category The catory of tests
	 */
	public void testResult(TestResult test, String projectApidId, String version, String category) {
		connector.notifyTestResult(test, projectApidId, version, category);
	}
}
