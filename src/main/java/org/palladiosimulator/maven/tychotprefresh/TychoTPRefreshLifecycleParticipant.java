package org.palladiosimulator.maven.tychotprefresh;

import java.util.Collection;
import java.util.HashSet;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.palladiosimulator.maven.tychotprefresh.util.IRootProjectFinder;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "TychoTPRefreshLifecycleParticipant")
public class TychoTPRefreshLifecycleParticipant extends AbstractMavenLifecycleParticipant {
	
	@Requirement
	protected IRootProjectFinder rootProjectFinder;
	
	@Requirement
	protected TPRefresher tpRefresher;

	private Collection<MavenProject> processedProjects = new HashSet<>();

	@Override
	public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
		MavenProject rootProject = rootProjectFinder.findRootProject(session);
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

}
