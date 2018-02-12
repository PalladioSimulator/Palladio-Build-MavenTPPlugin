package org.palladiosimulator.maven.tychotprefresh.tp;

import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.maven.MavenExecutionException;
import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;

public interface ITargetPlatformVersionUpdater {

	TargetPlatformFile updateArtifactVersions(TargetPlatformFile mergedTargetPlatform,
			Collection<Pattern> locationPatterns) throws MavenExecutionException;

}
