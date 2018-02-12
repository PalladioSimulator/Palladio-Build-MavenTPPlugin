package org.palladiosimulator.maven.tychotprefresh.tp.model;

import java.util.ArrayList;
import java.util.Collection;

public class TargetPlatformFile {

	private Collection<Location> locations = new ArrayList<>();

	public TargetPlatformFile(Collection<Location> locations) {
		super();
		this.locations = locations;
	}

	public Collection<Location> getLocations() {
		return locations;
	}

	public void setLocations(Collection<Location> locations) {
		this.locations = locations;
	}

}
