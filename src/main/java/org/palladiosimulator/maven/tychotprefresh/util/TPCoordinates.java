package org.palladiosimulator.maven.tychotprefresh.util;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TPCoordinates {

	private static final String GROUP_ID = "groupId";
	private static final String ARTIFACT_ID = "artifactId";
	private static final String VERSION_ID = "version";
	private static final String CLASSIFIER_ID = "classifier";
	private static final Pattern PARSE_PATTERN = Pattern
			.compile(String.format("(?<%s>[^:]+):(?<%s>[^:]+):(?<%s>[^:]+):(?<%s>[^:]+)", GROUP_ID, ARTIFACT_ID,
					VERSION_ID, CLASSIFIER_ID));
	
	private final String groupId;
	private final String artifactId;
	private final String version;
	private final String classifier;

	public TPCoordinates(String groupId, String artifactId, String version, String classifier) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.classifier = classifier;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}

	public String getClassifier() {
		return classifier;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
		result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TPCoordinates other = (TPCoordinates) obj;
		if (artifactId == null) {
			if (other.artifactId != null)
				return false;
		} else if (!artifactId.equals(other.artifactId))
			return false;
		if (classifier == null) {
			if (other.classifier != null)
				return false;
		} else if (!classifier.equals(other.classifier))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s:%s:%s:%s", groupId, artifactId, version, classifier);
	}

	public static Optional<TPCoordinates> parse(String coordinateString) {
		Matcher matcher = PARSE_PATTERN.matcher(coordinateString);
		if (!matcher.matches()) {
			return Optional.empty();
		}
		return Optional.of(new TPCoordinates(matcher.group(GROUP_ID), matcher.group(ARTIFACT_ID),
				matcher.group(VERSION_ID), matcher.group(CLASSIFIER_ID)));
	}
	
	public static boolean isValid(String coordinateString) {
		return PARSE_PATTERN.matcher(coordinateString).matches();
	}

}
