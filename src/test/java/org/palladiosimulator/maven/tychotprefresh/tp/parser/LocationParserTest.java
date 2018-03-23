package org.palladiosimulator.maven.tychotprefresh.tp.parser;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;
import org.palladiosimulator.maven.tychotprefresh.tp.model.Location;
import org.palladiosimulator.maven.tychotprefresh.tp.model.Unit;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class LocationParserTest extends XMLHandlingTestBase {

	@Test
	public void testParseValidElement() {
		Location expected = new Location("http://www.example.org", Arrays.asList(new Unit("abc", "def")));
		
		Element location = doc.createElement("location");
		Element repository = doc.createElement("repository");
		location.appendChild(repository);
		repository.setAttribute("location", expected.getRepositoryLocation());
		Element unit = doc.createElement("unit");
		location.appendChild(unit);
		unit.setAttribute("id", expected.getUnits().iterator().next().getId());
		unit.setAttribute("version", expected.getUnits().iterator().next().getVersion());
		
		Location actual = LocationParser.parse(location).get();
		
		assertThat(actual, is(equalTo(expected)));
	}
	
	@Test
	public void testParseFilteredElement() {
		Location expected = new Location("http://www.example.org", "test", true, Arrays.asList(new Unit("abc", "def")));
		
		Element location = doc.createElement("location");
		location.setAttribute("filter", "test");
		location.setAttribute("refresh", "true");
		Element repository = doc.createElement("repository");
		location.appendChild(repository);
		repository.setAttribute("location", expected.getRepositoryLocation());
		Element unit = doc.createElement("unit");
		location.appendChild(unit);
		unit.setAttribute("id", expected.getUnits().iterator().next().getId());
		unit.setAttribute("version", expected.getUnits().iterator().next().getVersion());
		
		Location actual = LocationParser.parse(location).get();
		
		assertThat(actual, is(equalTo(expected)));
	}
	
	@Test
	public void testParseReturnsEmptyForRepositoryMissingElement() {
		Element location = doc.createElement("location");
		Element repository = doc.createElement("repository");
		location.appendChild(repository);
		repository.setAttribute("location", "");
		Element unit = doc.createElement("unit");
		location.appendChild(unit);
		unit.setAttribute("id", "a");
		unit.setAttribute("version", "b");
		
		Optional<Location> actual = LocationParser.parse(location);
		
		assertThat(actual, is(equalTo(Optional.empty())));
	}
	
	@Test
	public void testParseReturnsEmptyForUnitMissingElement() {
		Element location = doc.createElement("location");
		Element repository = doc.createElement("repository");
		location.appendChild(repository);
		repository.setAttribute("location", "http://www.example.org");
		
		Optional<Location> actual = LocationParser.parse(location);
		
		assertThat(actual, is(equalTo(Optional.empty())));
	}
	
	@Test
	public void testSerializeValidElement() {
		Location input = new Location("http://www.example.org", "test", true, Arrays.asList(new Unit("abc", "def")));
		Node actual = LocationParser.create(doc, input);
		
		assertThat(actual, is(instanceOf(Element.class)));
		Element actualElement = (Element)actual;
		assertThat(actual.getAttributes().getLength(), is (5));
		assertThat(actualElement.getAttribute("includeAllPlatforms"), is(equalTo("false")));
		assertThat(actualElement.getAttribute("includeConfigurePhase"), is(equalTo("true")));
		assertThat(actualElement.getAttribute("includeMode"), is(equalTo("planner")));
		assertThat(actualElement.getAttribute("includeSource"), is(equalTo("false")));
		assertThat(actualElement.getAttribute("type"), is(equalTo("InstallableUnit")));
		assertThat(actualElement.getAttribute("filter"), isEmptyOrNullString());
		assertThat(actual.getChildNodes().getLength(), is(2));
		for (int i = 0; i < actual.getChildNodes().getLength(); ++i) {
			Element currentElement = (Element)actual.getChildNodes().item(i);
			assertThat(currentElement.getChildNodes().getLength(), is(equalTo(0)));
			if (currentElement.getNodeName().equals("repository")) {
				assertThat(currentElement.getAttributes().getLength(), is(equalTo(1)));
				assertThat(currentElement.getAttribute("location"), is(equalTo(input.getRepositoryLocation())));
			} else if (currentElement.getNodeName().equals("unit")) {
				assertThat(currentElement.getAttributes().getLength(), is(equalTo(2)));
				assertThat(currentElement.getAttribute("id"), is(equalTo(input.getUnits().iterator().next().getId())));
				assertThat(currentElement.getAttribute("version"), is(equalTo(input.getUnits().iterator().next().getVersion())));
			} else {
				fail("Unexpected child element type.");
			}
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSerializeThrowsExceptionForRepositoryMissingElement() {
		Location input = new Location("", Arrays.asList(new Unit("abc", "def")));
		LocationParser.create(doc, input);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSerializeThrowsExceptionForUnitMissingElement() {
		Location input = new Location("http://www.example.org", Arrays.asList());
		LocationParser.create(doc, input);
	}
	
}
