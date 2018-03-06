package org.palladiosimulator.maven.tychotprefresh.util;

import java.util.List;
import java.util.Optional;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;

public interface IArtifactResolver {

	Optional<Artifact> resolveArtifact(TPCoordinates artifactCoordinates, ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories);

}
