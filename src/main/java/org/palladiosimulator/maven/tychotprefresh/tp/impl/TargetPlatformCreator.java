package org.palladiosimulator.maven.tychotprefresh.tp.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.palladiosimulator.maven.tychotprefresh.tp.ITargetPlatformCreator;
import org.palladiosimulator.maven.tychotprefresh.tp.model.Location;
import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;
import org.palladiosimulator.maven.tychotprefresh.tp.parser.TargetPlatformParser;
import org.palladiosimulator.maven.tychotprefresh.util.TPCoordinates;
import org.xml.sax.SAXException;

@Component(role=ITargetPlatformCreator.class)
public class TargetPlatformCreator implements ITargetPlatformCreator, ArtefactCreationMixin {
	
	@Requirement
	private Logger log;

	@Requirement
	protected RepositorySystem repositorySystem;
	
	@Override
	public TargetPlatformFile createMergedTargetPlatform(Collection<String> targetPlatformsToConsider,
			Collection<String> targetPlatformFilters, ArtifactRepository localRepository,
			List<ArtifactRepository> remoteRepositories) throws MavenExecutionException {
		Collection<String> tpFilePaths = findTargetPlatformDefinitionFiles(targetPlatformsToConsider, localRepository, remoteRepositories);
		return createMergedTargetPlatformDefinition(tpFilePaths, targetPlatformFilters);
	}
	
	private Collection<String> findTargetPlatformDefinitionFiles(Collection<String> targetPlatformsToConsider, ArtifactRepository localRepository,
			List<ArtifactRepository> remoteRepositories) {

		Collection<String> resolvedTPPaths = targetPlatformsToConsider.stream().map(TPCoordinates::parse)
				.filter(Optional::isPresent).map(Optional::get).map(this::createTargetArtifact)
				.map(a -> resolveArtifact(a, localRepository, remoteRepositories))
				.filter(Optional::isPresent).map(Optional::get).map(Artifact::getFile).filter(Objects::nonNull)
				.map(File::getAbsolutePath).collect(Collectors.toList());

		Collection<String> givenTPPaths = targetPlatformsToConsider.stream()
				.filter(((Predicate<String>) TPCoordinates::isValid).negate()).collect(Collectors.toList());

		Collection<String> targetPlatformFilePaths = Stream.concat(resolvedTPPaths.stream(), givenTPPaths.stream())
				.collect(Collectors.toList());

		log.debug("Resolved target platform hints to paths:"
				+ targetPlatformFilePaths.stream().collect(Collectors.joining("\n\t", "\n\t", "")));

		return targetPlatformFilePaths;
	}
	
	private Artifact createTargetArtifact(TPCoordinates coordinates) {
		return createTargetArtifact(repositorySystem, coordinates);
	}
	
	private Optional<Artifact> resolveArtifact(Artifact artifact, ArtifactRepository localRepository,
			List<ArtifactRepository> remoteRepositories) {
		ArtifactResolutionRequest request = new ArtifactResolutionRequest();
		request.setArtifact(artifact);
		request.setLocalRepository(localRepository);
		request.setRemoteRepositories(remoteRepositories);
		ArtifactResolutionResult result = repositorySystem.resolve(request);
		if (!result.isSuccess()) {
			log.warn("Could not resolve " + artifact + ". Skipping.");
		}
		return result.getArtifacts().stream().findFirst();
	}
	
	private TargetPlatformFile createMergedTargetPlatformDefinition(Collection<String> tpFilePaths, Collection<String> targetPlatformFilters)
			throws MavenExecutionException {
		Collection<TargetPlatformFile> targetPlatformFiles = new ArrayList<>();
		for (String tpFilePath : tpFilePaths) {
			try {
				Optional<TargetPlatformFile> parsedTP = TargetPlatformParser.parse(new File(tpFilePath));
				parsedTP.ifPresent(targetPlatformFiles::add);
				if (!parsedTP.isPresent()) {
					log.warn("Invalid content in " + tpFilePath + ". Skipping entry.");
				}
			} catch (ParserConfigurationException | SAXException | IOException e) {
				log.warn("Error in processing " + tpFilePath + ". Skipping entry.", e);
			}
		}
		targetPlatformFiles = filterTargetPlatformFiles(targetPlatformFiles, targetPlatformFilters);
		return merge(targetPlatformFiles);
	}
	
	private Collection<TargetPlatformFile> filterTargetPlatformFiles(
			Collection<TargetPlatformFile> targetPlatformFiles, Collection<String> targetPlatformFilters) {
		return targetPlatformFiles.stream().map(tpFile -> filterTargetPlatformFile(tpFile, targetPlatformFilters)).collect(Collectors.toList());
	}
	
	private TargetPlatformFile filterTargetPlatformFile(TargetPlatformFile tpFile, Collection<String> targetPlatformFilters) {
		Collection<Location> filteredLocations = new ArrayList<>();
		for (Location location : tpFile.getLocations()) {
			if (StringUtils.isBlank(location.getFilter())) {
				filteredLocations.add(new Location(location.getRepositoryLocation(), location.getUnits()));
			} else if (targetPlatformFilters.contains(location.getFilter())) {
				filteredLocations.add(new Location(location.getRepositoryLocation(), location.getUnits()));
			} else {
				log.debug("Filtered location entry " + location.getRepositoryLocation() + " with filter keyword " + location.getFilter());
			}
		}
		return new TargetPlatformFile(filteredLocations);
	}

	private static TargetPlatformFile merge(Collection<TargetPlatformFile> files) {
		Collection<Location> locations = files.stream().map(TargetPlatformFile::getLocations)
				.flatMap(Collection::stream).map(TargetPlatformCreator::normalizeURL)
				.collect(Collectors.toMap(Location::getRepositoryLocation, Location::getUnits,
						(c1, c2) -> Stream.concat(c1.stream(), c2.stream()).collect(Collectors.toSet())))
				.entrySet().stream().map(e -> new Location(e.getKey(), e.getValue())).collect(Collectors.toList());
		return new TargetPlatformFile(locations);
	}

	private static Location normalizeURL(Location location) {
		try {
			String normalizedURL = new URL(location.getRepositoryLocation()).toExternalForm();
			return new Location(normalizedURL, location.getUnits());
		} catch (MalformedURLException e) {
			return location;
		}
	}

}
