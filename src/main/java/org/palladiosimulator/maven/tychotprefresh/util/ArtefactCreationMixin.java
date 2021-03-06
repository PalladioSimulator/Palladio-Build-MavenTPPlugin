package org.palladiosimulator.maven.tychotprefresh.util;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.repository.RepositorySystem;

public interface ArtefactCreationMixin {

	default Artifact createTargetArtifact(RepositorySystem repositorySystem, TPCoordinates coordinates) {
		return repositorySystem.createArtifactWithClassifier(coordinates.getGroupId(), coordinates.getArtifactId(),
				coordinates.getVersion(), coordinates.getType(), coordinates.getClassifier());
	}
	
}