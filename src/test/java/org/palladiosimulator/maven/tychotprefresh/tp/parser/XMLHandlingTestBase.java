package org.palladiosimulator.maven.tychotprefresh.tp.parser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.w3c.dom.Document;

public abstract class XMLHandlingTestBase {
	
	protected Document doc;

	@Before
	public void setup() throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		this.doc = builder.newDocument();

	}
	
}
