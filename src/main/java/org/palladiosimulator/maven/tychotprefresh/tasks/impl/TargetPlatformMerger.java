package org.palladiosimulator.maven.tychotprefresh.tasks.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.palladiosimulator.maven.tychotprefresh.tasks.TargetPlatformTaskBase;
import org.palladiosimulator.maven.tychotprefresh.tasks.TaskDependencies;
import org.palladiosimulator.maven.tychotprefresh.tp.model.Location;
import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;

public class TargetPlatformMerger extends TargetPlatformTaskBase {

	public TargetPlatformMerger(TaskDependencies dependencies) {
		super(dependencies, "Merging available target platform definitions.");
	}

	@Override
	public Collection<TargetPlatformFile> process(Collection<TargetPlatformFile> input) {
		Validate.notNull(input);
		return Arrays.asList(merge(input));
	}
	
	protected static TargetPlatformFile merge(Collection<TargetPlatformFile> files) {
		Collection<Location> locations = files.stream().map(TargetPlatformFile::getLocations)
				.flatMap(Collection::stream).map(TargetPlatformMerger::normalizeURL)
				.collect(Collectors.toMap(Location::getRepositoryLocation, Location::getUnits,
						(c1, c2) -> Stream.concat(c1.stream(), c2.stream()).collect(Collectors.toSet())))
				.entrySet().stream().map(e -> new Location(e.getKey(), e.getValue())).collect(Collectors.toList());
		return new TargetPlatformFile(locations);
	}

	protected static Location normalizeURL(Location location) {
		try {
			String normalizedURL = new URL(location.getRepositoryLocation()).toExternalForm();
			return new Location(normalizedURL, location.getUnits());
		} catch (MalformedURLException e) {
			return location;
		}
	}

}
