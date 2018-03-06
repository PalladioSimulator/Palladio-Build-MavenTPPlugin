package org.palladiosimulator.maven.tychotprefresh.tasks.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.palladiosimulator.maven.tychotprefresh.tasks.TargetPlatformTaskBase;
import org.palladiosimulator.maven.tychotprefresh.tasks.TaskDependencies;
import org.palladiosimulator.maven.tychotprefresh.tp.model.Location;
import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;

public class TargetPlatformFilter extends TargetPlatformTaskBase {

	private final Collection<String> filters;

	public TargetPlatformFilter(TaskDependencies dependencies, Collection<String> filters) {
		super(dependencies, "Filtering locations of target platform definitions.");
		this.filters = Collections.unmodifiableCollection(filters);
	}

	@Override
	public Collection<TargetPlatformFile> process(Collection<TargetPlatformFile> input) {
		Validate.notNull(input);

		return input.stream().map(this::filterTargetPlatformFile).collect(Collectors.toList());
	}

	protected TargetPlatformFile filterTargetPlatformFile(TargetPlatformFile tpFile) {
		Collection<Location> filteredLocations = new ArrayList<>();
		for (Location location : tpFile.getLocations()) {
			if (StringUtils.isBlank(location.getFilter())) {
				filteredLocations.add(new Location(location));
			} else if (filters.contains(location.getFilter())) {
				filteredLocations.add(new Location(location));
			} else {
				getLog().debug("Filtered location entry " + location.getRepositoryLocation() + " with filter keyword "
						+ location.getFilter());
			}
		}
		return new TargetPlatformFile(filteredLocations);
	}

}
