package org.palladiosimulator.maven.tychotprefresh.tp.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.palladiosimulator.maven.tychotprefresh.tp.model.Location;
import org.palladiosimulator.maven.tychotprefresh.tp.model.Unit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LocationParser {

	public static Optional<Location> parse(Node node) {
		String repositoryLocation = null;
		Collection<Unit> units = new ArrayList<>();
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); ++i) {
			Optional<Element> currentElement = Optional.of(childNodes.item(i)).filter(Element.class::isInstance)
					.map(Element.class::cast);
			switch (currentElement.map(Element::getNodeName).orElse("")) {
			case "unit":
				currentElement.flatMap(UnitParser::parse).ifPresent(units::add);
				break;
			case "repository":
				repositoryLocation = currentElement.map(e -> e.getAttribute("location")).orElse(null);
				break;
			default:
				continue;
			}
		}
		if (StringUtils.isBlank(repositoryLocation) || units.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(new Location(repositoryLocation, units));
	}
	
	public static Node create(Document doc, Location location) {
		if (StringUtils.isBlank(location.getRepositoryLocation()) || location.getUnits().isEmpty()) {
			throw new IllegalArgumentException("Locations require a repository location and at least one unit.");
		}
		
		Element element = doc.createElement("location");
		element.setAttribute("includeAllPlatforms", "true");
		element.setAttribute("includeConfigurePhase", "true");
		element.setAttribute("includeMode", "slicer");
		element.setAttribute("includeSource", "false");
		element.setAttribute("type", "InstallableUnit");

		Element repository = doc.createElement("repository");
		repository.setAttribute("location", location.getRepositoryLocation());
		element.appendChild(repository);
		
		location.getUnits().stream().map(unit -> UnitParser.create(doc, unit)).forEach(element::appendChild);
		
		return element;
	}

}
