package org.palladiosimulator.maven.tychotprefresh.tp.model;

import java.util.Collection;

public class Location {

	private String repositoryLocation;
	private Collection<Unit> units;
	private String filter;
	private boolean refresh;

	public Location(String repositoryLocation, Collection<Unit> units) {
		this(repositoryLocation, null, false, units);
	}
	
	public Location(Location location) {
		this(location.repositoryLocation, location.filter, location.refresh, location.units);
	}
	
	public Location(String repositoryLocation, String filter, boolean refresh, Collection<Unit> units) {
		super();
		this.repositoryLocation = repositoryLocation;
		this.filter = filter;
		this.refresh = refresh;
		this.units = units;
	}

	public String getRepositoryLocation() {
		return repositoryLocation;
	}

	public void setRepositoryLocation(String repositoryLocation) {
		this.repositoryLocation = repositoryLocation;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public boolean isRefresh() {
		return refresh;
	}

	public void setRefresh(boolean refresh) {
		this.refresh = refresh;
	}

	public Collection<Unit> getUnits() {
		return units;
	}

	public void setUnits(Collection<Unit> units) {
		this.units = units;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((repositoryLocation == null) ? 0 : repositoryLocation.hashCode());
		result = prime * result + ((units == null) ? 0 : units.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (repositoryLocation == null) {
			if (other.repositoryLocation != null)
				return false;
		} else if (!repositoryLocation.equals(other.repositoryLocation))
			return false;
		if (units == null) {
			if (other.units != null)
				return false;
		} else if (!units.equals(other.units))
			return false;
		return true;
	}
	
}
