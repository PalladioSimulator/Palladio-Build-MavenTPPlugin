package org.palladiosimulator.maven.tychotprefresh.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.palladiosimulator.maven.tychotprefresh.tasks.TargetPlatformTask;
import org.palladiosimulator.maven.tychotprefresh.tasks.TaskDependenciesInitializable;
import org.palladiosimulator.maven.tychotprefresh.tasks.TaskExecutionException;
import org.palladiosimulator.maven.tychotprefresh.tasks.impl.TargetPlatformAttacher;
import org.palladiosimulator.maven.tychotprefresh.tasks.impl.TargetPlatformFilter;
import org.palladiosimulator.maven.tychotprefresh.tasks.impl.TargetPlatformInitializer;
import org.palladiosimulator.maven.tychotprefresh.tasks.impl.TargetPlatformMerger;
import org.palladiosimulator.maven.tychotprefresh.tasks.impl.TargetPlatformUpdater;
import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;

@Component(role = TPRefresher.class)
public class TPRefresher {

	@Requirement
	protected Logger log;
	
	@Requirement
	protected TaskDependenciesInitializable dependencies;

	public void performRefresh(MavenSession session, MavenProject rootProject) throws MavenExecutionException {
		log.info("Starting target platform refresh for root package " + rootProject.getId());

		PropertyGatherer properties = new PropertyGatherer(rootProject);
		log.info("Using following configuration for target platform refresh:\n" + properties);
		
		List<TargetPlatformTask> taskSequence = createTasks(session, rootProject, properties);
		executeTasks(taskSequence);
		
		log.info("Finished target platform preparation.");
	}

	protected List<TargetPlatformTask> createTasks(MavenSession session, MavenProject rootProject,
			PropertyGatherer properties) {
		dependencies.setMavenSession(session);
		dependencies.setRemoteRepositories(rootProject.getRemoteArtifactRepositories());
		dependencies.setLocalRepository(session.getLocalRepository());
		
		List<TargetPlatformTask> taskSequence = new ArrayList<>();
		taskSequence.add(new TargetPlatformInitializer(dependencies, properties.getTpTargetLocations()));
		taskSequence.add(new TargetPlatformFilter(dependencies, properties.getTpFilters()));
		taskSequence.add(new TargetPlatformUpdater(dependencies));
		taskSequence.add(new TargetPlatformMerger(dependencies));
		taskSequence.add(new TargetPlatformAttacher(dependencies, properties.getTpProjectCoordinates()));
		return taskSequence;
	}
	
	protected void executeTasks(List<TargetPlatformTask> taskSequence) throws MavenExecutionException {
		try {
			Collection<TargetPlatformFile> tpFiles = Collections.emptyList();
			for (TargetPlatformTask task : taskSequence) {
				log.info(task.getDescription());
				tpFiles = task.process(tpFiles);
			}
		} catch (TaskExecutionException e) {
			throw new MavenExecutionException("Failed to execute a task.", e);
		}
	}

}
