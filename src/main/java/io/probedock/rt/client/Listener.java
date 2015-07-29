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
	 * @param project Project name
	 * @param version Project version
	 * @param category Category of tests
	 */
	public void testRunStart(String project, String version, String category) {
		connector.notifyStart(project, version, category);
	}
	
	/**
	 * Notify that a test run ended
	 * 
	 * @param project Project name
	 * @param version Project version
	 * @param category Category of tests
	 * @param duration Duration of the whole test run
	 */
	public void testRunEnd(String project, String version, String category, Long duration) {
		connector.notifyEnd(project, version, category, duration);
	}

	/**
	 * Notify a test result
	 * 
	 * @param test The test data
	 * @param project The project name
	 * @param version The project version
	 * @param category The catory of tests
	 */
	public void testResult(TestResult test, String project, String version, String category) {
		connector.notifyTestResult(test, project, version, category);
	}
}
