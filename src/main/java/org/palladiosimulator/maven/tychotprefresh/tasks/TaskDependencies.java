package org.palladiosimulator.maven.tychotprefresh.tasks;

import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.logging.Logger;
import org.palladiosimulator.maven.tychotprefresh.util.IArtifactResolver;

public interface TaskDependencies {

	RepositorySystem getRepositorySystem();

	IArtifactResolver getArtifactResolver();

	List<ArtifactRepository> getRemoteRepositories();

	ArtifactRepository getLocalRepository();

	MavenSession getMavenSession();

	Logger getLog();

}
