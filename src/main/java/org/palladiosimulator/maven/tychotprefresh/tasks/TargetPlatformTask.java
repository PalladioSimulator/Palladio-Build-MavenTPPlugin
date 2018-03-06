package org.palladiosimulator.maven.tychotprefresh.tasks;

import java.util.Collection;

import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;

public interface TargetPlatformTask {

	Collection<TargetPlatformFile> process(Collection<TargetPlatformFile> input) throws TaskExecutionException;
	
	String getDescription();
	
}
