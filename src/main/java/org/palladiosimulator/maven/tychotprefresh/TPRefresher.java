package org.palladiosimulator.maven.tychotprefresh;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.palladiosimulator.maven.tychotprefresh.tp.ITargetPlatformAttacher;
import org.palladiosimulator.maven.tychotprefresh.tp.ITargetPlatformCreator;
import org.palladiosimulator.maven.tychotprefresh.tp.ITargetPlatformVersionUpdater;
import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;

@Component(role = TPRefresher.class)
public class TPRefresher {

	@Requirement
	protected Logger log;

	@Requirement
	protected ITargetPlatformCreator targetPlatformCreator;

	@Requirement
	protected ITargetPlatformVersionUpdater targetPlatformVersionUpdater;

	@Requirement
	protected ITargetPlatformAttacher targetPlatformAttacher;

	public void performRefresh(MavenSession session, MavenProject rootProject) throws MavenExecutionException {
		log.info("Starting target platform refresh for root package " + rootProject.getId());

		PropertyGatherer properties = new PropertyGatherer(rootProject);
		log.info("Using following configuration for target platform refresh:\n" + properties);

		log.info("Merging available target platform definitions.");
		TargetPlatformFile mergedTargetPlatform = targetPlatformCreator.createMergedTargetPlatform(
				properties.getTpTargetLocations(), properties.getTpFilters(), session.getLocalRepository(),
				rootProject.getRemoteArtifactRepositories());

		log.info("Updating target platform definitions.");
		TargetPlatformFile refreshedTP = targetPlatformVersionUpdater.updateArtifactVersions(mergedTargetPlatform,
				properties.getTpUpdateLocations());

		log.info("Attaching target platform definition to build.");
		targetPlatformAttacher.attachTargetPlatform(session, refreshedTP, properties.getTpProjectCoordinates());

		log.info("Finished target platform preparation.");
	}

}
