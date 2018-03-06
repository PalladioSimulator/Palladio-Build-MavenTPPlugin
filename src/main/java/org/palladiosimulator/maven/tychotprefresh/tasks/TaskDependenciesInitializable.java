package org.palladiosimulator.maven.tychotprefresh.tasks;

import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;

public interface TaskDependenciesInitializable extends TaskDependencies {

	void setMavenSession(MavenSession mavenSession);

	void setRemoteRepositories(List<ArtifactRepository> remoteRepositories);

	void setLocalRepository(ArtifactRepository localRepository);

}
