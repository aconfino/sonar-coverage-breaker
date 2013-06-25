package com.confino.plugins.sonar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BuildBreaker;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;

public class CoverageBreaker extends BuildBreaker {

	private static final Logger LOG = LoggerFactory.getLogger(CoverageBreaker.class);
	private static final String COVERAGE_PROPERTY_KEY = "coverage-threshhold";
	private final Settings settings;

	public CoverageBreaker(Settings settings) {
		this.settings = settings;
	}

	public void executeOn(Project project, SensorContext context) {
		if (settings.getBoolean(CoverageBreakerPlugin.SKIP_KEY)) {
			LOG.debug("CoverageBreaker disabled on project " + project);
		} else {
			analyzeCoverage();
		}
	}

	private void analyzeCoverage() {
		Integer coverageThreshhold = parseThreshhold();
		Integer coverageActual = getCoverageActual();
		if (coverageActual != null && coverageThreshhold != null && coverageActual < coverageThreshhold) {
			fail("Coverage is less than the threshhold of " + coverageThreshhold + ".");
		}
	}
	
	private Integer parseThreshhold(){
		File propertyFile = new File("build/build-project.properties");
		if (propertyFile.exists()){
			return getCoverageThreshhold(propertyFile);
		}
		return null;
	}
	
	private Integer getCoverageThreshhold(File file){
		Properties prop = new Properties();
		Integer threshhold = null;
		try {
			prop.load(new FileInputStream(file));
			if (prop.containsKey(COVERAGE_PROPERTY_KEY)){
				threshhold = Integer.valueOf(prop.getProperty(COVERAGE_PROPERTY_KEY));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return threshhold;
	}
	
	private Integer getCoverageActual(){
		// hardcoded for now...
		return new Integer(70);
	}

}
