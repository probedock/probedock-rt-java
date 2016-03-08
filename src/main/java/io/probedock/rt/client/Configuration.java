package io.probedock.rt.client;

import io.probedock.client.common.config.ServerListConfiguration;
import io.probedock.client.common.config.ScmInfo;
import io.probedock.client.common.config.YamlConfigurationFile;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration for Probe Dock RT clients.
 *
 * <strong>Example:</strong>
 * <pre>
 *   enabled: true
 *   host: 127.0.0.1
 *   port: 1337
 * </pre>
 *
 * @author Laurent Prevost <laurent.prevost@probedock.io>
 */
public class Configuration {
	private static final Logger LOGGER = Logger.getLogger(Configuration.class.getCanonicalName());

	/**
	 * Base configuration that should be present in the home directory
	 */
	private static final String BASE_CONFIG_PATH = ".probedock/probedock-rt.yml";

	/**
	 * Root node name of the tree configuration
	 */
	private static final String P_ROOT_NODE_NAME = "probedock-rt";

	private static final String P_PDRT_ENABLE   = P_ROOT_NODE_NAME + ".enable";
	private static final String P_PDRT_HOST     = P_ROOT_NODE_NAME + ".host";
    private static final String P_PDRT_PORT     = P_ROOT_NODE_NAME + ".port";

	/**
	 * Not thread safe, not critical
	 */
	private static Configuration instance;

	/**
	 * Configuration
	 */
	protected CompositeConfiguration config;

	/**
	 * Define if Probe Dock is enabled
	 */
	private boolean enabled = true;

	/**
	 * Constructor
	 */
	protected Configuration() {
		config = new CompositeConfiguration();

		try {
			config.addConfiguration(new YamlConfigurationFile(BASE_CONFIG_PATH, P_ROOT_NODE_NAME, new ServerListConfiguration(), new ScmInfo()));
		}
		catch (ConfigurationException ce) {
			if (LOGGER.getLevel() == Level.FINEST) {
				LOGGER.log(Level.FINEST, "Unable to load the probe dock rt configuration.", ce);
			}
			else {
				LOGGER.log(Level.WARNING, "Unable to load the probe dock rt configuration due to: " + ce.getMessage());
			}
		}
	}

	/**
	 * @return The configuration instance
	 */
	public static Configuration getInstance() {
		if (instance == null) {
			instance = new Configuration();
		}

		return instance;
	}

	/**
	 * Enforce the fact that a parameter is mandatory
	 * @param name The name of the parameter
	 * @return The value found
	 * @throws RuntimeException When a mandatory parameter is missing
	 */
	private String getMandatory(String name) {
		if (!config.containsKey(name)) {
			throw new RuntimeException(name + " parameter is missing.");
		}
		else {
			return config.getString(name);
		}

	}

	/**
	 * @return Define if the clients will communicate with the agent or not
	 */
	public boolean isEnabled() {
		return config.getBoolean(P_PDRT_ENABLE, true);
	}

	/**
	 * @return The URL to send the notifications and retrieve the filters
	 */
	public String getUrl() {
		return "http://" + config.getString(P_PDRT_HOST, "127.0.0.1") + ":" + config.getInteger(P_PDRT_PORT, 1337);
	}
}