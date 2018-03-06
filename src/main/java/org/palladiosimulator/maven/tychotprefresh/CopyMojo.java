package org.palladiosimulator.maven.tychotprefresh;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.palladiosimulator.maven.tychotprefresh.util.IArtifactResolver;
import org.palladiosimulator.maven.tychotprefresh.util.IRootProjectFinder;
import org.palladiosimulator.maven.tychotprefresh.util.TPCoordinates;

@Mojo(name = "copy", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresProject = true)
public class CopyMojo extends AbstractMojo {

	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
	private String buildDirectory;

	@Parameter(defaultValue = "targetPlatform.target", required = true)
	private String destinationFileName;

	@Component
	private IRootProjectFinder rootProjectFinder;

	@Component
	private IArtifactResolver artifactResolver;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			if (!project.equals(rootProjectFinder.findRootProject(session))) {
				getLog().info("No target platform definition available in this project.");
				return;
			}
		} catch (MavenExecutionException e) {
			throw new MojoExecutionException("Unable to determine root project.", e);
		}
		
		getLog().info("Copying target platform definition.");

		PropertyGatherer properties = new PropertyGatherer(project);
		TPCoordinates tpCoordinates = properties.getTpProjectCoordinates();
		Optional<Artifact> resolvedTP = artifactResolver.resolveArtifact(tpCoordinates, session.getLocalRepository(),
				project.getRemoteArtifactRepositories());
		if (!resolvedTP.isPresent()) {
			resolvedTP = session.getProjects().stream().map(MavenProject::getAttachedArtifacts)
					.flatMap(Collection::stream).filter(a -> match(tpCoordinates, a)).findFirst();
		}

		if (!resolvedTP.isPresent()) {
			throw new MojoExecutionException("The target platform definition could not be found.");
		}

		File buildDir = new File(buildDirectory);
		File dstFile = new File(buildDir, destinationFileName);
		try {
			FileUtils.forceMkdir(buildDir);
			FileUtils.copyFile(resolvedTP.get().getFile(), dstFile);
		} catch (IOException e) {
			throw new MojoFailureException("Unable to write target platform file.", e);
		}

	}

	protected static boolean match(TPCoordinates coordinates, Artifact artifact) {
		return Objects.equals(coordinates.getArtifactId(), artifact.getArtifactId())
				&& Objects.equals(coordinates.getGroupId(), artifact.getGroupId())
				&& Objects.equals(coordinates.getVersion(), artifact.getVersion())
				&& Objects.equals(coordinates.getClassifier(), artifact.getClassifier())
				&& Objects.equals(coordinates.getType(), artifact.getType());
	}

}
