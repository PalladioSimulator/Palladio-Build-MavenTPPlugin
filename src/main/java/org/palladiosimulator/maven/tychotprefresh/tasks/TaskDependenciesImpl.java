package org.palladiosimulator.maven.tychotprefresh.tasks;

import java.util.List;

import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.palladiosimulator.maven.tychotprefresh.util.IArtifactResolver;

@Component(role=TaskDependenciesInitializable.class)
public class TaskDependenciesImpl implements TaskDependenciesInitializable {

	@Requirement
	protected Logger log;
	
	@Requirement
	protected IArtifactResolver artifactResolver;
	
	@Requirement
	protected ArtifactInstaller artifactInstaller;
	
	protected ArtifactRepository localRepository;
	
	protected List<ArtifactRepository> remoteRepositories;

	@Override
	public ArtifactRepository getLocalRepository() {
		return localRepository;
	}

	@Override
	public void setLocalRepository(ArtifactRepository localRepository) {
		this.localRepository = localRepository;
	}

	@Override
	public List<ArtifactRepository> getRemoteRepositories() {
		return remoteRepositories;
	}

	@Override
	public void setRemoteRepositories(List<ArtifactRepository> remoteRepositories) {
		this.remoteRepositories = remoteRepositories;
	}

	@Override
	public IArtifactResolver getArtifactResolver() {
		return artifactResolver;
	}

	@Override
	public Logger getLog() {
		return log;
	}

	@Override
	public ArtifactInstaller getArtifactInstaller() {
		return artifactInstaller;
	}
	
}
