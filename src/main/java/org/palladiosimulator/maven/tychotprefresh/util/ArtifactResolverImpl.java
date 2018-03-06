package org.palladiosimulator.maven.tychotprefresh.util;

import java.util.List;
import java.util.Optional;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.palladiosimulator.maven.tychotprefresh.tp.impl.ArtefactCreationMixin;

@Component(role = IArtifactResolver.class)
public class ArtifactResolverImpl implements IArtifactResolver, ArtefactCreationMixin {

	@Requirement
	protected RepositorySystem repositorySystem;

	@Override
	public Optional<Artifact> resolveArtifact(TPCoordinates artifactCoordinates, ArtifactRepository localRepository,
			List<ArtifactRepository> remoteRepositories) {

		Artifact artifact = createTargetArtifact(repositorySystem, artifactCoordinates);

		ArtifactResolutionRequest request = new ArtifactResolutionRequest();
		request.setArtifact(artifact);
		request.setLocalRepository(localRepository);
		request.setRemoteRepositories(remoteRepositories);
		ArtifactResolutionResult result = repositorySystem.resolve(request);
		return result.getArtifacts().stream().findFirst();
	}

}
