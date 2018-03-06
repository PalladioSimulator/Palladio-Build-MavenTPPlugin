package org.palladiosimulator.maven.tychotprefresh.tasks.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.palladiosimulator.maven.tychotprefresh.tasks.TargetPlatformTaskBase;
import org.palladiosimulator.maven.tychotprefresh.tasks.TaskDependencies;
import org.palladiosimulator.maven.tychotprefresh.tasks.TaskExecutionException;
import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;
import org.palladiosimulator.maven.tychotprefresh.tp.parser.TargetPlatformParser;
import org.palladiosimulator.maven.tychotprefresh.util.ArtefactCreationMixin;
import org.palladiosimulator.maven.tychotprefresh.util.TPCoordinates;

public class TargetPlatformAttacher extends TargetPlatformTaskBase implements ArtefactCreationMixin {

	private static final String PACKAGING_TYPE_ECLIPSE_TARGET_DEFINITION = "eclipse-target-definition";
	private TPCoordinates projectCoordinates;
	
	public TargetPlatformAttacher(TaskDependencies dependencies, TPCoordinates projectCoordinates) {
		super(dependencies, "Attaching target platform definition to build.");
		this.projectCoordinates = projectCoordinates;
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
		getLog().info("Creating virtual project for merged target platform.");
		try {
			MavenProject mp = new MavenProject();
			mp.setGroupId(projectCoordinates.getGroupId());
			mp.setArtifactId(projectCoordinates.getArtifactId());
			mp.setVersion(projectCoordinates.getVersion());
			mp.setPackaging(PACKAGING_TYPE_ECLIPSE_TARGET_DEFINITION);

			Path tempDirPath = java.nio.file.Files.createTempDirectory("tp_tmp");
			File tempDir = tempDirPath.toFile();
			FileUtils.forceDeleteOnExit(tempDir);
			File pomFile = new File(tempDir, "pom.xml");
			mp.setFile(pomFile);

			File tpFile = new File(tempDir, projectCoordinates.getClassifier() + "." + projectCoordinates.getType());
			FileUtils.write(tpFile, targetPlatformDefinitionContent, StandardCharsets.UTF_8);
			Artifact tpArtifact = createTargetArtifact(getDependencies().getRepositorySystem(), projectCoordinates);
			tpArtifact.setFile(tpFile);
			mp.addAttachedArtifact(tpArtifact);

			MavenSession session = getDependencies().getMavenSession();
			List<MavenProject> projects = new ArrayList<>(session.getProjects());
			projects.add(mp);
			session.setProjects(projects);
		} catch (IOException e) {
			throw new IOException("failed to create temporary tp", e);
		}
	}
}
