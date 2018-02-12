package org.palladiosimulator.maven.tychotprefresh.tp.parser;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.palladiosimulator.maven.tychotprefresh.tp.model.Location;
import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;

public class TargetPlatformParser {

	public static Optional<TargetPlatformFile> parse(File file)
			throws SAXException, IOException, ParserConfigurationException {
		Collection<Location> locations = new ArrayList<>();
		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = dBuilder.parse(file);
		NodeList locationNodes = doc.getElementsByTagName("location");
		for (int i = 0; i < locationNodes.getLength(); ++i) {
			Optional.of(locationNodes.item(i)).filter(Element.class::isInstance).map(Element.class::cast)
					.flatMap(LocationParser::parse).ifPresent(locations::add);
		}
		if (locations.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(new TargetPlatformFile(locations));
	}

	public static void serialize(TargetPlatformFile tpFile, File file)
			throws ParserConfigurationException, TransformerException, IOException {
		FileUtils.write(file, serialize(tpFile), StandardCharsets.UTF_8);
	}

	public static String serialize(TargetPlatformFile tpFile)
			throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		ProcessingInstruction pdeInstruction = doc.createProcessingInstruction("pde", "version=\"3.8\"");
		doc.appendChild(pdeInstruction);

		Element target = doc.createElement("target");
		doc.appendChild(target);
		target.setAttribute("name", "temporary-tp");

		Element locations = doc.createElement("locations");
		target.appendChild(locations);

		tpFile.getLocations().stream().map(l -> LocationParser.create(doc, l)).forEach(locations::appendChild);

		DOMSource domSource = new DOMSource(doc);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.transform(domSource, result);
		return writer.toString();
	}
}
