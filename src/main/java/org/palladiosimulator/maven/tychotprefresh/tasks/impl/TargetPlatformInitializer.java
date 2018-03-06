package org.palladiosimulator.maven.tychotprefresh.tasks.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.Validate;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.palladiosimulator.maven.tychotprefresh.tasks.TargetPlatformTaskBase;
import org.palladiosimulator.maven.tychotprefresh.tasks.TaskDependencies;
import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;
import org.palladiosimulator.maven.tychotprefresh.tp.parser.TargetPlatformParser;
import org.palladiosimulator.maven.tychotprefresh.util.TPCoordinates;
import org.xml.sax.SAXException;

public class TargetPlatformInitializer extends TargetPlatformTaskBase {

	private final Collection<String> targetPlatformsToConsider;

	public TargetPlatformInitializer(TaskDependencies dependencies, Collection<String> targetPlatformsToConsider) {
		super(dependencies, "Collecting target platform definitions.");
		this.targetPlatformsToConsider = Collections.unmodifiableCollection(targetPlatformsToConsider);
	}

	@Override
	public Collection<TargetPlatformFile> process(Collection<TargetPlatformFile> input) {
		Validate.notNull(input);
		Validate.isTrue(input.isEmpty());

		Collection<String> tpFilePaths = findTargetPlatformDefinitionFiles(targetPlatformsToConsider,
				getDependencies().getLocalRepository(), getDependencies().getRemoteRepositories());

		return parseTargetPlatformDefinitionFiles(tpFilePaths);
	}

	protected Collection<String> findTargetPlatformDefinitionFiles(Collection<String> targetPlatformsToConsider,
			ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories) {

		Collection<String> resolvedTPPaths = targetPlatformsToConsider.stream().map(TPCoordinates::parse)
				.filter(Optional::isPresent).map(Optional::get)
				.map(coordinates -> resolveArtifact(coordinates, localRepository, remoteRepositories))
				.filter(Optional::isPresent).map(Optional::get).map(Artifact::getFile).filter(Objects::nonNull)
				.map(File::getAbsolutePath).collect(Collectors.toList());

		Collection<String> givenTPPaths = targetPlatformsToConsider.stream()
				.filter(((Predicate<String>) TPCoordinates::isValid).negate()).collect(Collectors.toList());

		Collection<String> targetPlatformFilePaths = Stream.concat(resolvedTPPaths.stream(), givenTPPaths.stream())
				.collect(Collectors.toList());

		getLog().debug("Resolved target platform hints to paths:"
				+ targetPlatformFilePaths.stream().collect(Collectors.joining("\n\t", "\n\t", "")));

		return targetPlatformFilePaths;
	}

	protected Optional<Artifact> resolveArtifact(TPCoordinates coordinates, ArtifactRepository localRepository,
			List<ArtifactRepository> remoteRepositories) {
		Optional<Artifact> result = getDependencies().getArtifactResolver().resolveArtifact(coordinates, localRepository,
				remoteRepositories);
		if (!result.isPresent()) {
			getLog().warn("Could not resolve " + coordinates + ". Skipping.");
		}
		return result;
	}
	
	protected Collection<TargetPlatformFile> parseTargetPlatformDefinitionFiles(Collection<String> tpFilePaths) {
		Collection<TargetPlatformFile> targetPlatformFiles = new ArrayList<>();
		for (String tpFilePath : tpFilePaths) {
			try {
				Optional<TargetPlatformFile> parsedTP = TargetPlatformParser.parse(new File(tpFilePath));
				parsedTP.ifPresent(targetPlatformFiles::add);
				if (!parsedTP.isPresent()) {
					getLog().warn("Invalid content in " + tpFilePath + ". Skipping entry.");
				}
			} catch (ParserConfigurationException | SAXException | IOException e) {
				getLog().warn("Error in processing " + tpFilePath + ". Skipping entry.", e);
			}
		}
		return targetPlatformFiles;
	}
	
}
