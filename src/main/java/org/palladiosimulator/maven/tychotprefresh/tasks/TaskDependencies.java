package org.palladiosimulator.maven.tychotprefresh.tasks;

import java.util.List;

import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.logging.Logger;
import org.palladiosimulator.maven.tychotprefresh.util.IArtifactResolver;

public interface TaskDependencies {

	IArtifactResolver getArtifactResolver();

	List<ArtifactRepository> getRemoteRepositories();

	ArtifactRepository getLocalRepository();

	Logger getLog();
	
	ArtifactInstaller getArtifactInstaller();

}
