package org.palladiosimulator.maven.tychotprefresh;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "TychoTPRefreshLifecycleParticipant")
public class TychoTPRefreshLifecycleParticipant extends AbstractMavenLifecycleParticipant {
	
	@Requirement
	protected TPRefresher tpRefresher;

	private Collection<MavenProject> processedProjects = new HashSet<>();

	@Override
	public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
		MavenProject rootProject = findRootProject(session);
		if (!registerRootProjectProcessing(rootProject)) {
			return;
		}
		
		tpRefresher.performRefresh(session, rootProject);
		super.afterProjectsRead(session);
	}

	private boolean registerRootProjectProcessing(MavenProject rootProject) {
		if (processedProjects.contains(rootProject)) {
			return false;
		}
		processedProjects.add(rootProject);
		return true;
	}

	private static MavenProject findRootProject(MavenSession session) throws MavenExecutionException {
		Collection<MavenProject> rootProjects = session.getAllProjects().stream()
				.filter(TychoTPRefreshLifecycleParticipant::hasExactlyOneParent).collect(Collectors.toSet());
		if (rootProjects.size() != 1) {
			throw new MavenExecutionException(
					"Could not find root project (projects having exactly one transitive parent).",
					Optional.ofNullable(session.getCurrentProject()).map(MavenProject::getFile).orElse(null));
		}
		return rootProjects.iterator().next();
	}

	private static boolean hasExactlyOneParent(MavenProject project) {
		MavenProject parentProject = project.getParent();
		return parentProject != null && parentProject.getParent() == null;
	}


}
