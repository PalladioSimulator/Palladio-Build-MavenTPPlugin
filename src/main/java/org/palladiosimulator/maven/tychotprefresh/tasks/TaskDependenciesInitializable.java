package org.palladiosimulator.maven.tychotprefresh.tasks;

import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;

public interface TaskDependenciesInitializable extends TaskDependencies {

	void setRemoteRepositories(List<ArtifactRepository> remoteRepositories);

	void setLocalRepository(ArtifactRepository localRepository);
	
}
