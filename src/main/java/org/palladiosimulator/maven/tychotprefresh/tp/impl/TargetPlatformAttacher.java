package org.palladiosimulator.maven.tychotprefresh.tp.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.palladiosimulator.maven.tychotprefresh.tp.ITargetPlatformAttacher;
import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;
import org.palladiosimulator.maven.tychotprefresh.tp.parser.TargetPlatformParser;
import org.palladiosimulator.maven.tychotprefresh.util.TPCoordinates;

@Component(role = ITargetPlatformAttacher.class)
public class TargetPlatformAttacher implements ITargetPlatformAttacher, ArtefactCreationMixin {

	private static final String PACKAGING_TYPE_ECLIPSE_TARGET_DEFINITION = "eclipse-target-definition";

	@Requirement
	private Logger log;

	@Requirement
	protected RepositorySystem repositorySystem;

	@Override
	public void attachTargetPlatform(MavenSession session, TargetPlatformFile targetPlatform, TPCoordinates coordinates)
			throws MavenExecutionException {
		String mergedTargetPlatformContent = serializeTargetPlatformFile(targetPlatform);
		attachTargetPlatformDefinitionProject(session, mergedTargetPlatformContent, coordinates);
	}

	private static String serializeTargetPlatformFile(TargetPlatformFile mergedTargetPlatform)
			throws MavenExecutionException {
		try {
			return TargetPlatformParser.serialize(mergedTargetPlatform);
		} catch (TransformerException | ParserConfigurationException e) {
			throw new MavenExecutionException("Unable to serialize merged target platform.", e);
		}
	}

	private void attachTargetPlatformDefinitionProject(MavenSession session, String targetPlatformDefinitionContent,
			TPCoordinates coordinates) throws MavenExecutionException {
		log.info("Creating virtual project for merged target platform.");
		try {
			MavenProject mp = new MavenProject();
			mp.setGroupId(coordinates.getGroupId());
			mp.setArtifactId(coordinates.getArtifactId());
			mp.setVersion(coordinates.getVersion());
			mp.setPackaging(PACKAGING_TYPE_ECLIPSE_TARGET_DEFINITION);

			Path tempDirPath = java.nio.file.Files.createTempDirectory("tp_tmp");
			File tempDir = tempDirPath.toFile();
			FileUtils.forceDeleteOnExit(tempDir);
			File pomFile = new File(tempDir, "pom.xml");
			mp.setFile(pomFile);

			File tpFile = new File(tempDir, coordinates.getClassifier() + "." + TARGET_PLATFORM_FILE_EXTENSION);
			FileUtils.write(tpFile, targetPlatformDefinitionContent, StandardCharsets.UTF_8);
			Artifact tpArtifact = createTargetArtifact(repositorySystem, coordinates);
			tpArtifact.setFile(tpFile);
			mp.addAttachedArtifact(tpArtifact);

			List<MavenProject> projects = new ArrayList<>(session.getProjects());
			projects.add(mp);
			session.setProjects(projects);
		} catch (IOException e) {
			throw new MavenExecutionException("failed to create temporary tp", e);
		}
	}

}
