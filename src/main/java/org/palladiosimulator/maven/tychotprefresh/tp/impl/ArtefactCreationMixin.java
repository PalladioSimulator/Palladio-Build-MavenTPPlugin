package org.palladiosimulator.maven.tychotprefresh.tp.impl;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.repository.RepositorySystem;
import org.palladiosimulator.maven.tychotprefresh.util.TPCoordinates;

public interface ArtefactCreationMixin {

	public static final String TARGET_PLATFORM_FILE_EXTENSION = "target";
	
	default Artifact createTargetArtifact(RepositorySystem repositorySystem, TPCoordinates coordinates) {
		return repositorySystem.createArtifactWithClassifier(coordinates.getGroupId(), coordinates.getArtifactId(),
				coordinates.getVersion(), TARGET_PLATFORM_FILE_EXTENSION, coordinates.getClassifier());
	}
	
}