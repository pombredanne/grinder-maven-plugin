//   Copyright 2012 Giuseppe Iacono, Felipe Munoz Castillo
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.fides;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provide methods to configure the plug-in
 * 
 * @author Giuseppe Iacono
 */
public abstract class GrinderPropertiesConfigure extends AbstractMojo
{
	// Jython version for The Grinder
	public static final String GRINDER_JYTHON_VERSION = "2.2.1";

	// Jython version for The Grinder Analyzer
	public static final String GRINDER_ANALYZER_JYTHON_VERSION = "2.5.2";	
	
	// default agent
	private static final boolean DEFAULT_DAEMON_OPTION = false;						
	
	// default agent sleep time in milliseconds
	private static final long DEFAULT_DAEMON_PERIOD = 60000;							
	
	// default local path test directory
	private static final String PATH_TEST_DIR = "src/test/jython";  		
	
	// local configuration directory
	private static final String CONFIG = "target/test/config";				
	
	// local grinder properties directory
	private static final String PATH_PROPERTIES_DIR = "src/test/config"; 	
	
	// local log directory 
	private static final String LOG_DIRECTORY = "target/test/log_files"; 			
	
	// local tcpproxy directory
	private static final String TCP_PROXY_DIRECTORY = "target/test/tcpproxy";	
	
	// grinder properties
	private Properties propertiesPlugin = new Properties();					

	// configuration file
	private File fileProperties = null;										
	
	// grinder properties file path
	private String pathProperties = null;									

	// jython test paths 
	private Set<String> tests = null;												

	// value of agent daemon option
	private boolean daemonOption = DEFAULT_DAEMON_OPTION;							
	
	// value of agent sleep time
	private long daemonPeriod = DEFAULT_DAEMON_PERIOD;								 
	
	private Set<File> propertiesFiles = new HashSet<File>();
	
	// GrinderPropertiesConfigure logger
	private final Logger logger = LoggerFactory.getLogger("GrinderPropertiesConfigure");
	
	/**
	 * List of properties defined in the pom.xml file of Maven project.
	 * 
	 * @parameter default-value="${project.properties}"
	 */
	private Map<String, String> properties;

	/**
	 * The grinder properties file path defined in the pom.xml file of Maven project.
	 * 
	 * @parameter
	 */
	private String path;

	/**
	 * The absolute path of test script directory defined in the pom.xml file of Maven project.
	 * 
	 * @parameter
	 */
	private String pathTest;

	/**
	 * Agent daemon option defined in the pom.xml file of Maven project.
	 * 
	 * @parameter
	 */
	private boolean daemon_option;

	/**
	 * Agent sleep time in milliseconds defined in the pom.xml file of Maven project.
	 * 
	 * @parameter
	 */
	private long daemon_period;
	
	/**
	 * Include the project defined dependencies (false)
	 * 
	 * @parameter 
	 */
	private boolean includeDependencies;
	
    /**
     * The enclosing project.
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;
	
	/**
	 * List of Plugin dependencies
	 * 
     * @parameter expression="${plugin.artifacts}"
     */
    private List<Artifact> pluginArtifacts; 
	
	public List<Artifact> getPluginArtifacts() {
		return pluginArtifacts;
	}

	public void setPluginArtifacts(List<Artifact> pluginArtifacts) {
		this.pluginArtifacts = pluginArtifacts;
	}
	
	public static boolean getDEFAULT_DAEMON_OPTION() {
		return DEFAULT_DAEMON_OPTION;
	}
	
	public static long getDEFAULT_DAEMON_PERIOD() {
		return DEFAULT_DAEMON_PERIOD;
	}
	
	public static String getPATH_TEST_DIR() {
		return PATH_TEST_DIR;
	}
	
	public static String getCONFIG() {
		return CONFIG;
	}

	public static String getPATH_PROPERTIES_DIR() {
		return PATH_PROPERTIES_DIR;
	}

	public static String getLOG_DIRECTORY() {
		return LOG_DIRECTORY;
	}

	public static String getTCP_PROXY_DIRECTORY() {
		return TCP_PROXY_DIRECTORY;
	}	
	
	public Set<File> getPropertiesFiles() {
		return this.propertiesFiles;
	}
	
	public Properties getPropertiesPlugin() {
		return propertiesPlugin;
	}

	public void setPropertiesPlugin(Properties propertiesPlugin) {
		this.propertiesPlugin = propertiesPlugin;
	}

	public File getFileProperties() {
		return fileProperties;
	}

	public void setFileProperties(File fileProperties) {
		this.fileProperties = fileProperties;
	}

	public String getPathProperties() {
		return pathProperties;
	}
	
	public void setPathProperties(String pathProperties) {
		this.pathProperties = pathProperties;
	}
	
	public Set<String> getTests() {
		return tests;
	}

	public void addTest(String test) {
		if ( this.tests == null ) {
			this.tests = new HashSet<String>();
		}
		this.tests.add(test);
	}
	
	public boolean isDaemonOption() {
		return daemonOption;
	}

	public boolean getdaemonOption() {
		return daemonOption;
	}
	
	public void setDaemonOption(boolean daemonOption) {
		this.daemonOption = daemonOption;
	}

	public long getDaemonPeriod() {
		return daemonPeriod;
	}
	
	public void setDaemonPeriod(long daemonPeriod) {
		this.daemonPeriod = daemonPeriod;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	protected abstract String getJythonVersion();

	private void setClassPath() {
		// Print the list of plugin dependencies
		logger.debug("------------------PROJECT DEPENDENCIES----------------------");

		Artifact a = null;
		Collection artifacts = pluginArtifacts;
		StringBuffer pluginDependencies = new StringBuffer();
		String grinderJar = null;
				
		for (Iterator i = artifacts.iterator();  i.hasNext();) {
			a = (Artifact) i.next();
			logger.debug("------------------------------------------------------------");
			if (a.getArtifactId().equals("grinder") == false
				&& ( !a.getArtifactId().contains("jython")
					 || (a.getArtifactId().contains("jython") && a.getVersion().equals(getJythonVersion())))) {
				
				logger.debug("GroupId: {}  ArtifactId: {}  Version: {} " , 
						new Object[]{a.getGroupId(), a.getArtifactId(),a.getVersion()});
				try {
					grinderJar = MavenUtilities.getPluginAbsolutePath(
														a.getGroupId(), 
														a.getArtifactId(), 
														a.getVersion());
					grinderJar = MavenUtilities.normalizePath(grinderJar);
					pluginDependencies.append(grinderJar);
					if (i.hasNext()) {
						pluginDependencies.append(File.pathSeparator);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				}				
			}
		}
		
		// include all COMPILE scoped dependencies if configured for such
		if ( includeDependencies ) {
			for (Artifact artifact : (Set<Artifact>) this.project.getDependencyArtifacts()) {
				if ("jar".equals(artifact.getType())
						&& !Artifact.SCOPE_PROVIDED.equals(artifact.getScope())
						&& (!Artifact.SCOPE_TEST.equals(artifact.getScope()))) {
					logger.debug("Adding dependency: {} to the classpath", ArtifactUtils.versionlessKey(artifact) );
					try {
						grinderJar = MavenUtilities.getPluginAbsolutePath(
								artifact.getGroupId(), 
								artifact.getArtifactId(), 
								artifact.getVersion());
						grinderJar = MavenUtilities.normalizePath(grinderJar);
						pluginDependencies.append(grinderJar);
						pluginDependencies.append(File.pathSeparator);
					} catch(Exception ex ) {
						ex.printStackTrace();
					}
				}
			}
		}
		
		propertiesPlugin.setProperty("grinder.jvm.classpath", pluginDependencies.toString());	
		
		logger.debug("--- Classpath Now configured");
	}

	/*
	 * slf4j-style message with string to replace
	 */
	private void logConfigErrorAndExit(String message, Object ... replaceVars) {
		logger.error("");
		logger.error(" ----------------------------");
		logger.error("|   Configuration ERROR!!!   |");
		logger.error(" ----------------------------");
		logger.error("");
		logger.error(message, replaceVars);
		logger.error("");
		logger.error(" Create this directory to configure grinder properties file. ");
		System.exit(0);
	}

	/**
	 * Set grinder properties
	 */
	private void initPropertiesFile() 
	{		
		if (path == null) {		// try to find grinder properties file in the PATH_PROPERTIES_DIR
			
			// make sure we have a properties file
			File[] config = new File(PATH_PROPERTIES_DIR).listFiles();
			if (config == null) {
				logConfigErrorAndExit("Configuration directory {} do not exists! ", PATH_PROPERTIES_DIR);
			}

			// check the length f the array is not empty
			if (config.length == 0) {
				logConfigErrorAndExit("{} is empty. Copy grinder properties file in this directory or set <path> from POM file. ", PATH_PROPERTIES_DIR);				
			}
			
			// allow only one properties file for now
			if (config.length > 1) {
				logConfigErrorAndExit("{} contains other files. Only one grinder.properties file is allowed", PATH_PROPERTIES_DIR);							
			}
			
			String properties = config[0].getName();
			
			if (!properties.endsWith(".properties")) {
				logConfigErrorAndExit("{}/{} must have a '.properties' extension.", PATH_PROPERTIES_DIR, properties);														
			}
			String pathProp = PATH_PROPERTIES_DIR + File.separator + properties;
			setPathProperties(pathProp);
		}
		else {
			setPathProperties(path);	
		}
		
		// load grinder properties from the grinder properties file
		FileInputStream is = null;
		try {
			is = new FileInputStream(pathProperties);
			propertiesPlugin.load(is); 					
		} catch (FileNotFoundException e) {
			logConfigErrorAndExit("The grinder properties file path {} does not exist.", pathProperties);			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {}
			}
		}		
		
		// load grinder properties from pom.xml file of Maven project
		extractGrinderProperty(System.getProperties());
		extractGrinderProperty(properties);		
		
		setClassPath();
		
		logger.debug("--- Grinder properties file:  {}", pathProperties);
	}
	
	private void extractGrinderProperty(Map propertiesSource) {
		for(Object pair : propertiesSource.entrySet() )
			if ( pair instanceof Map.Entry) {
				Map.Entry entry = (Map.Entry)pair;
				if (entry.getKey() instanceof String ) {					
					if (entry.getValue() != null && entry.getKey().toString().startsWith("grinder.")) {
						propertiesPlugin.setProperty(entry.getKey().toString(),entry.getValue().toString());
						logger.debug("--Current property: {}", pair);
					}
				}
			}
	}


	/**
	 * Set test file path to execute
	 */
	private void initTestFile() 
	{
		// if pathTest == null, pathTest = PATH_TEST_DIR.
		if ( StringUtils.isBlank(pathTest)) {
			pathTest = PATH_TEST_DIR;
		}

		if ( propertiesPlugin.containsKey("grinder.script")) {
			String [] names = StringUtils.split(propertiesPlugin.getProperty("grinder.script"), ',');
			for ( String name : names ) {
				addTest(name);
			}
		}  else {
			File filePathTest = new File(pathTest);
			if (!filePathTest.exists() ) {
				logConfigErrorAndExit("{} do not exist. Create this directory to configure the test file.", pathTest);				
			}
			File[] jython = filePathTest.listFiles();
			if (jython.length == 0) {
				logConfigErrorAndExit("{} is empty! Copy test files to this directory or set <pathTest> from POM file.", pathTest);				
			}	
			for ( File jy : jython ){
				addTest(jy.getName());
			}				
		}
			
		logger.debug("--- Jython test file:  {}", tests);
	}
	
	/**
	 * Set log directory
	 */
	private void initLogDirectory() 
	{
		// make sure the logDirectory exists
		File logDirectory = new File(LOG_DIRECTORY);
		if (logDirectory != null && !logDirectory.exists()) {
			logDirectory.mkdirs();
		}

		// set logDirectory
		propertiesPlugin.setProperty("grinder.logDirectory", LOG_DIRECTORY);
		
		logger.debug("--- Log directory:  {}", LOG_DIRECTORY);
	}
	
	/**
	 * Set agent daemon option and sleep time
	 */
	private void initAgentOption()
	{
		if (daemon_option == true) {
			daemonOption = true;
			if (daemon_period > 0) {
				daemonPeriod = daemon_period;
			} 
			else {
				daemonPeriod = DEFAULT_DAEMON_PERIOD;
			}
		}
		else {
			daemonOption = DEFAULT_DAEMON_OPTION;
			daemonPeriod = DEFAULT_DAEMON_PERIOD;
		}
		
		logger.debug("--- Agent -daemon option: {} ", daemonOption);
		
		if (daemonOption == true) {
			logger.debug("--- Agent sleep time:  {}", daemonPeriod);
		}
	}
	
	/**
	 * Initialize configuration directory
	 */
	private void initConfigurationDirectory()
	{
		// make sure the configDirectory exists
		File configDirectory = new File(CONFIG);	
		if (configDirectory != null && !configDirectory.exists()) {
			configDirectory.mkdirs();								
		}
		

		BufferedWriter out;
	
		try {
			
			Properties propCopy;
			for ( String fileName : tests ) {
				logger.debug("using file {} for copy", fileName);
				File file = new File(CONFIG,"grinder_agent_"+FilenameUtils.getBaseName(fileName)+".properties");
				propertiesFiles.add(file);
				out = new BufferedWriter(new FileWriter(file));
				// need to iterate keys and copy to new propertes
				propCopy = new Properties(propertiesPlugin);
				propCopy.put("grinder.script", fileName);
				propCopy.store(out, "Grinder Agent Properties for " + fileName);
				FileUtils.copyFile(new File(pathTest,fileName), new File(CONFIG,fileName));
			}
		} catch(IOException ioe) {
			ioe.printStackTrace();
			logConfigErrorAndExit("Could not copy files: {}", tests);
		}
		
		logger.debug("--- Grinderplugin to be configured ---");
	}
	
	public void execute() throws MojoExecutionException, MojoFailureException 
	{
		initPropertiesFile();
		
		initTestFile();
		
		initLogDirectory();
		
		initAgentOption();
				
		initConfigurationDirectory();		
	}
	
}
