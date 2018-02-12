package org.palladiosimulator.maven.tychotprefresh;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
import org.palladiosimulator.maven.tychotprefresh.tp.parser.TargetPlatformParser;

@Component(role=TPRefresher.class)
public class TPRefresher {

	@Requirement
	protected Logger log;
	
	@Requirement
	protected ITargetPlatformCreator targetPlatformCreator;
	
	@Requirement
	protected ITargetPlatformVersionUpdater targetPlatformVersionUpdater;
	
	@Requirement
	protected ITargetPlatformAttacher targetPlatformAttacher;
	
	public String simulateRefreshToString(MavenSession session, MavenProject rootProject) throws MavenExecutionException {
		TargetPlatformFile refreshedTP = getRefreshedTP(session, rootProject, false);
		try {
			return TargetPlatformParser.serialize(refreshedTP);
		} catch (ParserConfigurationException | TransformerException e) {
			throw new MavenExecutionException("Failed to serialize target platform.", e);
		}
	}
	
	public void performRefresh(MavenSession session, MavenProject rootProject) throws MavenExecutionException {
		getRefreshedTP(session, rootProject, true);
	}
	
	private TargetPlatformFile getRefreshedTP(MavenSession session, MavenProject project, boolean attach)
			throws MavenExecutionException {
		log.info("Starting target platform refresh for root package " + project.getId());

		PropertyGatherer properties = new PropertyGatherer(project);
		log.info("Using following configuration for target platform refresh:\n" + properties);

		log.info("Merging available target platform definitions.");
		TargetPlatformFile mergedTargetPlatform = targetPlatformCreator.createMergedTargetPlatform(
				properties.getTpTargetLocations(), session.getLocalRepository(),
				project.getRemoteArtifactRepositories());

		log.info("Updating target platform definitions.");
		TargetPlatformFile refreshedTP = targetPlatformVersionUpdater.updateArtifactVersions(mergedTargetPlatform,
				properties.getTpUpdateLocations());
		
		if (attach) {
			log.info("Attaching target platform definition to build.");
			targetPlatformAttacher.attachTargetPlatform(session, refreshedTP, properties.getTpProjectCoordinates());			
		}

		log.info("Finished target platform preparation.");
		return refreshedTP;
	}
	
}
