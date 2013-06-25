package com.confino.plugins.sonar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BuildBreaker;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CoverageBreaker extends BuildBreaker {

	private static final Logger LOG = LoggerFactory
			.getLogger(CoverageBreaker.class);
	private static final String COVERAGE_PROPERTY_KEY = "coverage-threshhold";
	File xmlFile = new File("target/site/jacoco/jacoco.xml");
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
		Integer coverageActual = getCoverageActual(xmlFile);
		if (coverageActual != null && coverageThreshhold != null
				&& coverageActual < coverageThreshhold) {
			fail("Coverage is less than the threshhold of "
					+ coverageThreshhold + ".");
		}
	}

	private Integer parseThreshhold() {
		File propertyFile = new File("build/build-project.properties");
		if (propertyFile.exists()) {
			return getCoverageThreshhold(propertyFile);
		}
		return null;
	}

	private Integer getCoverageThreshhold(File file) {
		Properties prop = new Properties();
		Integer threshhold = null;
		try {
			prop.load(new FileInputStream(file));
			if (prop.containsKey(COVERAGE_PROPERTY_KEY)) {
				threshhold = Integer.valueOf(prop
						.getProperty(COVERAGE_PROPERTY_KEY));
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

	protected static Integer getCoverageActual(File file) {
		if (file.exists()) {
			return parseJacocoXml(file);
		}
		return null;
	}

	private static Integer parseJacocoXml(File xmlFile) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			// we don't care about the report.dtd
			dBuilder.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId,
						String systemId) throws SAXException, IOException {
					return new InputSource(new StringReader(""));
				}
			});
			Document document = dBuilder.parse(xmlFile);
			document.getDocumentElement().normalize(); // recommended
			return getOverallCoverage(document);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Integer getOverallCoverage(Document document) {
		Node child = document.getFirstChild();
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			XPathExpression expr1  = xpath.compile("//report/counter[@type='INSTRUCTION']");
			NodeList nodes = (NodeList)expr1.evaluate(document, XPathConstants.NODESET);
	        calculateCoverage(nodes.item(0));
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}	
		return null;
	}
	
	public static void calculateCoverage(Node node) {
        System.out.println(node.getAttributes().getNamedItem("missed").getNodeValue());
        System.out.println(node.getAttributes().getNamedItem("covered").getNodeValue());
    }

}
