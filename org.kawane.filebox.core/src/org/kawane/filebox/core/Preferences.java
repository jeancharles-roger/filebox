/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.kawane.filebox.core.internal.Activator;
import org.osgi.service.log.LogService;

/**
 * @author Jean-Charles Roger
 *
 */
public class Preferences extends Observable {
	
	public static final String NAME = "filebox.name";
	public static final String PORT = "filebox.port";
	
	protected static final int DEFAULT_PORT = 9999;
	
	
	protected final LogService logger = Activator.getInstance().getLogger();
	
	protected Date configurationLastLoadDate = null;
	protected File configurationFile = null;

	public Preferences(File configurationFile) {
		this.configurationFile = configurationFile;
	}
	
	/** 
	 * @return the configuration file.
	 */
	protected File getConfigurationFile() {
		return configurationFile;
	}

	/**
	 * <p>
	 * Sets of properties used for configuration.
	 * </p>
	 */
	protected final Properties configuration = new Properties();

	/**
	 * <p>
	 * Loads properties in {@link #configuration} from
	 * {@link #ConfigurationFile}.
	 * </p>
	 */
	protected void loadProperties() {
		if (configurationLastLoadDate == null || getConfigurationFile().lastModified() > configurationLastLoadDate.getTime()) {
			if (getConfigurationFile().exists()) {
				try {
					configuration.clear();
					configuration.load(new FileInputStream(getConfigurationFile()));
				} catch (IOException e) {
					logger.log(LogService.LOG_ERROR, "Can't load preferences (I/O error: " + e.getMessage() +").");
				}
			}
			configurationLastLoadDate = new Date();
		}
	}

	/**
	 * <p>
	 * Saves properties in {@link #configuration} to {@link #ConfigurationFile}.
	 * </p>
	 */
	public void saveProperties() {
		try {
			configuration.store(new FileOutputStream(getConfigurationFile()), "Saved by CobEditor");
		} catch (IOException e) {
			logger.log(LogService.LOG_ERROR, "Can't save preferences (I/O error: " + e.getMessage() +").");
		}
	}

	/**
	 * <p>
	 * Get one property's value. It reloads the configuration file if needed.
	 * </p>
	 * 
	 * @param name
	 *            properties name.
	 * @return the value or null if it doesn't exists.
	 */
	public String getProperty(String name) {
		loadProperties();
		return configuration.getProperty(name);

	}

	/**
	 * <p>
	 * Set one property value. It saves the configuration file after.
	 * </p>
	 * 
	 * @param name
	 *            properties name.
	 * @param value
	 *            the value to set.
	 * @param save
	 *            if true, the property are saved
	 */
	public void setProperty(String name, String value) {
		String oldValue = getProperty(name);
		configuration.setProperty(name, value);
		getObservable().firePropertyChange(name, oldValue, value);
	}

	
	public String getName() {
		String name = getProperty(NAME);
		if ( name == null || name.length() == 0  ) {
			name = "Me";
		}
		return name;
	}
	
	public void setName(String value) {
		setProperty(NAME, value);
	}

	
	public int getPort() {
		String stringValue = getProperty(PORT);
		int value = DEFAULT_PORT;
		try {
			value = Integer.valueOf(stringValue);
		} catch( NumberFormatException e)  { /* do nothing */ }
		return value;
	}
	
	public void setPort(int value) {
		setProperty(PORT, Integer.toString(value));
	}

}
