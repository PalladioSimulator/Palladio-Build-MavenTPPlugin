package org.palladiosimulator.maven.tychotprefresh.tp.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Test;
import org.palladiosimulator.maven.tychotprefresh.tp.model.Unit;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class UnitParserTest extends XMLHandlingTestBase {
	
	@Test
	public void testParseValidElement() {
		Unit expected = new Unit("abc", "def");

		Element unit = doc.createElement("unit");
		unit.setAttribute("id", expected.getId());
		unit.setAttribute("version", expected.getVersion());
		
		Unit actual = UnitParser.parse(unit).get();
		
		assertThat(actual, is(equalTo(expected)));
	}
	
	@Test
	public void testParseReturnsEmptyOnElementWithoutId() {
		Element unit = doc.createElement("unit");
		unit.setAttribute("version", "abc");
		
		Optional<Unit> actual = UnitParser.parse(unit);
		
		assertThat(actual, is(equalTo(Optional.empty())));
	}
	
	@Test
	public void testParseReturnsEmptyOnElementWithoutVersion() {
		Element unit = doc.createElement("unit");
		unit.setAttribute("id", "abc");
		
		Optional<Unit> actual = UnitParser.parse(unit);
		
		assertThat(actual, is(equalTo(Optional.empty())));
	}
	
	@Test
	public void testSerializeValidElement() {
		Unit input = new Unit("abc", "def");

		Node actual = UnitParser.create(doc, input);
		
		assertThat(actual, is(instanceOf(Element.class)));
		Element actualElement = (Element)actual;
		assertThat(actualElement.getChildNodes().getLength(), is(0));
		assertThat(actualElement.getAttributes().getLength(), is(2));
		assertThat(actualElement.getAttribute("id"), is(equalTo(input.getId())));
		assertThat(actualElement.getAttribute("version"), is(equalTo(input.getVersion())));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSerializeInvalidElement() {
		Unit invalidInput = new Unit("", "");
		
		UnitParser.create(doc, invalidInput);
	}

}
