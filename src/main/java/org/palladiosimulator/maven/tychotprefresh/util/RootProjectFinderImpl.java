package org.palladiosimulator.maven.tychotprefresh.util;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

@Component(role=IRootProjectFinder.class)
public class RootProjectFinderImpl implements IRootProjectFinder {

	@Override
	public MavenProject findRootProject(MavenSession session) throws MavenExecutionException {
		Collection<MavenProject> rootProjects = session.getAllProjects().stream()
				.filter(RootProjectFinderImpl::hasExactlyOneParent).collect(Collectors.toSet());
		if (rootProjects.size() != 1) {
			throw new MavenExecutionException(
					"Could not find root project (projects having exactly one transitive parent).",
					Optional.ofNullable(session.getCurrentProject()).map(MavenProject::getFile).orElse(null));
		}
		return rootProjects.iterator().next();
	}

	protected static boolean hasExactlyOneParent(MavenProject project) {
		MavenProject parentProject = project.getParent();
		return parentProject != null && parentProject.getParent() == null;
	}
}
