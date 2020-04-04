package org.palladiosimulator.maven.tychotprefresh.tasks.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.palladiosimulator.maven.tychotprefresh.tasks.TargetPlatformTaskBase;
import org.palladiosimulator.maven.tychotprefresh.tasks.TaskDependencies;
import org.palladiosimulator.maven.tychotprefresh.tasks.TaskExecutionException;
import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;
import org.palladiosimulator.maven.tychotprefresh.tp.parser.TargetPlatformParser;
import org.palladiosimulator.maven.tychotprefresh.util.ArtefactCreationMixin;
import org.palladiosimulator.maven.tychotprefresh.util.TPCoordinates;

public class TargetPlatformAttacher extends TargetPlatformTaskBase implements ArtefactCreationMixin {

	private TPCoordinates projectCoordinates;
	private final File tmpDirectory;
	
	public TargetPlatformAttacher(TaskDependencies dependencies, TPCoordinates projectCoordinates, File tmpDirectory) {
		super(dependencies, "Attaching target platform definition to build.");
		this.projectCoordinates = projectCoordinates;
		this.tmpDirectory = tmpDirectory;
	}

	@Override
	public Collection<TargetPlatformFile> process(Collection<TargetPlatformFile> input) throws TaskExecutionException {
		Validate.notNull(input);
		Validate.isTrue(input.size() == 1);
		
		try {
			TargetPlatformFile targetPlatform = input.iterator().next();
			String mergedTargetPlatformContent = serializeTargetPlatformFile(targetPlatform);
			attachTargetPlatformDefinitionProject(mergedTargetPlatformContent);			
		} catch (IOException e) {
			throw new TaskExecutionException(e);
		}
		
		return Collections.emptyList();
	}

	private static String serializeTargetPlatformFile(TargetPlatformFile mergedTargetPlatform) throws IOException {
		try {
			return TargetPlatformParser.serialize(mergedTargetPlatform);
		} catch (TransformerException | ParserConfigurationException e) {
			throw new IOException("Unable to serialize merged target platform.", e);
		}
	}

	protected void attachTargetPlatformDefinitionProject(String targetPlatformDefinitionContent) throws IOException {
		getLog().info("Installing merged target platform into local repository.");
		try {
			File tpFile = new File(tmpDirectory, "merged-tp.target");
			FileUtils.write(tpFile, targetPlatformDefinitionContent, StandardCharsets.UTF_8);
			DefaultArtifactHandler artifactHandler = new DefaultArtifactHandler();
			artifactHandler.setExtension(projectCoordinates.getType());
			Artifact tpArtifact = new DefaultArtifact(projectCoordinates.getGroupId(), projectCoordinates.getArtifactId(), projectCoordinates.getVersion(), DefaultArtifact.SCOPE_COMPILE, projectCoordinates.getType(), projectCoordinates.getClassifier(), artifactHandler);
			getDependencies().getArtifactInstaller().install(tpFile, tpArtifact, getDependencies().getLocalRepository());
		} catch (IOException | ArtifactInstallationException e) {
			throw new IOException("failed to create temporary tp", e);
		}
	}
}
