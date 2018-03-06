package org.palladiosimulator.maven.tychotprefresh.tp;

import java.util.Collection;
import java.util.List;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;

public interface ITargetPlatformCreator {

	TargetPlatformFile createMergedTargetPlatform(Collection<String> targetPlatformsToConsider, Collection<String> targetPlatformFilters, ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories) throws MavenExecutionException;

}
