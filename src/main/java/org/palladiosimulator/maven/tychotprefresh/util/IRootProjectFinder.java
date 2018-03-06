package org.palladiosimulator.maven.tychotprefresh.util;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

public interface IRootProjectFinder {

	MavenProject findRootProject(MavenSession session) throws MavenExecutionException;

}
