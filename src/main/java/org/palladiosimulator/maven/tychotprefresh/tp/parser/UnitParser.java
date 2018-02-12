package org.palladiosimulator.maven.tychotprefresh.tp.parser;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.palladiosimulator.maven.tychotprefresh.tp.model.Unit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class UnitParser {

	public static Optional<Unit> parse(Element node) {
		String id = node.getAttribute("id");
		String version = node.getAttribute("version");
		if (StringUtils.isBlank(id) || StringUtils.isBlank(version)) {
			return Optional.empty();
		}
		return Optional.of(new Unit(id, version));
	}

	public static Node create(Document doc, Unit unit) {
		if (StringUtils.isBlank(unit.getId()) || StringUtils.isBlank(unit.getVersion())) {
			throw new IllegalArgumentException("A unit must have an id and a version.");
		}
		Element element = doc.createElement("unit");
		element.setAttribute("id", unit.getId());
		element.setAttribute("version", unit.getVersion());
		return element;
	}
	
}
