package org.palladiosimulator.maven.tychotprefresh.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.maven.project.MavenProject;
import org.palladiosimulator.maven.tychotprefresh.util.TPCoordinates;

public class PropertyGatherer {

	private static final String PROPERTY_PREFIX = "org.palladiosimulator.maven.tychotprefresh";

	private static final String PROPERTY_PREFIX_TP_LOCATIONS = String.format("%s.%s", PROPERTY_PREFIX, "tplocation");
	private static final String PROPERTY_PREFIX_FILTERS = String.format("%s.%s", PROPERTY_PREFIX, "filter");

	private static final String PROPERTY_PREFIX_TP_PROJECT = String.format("%s.%s", PROPERTY_PREFIX, "tpproject");
	private static final String PROPERTY_KEY_TP_PROJECT_GROUPID = String.format("%s.%s", PROPERTY_PREFIX_TP_PROJECT,
			"groupId");
	private static final String PROPERTY_KEY_TP_PROJECT_ARTIFACTID = String.format("%s.%s", PROPERTY_PREFIX_TP_PROJECT,
			"artifactId");
	private static final String PROPERTY_KEY_TP_PROJECT_VERSION = String.format("%s.%s", PROPERTY_PREFIX_TP_PROJECT,
			"version");
	private static final String PROPERTY_KEY_TP_PROJECT_CLASSIFIER = String.format("%s.%s", PROPERTY_PREFIX_TP_PROJECT,
			"classifier");
	private static final String PROPERTY_KEY_TP_PROJECT_TYPE = String.format("%s.%s", PROPERTY_PREFIX_TP_PROJECT,
			"type");
	private static final String PROPERTY_KEY_DISABLE = String.format("%s.%s", PROPERTY_PREFIX, "disable");
	private static final String PROPERTY_KEY_TP_COPY_DESTINATION = "targetPlatform.target";
	private static final String PROPERTY_DEFAULT_TP_COPY_DESTINATION = "tp.target";

	private final Collection<String> tpTargetLocations;
	private final Collection<String> tpFilters;
	private final TPCoordinates tpProjectCoordinates;
	private final String tpCopyDestinationFileName;
	private final boolean tpDisable;

	public PropertyGatherer(MavenProject project) {
		this.tpDisable = isDisabled(project);
		if (this.tpDisable) {
			this.tpTargetLocations = Collections.emptyList();
			this.tpFilters = Collections.emptyList();
			this.tpProjectCoordinates = new TPCoordinates(null, null, null, null, null);
			this.tpCopyDestinationFileName = null;
		} else {
			this.tpTargetLocations = readTPTargetLocations(project);
			this.tpFilters = readTPFilters(project);
			this.tpProjectCoordinates = readTPProjectCoordinates(project);	
			this.tpCopyDestinationFileName = readTPCopyDestinationFileName(project);
		}
	}

	public boolean isDisabled() {
		return tpDisable;
	}

	public Collection<String> getTpTargetLocations() {
		return tpTargetLocations;
	}

	public Collection<String> getTpFilters() {
		return tpFilters;
	}

	public TPCoordinates getTpProjectCoordinates() {
		return tpProjectCoordinates;
	}

	public String getTpCopyDestinationFileName() {
		return tpCopyDestinationFileName;
	}
	
	@Override
	public String toString() {
		return toSubString("TP Target Locations:", tpTargetLocations) + "\n"
				+ String.format("%s: %s", "TP Project Coordinates: ", tpProjectCoordinates);
	}

	private static String toSubString(String title, Collection<? extends Object> elements) {
		return String.format("%s\n\t%s", title, toSubString(elements));
	}

	private static String toSubString(Collection<? extends Object> elements) {
		return elements.stream().map(Object::toString).collect(Collectors.joining("\n\t"));
	}

	private static Collection<String> readTPTargetLocations(MavenProject project) {
		return getPropertiesWithPrefix(project, PROPERTY_PREFIX_TP_LOCATIONS);
	}

	private Collection<String> readTPFilters(MavenProject project) {
		return getPropertiesWithPrefix(project, PROPERTY_PREFIX_FILTERS);
	}
	
	private static boolean isDisabled(MavenProject project) {
		return Optional.ofNullable(readProperty(project, PROPERTY_KEY_DISABLE)).map(Boolean::valueOf).orElse(false);
	}

	private static TPCoordinates readTPProjectCoordinates(MavenProject project) {
		String groupId = readMandatoryProperty(project, PROPERTY_KEY_TP_PROJECT_GROUPID);
		String artifactId = readMandatoryProperty(project, PROPERTY_KEY_TP_PROJECT_ARTIFACTID);
		String version = readMandatoryProperty(project, PROPERTY_KEY_TP_PROJECT_VERSION);
		String classifier = readMandatoryProperty(project, PROPERTY_KEY_TP_PROJECT_CLASSIFIER);
		String type = readMandatoryProperty(project, PROPERTY_KEY_TP_PROJECT_TYPE);
		return new TPCoordinates(groupId, artifactId, version, classifier, type);
	}
	
	private String readTPCopyDestinationFileName(MavenProject project) {
		return Optional.ofNullable(readProperty(project, PROPERTY_KEY_TP_COPY_DESTINATION)).orElse(PROPERTY_DEFAULT_TP_COPY_DESTINATION);
	}

	private static String readMandatoryProperty(MavenProject project, String key) {
		if (!project.getProperties().containsKey(key)) {
			throw new IllegalStateException("The property " + key + " is mandatory, but not available.");
		}
		return readProperty(project, key);
	}
	
	private static String readProperty(MavenProject project, String key) {
		return project.getProperties().getProperty(key);
	}

	private static Collection<String> getPropertiesWithPrefix(MavenProject project, String prefix) {
		return project.getProperties().entrySet().stream().filter(e -> e.getKey() instanceof String)
				.filter(e -> ((String) e.getKey()).startsWith(prefix)).map(Entry::getValue)
				.filter(String.class::isInstance).map(String.class::cast).collect(Collectors.toSet());
	}

}
