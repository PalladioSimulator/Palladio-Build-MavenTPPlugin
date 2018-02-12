package org.palladiosimulator.maven.tychotprefresh.tp.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.MavenExecutionException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.osgi.framework.Version;
import org.palladiosimulator.maven.tychotprefresh.tp.ITargetPlatformVersionUpdater;
import org.palladiosimulator.maven.tychotprefresh.tp.model.Location;
import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;
import org.palladiosimulator.maven.tychotprefresh.tp.model.Unit;
import org.palladiosimulator.maven.tychotprefresh.util.P2RepositoryReader;

@Component(role=ITargetPlatformVersionUpdater.class)
public class TargetPlatformVersionUpdater implements ITargetPlatformVersionUpdater {

	private static final String TP_FEATURE_ID_POSTFIX = ".feature.group";
	
	@Requirement
	private Logger log;
	
	@Override
	public TargetPlatformFile updateArtifactVersions(TargetPlatformFile mergedTargetPlatform,
			Collection<Pattern> locationPatterns) throws MavenExecutionException {

		Map<Location, Location> locationReplacements = new HashMap<>();
		Collection<Location> locationsToUpdate = mergedTargetPlatform.getLocations().stream()
				.filter(location -> locationPatterns.stream()
						.anyMatch(pattern -> pattern.matcher(location.getRepositoryLocation()).matches()))
				.collect(Collectors.toList());
		for (Location locationToUpdate : locationsToUpdate) {
			locationReplacements.put(locationToUpdate, createLocationWithUpdatedVersions(locationToUpdate));
		}

		List<Location> newLocations = mergedTargetPlatform.getLocations().stream()
				.map(location -> locationReplacements.getOrDefault(location, location)).collect(Collectors.toList());
		return new TargetPlatformFile(newLocations);
	}
	
	private Location createLocationWithUpdatedVersions(Location locationToUpdate) {
		String repositoryLocation = locationToUpdate.getRepositoryLocation();
		log.info("Updating artifact versions from location " + repositoryLocation);
		try (P2RepositoryReader reader = new P2RepositoryReader(repositoryLocation)) {

			Map<String, Set<Version>> queryResult = reader.getArtifacts();

			List<Unit> updatedUnits = locationToUpdate.getUnits().stream()
					.map(oldUnit -> createUnitWithUpdatedVersion(oldUnit, queryResult.getOrDefault(
							StringUtils.removeEnd(oldUnit.getId(), TP_FEATURE_ID_POSTFIX), Collections.emptySet())))
					.collect(Collectors.toList());

			return new Location(repositoryLocation, updatedUnits);

		} catch (URISyntaxException | IOException e) {
			log.warn("Unable to retrieve repository contents. Skipping version update for location "
					+ repositoryLocation);
			return locationToUpdate;
		}
	}

	private static Unit createUnitWithUpdatedVersion(Unit oldUnits, Set<Version> availableVersions) {
		String newVersion = availableVersions.stream().sorted(Collections.reverseOrder()).findFirst()
				.map(Version::toString).orElse(oldUnits.getVersion());
		return new Unit(oldUnits.getId(), newVersion);
	}

	
}
