package org.palladiosimulator.maven.tychotprefresh.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Test;

public class TPCoordinatesTest {
	
	@Test
	public void testValidReturnsTrueOnValidEntry() {
		assertThat(TPCoordinates.isValid("a:b:c:d"), is(true));
	}
	
	@Test
	public void testParseWorksOnValidEntry() {
		TPCoordinates expected = new TPCoordinates("a", "b", "c", "d");
		TPCoordinates actual = TPCoordinates.parse("a:b:c:d").get();
		assertThat(actual, is(equalTo(expected)));
	}
	
	@Test
	public void testValidReturnsFalseOnInvalidEntry() {
		assertThat(TPCoordinates.isValid("invalid"), is(false));
	}
	
	@Test
	public void testParseReturnsEmptyOptionalOnInvalidEntry() {
		assertThat(TPCoordinates.parse("invalid"), is(equalTo(Optional.empty())));
	}

}
