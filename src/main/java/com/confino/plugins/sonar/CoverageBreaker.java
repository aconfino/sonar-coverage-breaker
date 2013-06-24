package com.confino.plugins.sonar;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BuildBreaker;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;

public class CoverageBreaker extends BuildBreaker {

	private static final Logger LOG = LoggerFactory.getLogger(CoverageBreaker.class);

	private final Settings settings;

	public CoverageBreaker(Settings settings) {
		this.settings = settings;
	}

	public void executeOn(Project project, SensorContext context) {
		if (settings.getBoolean(CoverageBreakerPlugin.SKIP_KEY)) {
			LOG.debug("CoverageBreaker disabled on project " + project);
		} else {
			compareCoverage();
		}
	}

	private void compareCoverage() {
		int value = new Random().nextInt(100);
		if (value < 50) {
			fail("Coverage is less than " + value + ".");
		}
	}

}
