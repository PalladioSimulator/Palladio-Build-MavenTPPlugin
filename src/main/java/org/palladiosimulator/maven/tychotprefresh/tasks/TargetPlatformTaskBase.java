package org.palladiosimulator.maven.tychotprefresh.tasks;

import org.codehaus.plexus.logging.Logger;

public abstract class TargetPlatformTaskBase implements TargetPlatformTask {

	private final TaskDependencies dependencies;
	private final String description;
	
	public TargetPlatformTaskBase(TaskDependencies dependencies, String description) {
		this.dependencies = dependencies;
		this.description = description;
	}

	protected TaskDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	public Logger getLog() {
		return dependencies.getLog();
	}
	
}
