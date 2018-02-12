package org.palladiosimulator.maven.tychotprefresh.tp;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;
import org.palladiosimulator.maven.tychotprefresh.util.TPCoordinates;

public interface ITargetPlatformAttacher {

	void attachTargetPlatform(MavenSession session, TargetPlatformFile targetPlatform, TPCoordinates coordinates)
			throws MavenExecutionException;

}
