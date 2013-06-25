package com.confino.plugins.sonar;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class CoverageBreakerTest {
	
	@Test
	public void getCoverageActualTest(){
		File file = new File("src/test/resources/jacoco.xml");
		assertTrue(file.exists());
		CoverageBreaker.getCoverageActual(file);
	}

}
