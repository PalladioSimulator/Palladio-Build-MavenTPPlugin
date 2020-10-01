package org.palladiosimulator.maven.tychotprefresh.tasks.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.settings.Proxy;
import org.osgi.framework.Version;
import org.palladiosimulator.maven.tychotprefresh.tasks.TargetPlatformTaskBase;
import org.palladiosimulator.maven.tychotprefresh.tasks.TaskDependencies;
import org.palladiosimulator.maven.tychotprefresh.tp.model.Location;
import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;
import org.palladiosimulator.maven.tychotprefresh.tp.model.Unit;
import org.palladiosimulator.maven.tychotprefresh.util.P2RepositoryReader;

public class TargetPlatformUpdater extends TargetPlatformTaskBase {

	private static final String TP_FEATURE_ID_POSTFIX = ".feature.group";
	private Optional<Proxy> proxy;
	
	public TargetPlatformUpdater(TaskDependencies dependencies, Optional<Proxy> proxy) {
		super(dependencies, "Updating target platform definitions.");
		this.proxy = proxy;
	}

	@Override
	public Collection<TargetPlatformFile> process(Collection<TargetPlatformFile> input) {
		Validate.notNull(input);
		
		return input.stream().map(this::updateTargetPlatformFile).collect(Collectors.toList());
	}
	
	protected TargetPlatformFile updateTargetPlatformFile(TargetPlatformFile tpFile) {
		Map<Location, Location> locationReplacements = new HashMap<>();
		Collection<Location> locationsToUpdate = tpFile.getLocations().stream()
				.filter(Location::isRefresh)
				.collect(Collectors.toList());
		for (Location locationToUpdate : locationsToUpdate) {
			locationReplacements.put(locationToUpdate, createLocationWithUpdatedVersions(locationToUpdate));
		}

		List<Location> newLocations = tpFile.getLocations().stream()
				.map(location -> locationReplacements.getOrDefault(location, location)).collect(Collectors.toList());
		return new TargetPlatformFile(newLocations);
	}

	private Location createLocationWithUpdatedVersions(Location locationToUpdate) {
		Location updatedLocation = new Location(locationToUpdate);
		
		String repositoryLocation = updatedLocation.getRepositoryLocation();
		getLog().info("Updating artifact versions from location " + repositoryLocation);
		try (P2RepositoryReader reader = new P2RepositoryReader(repositoryLocation, proxy)) {

			Map<String, Set<Version>> queryResult = reader.getArtifacts();

			List<Unit> updatedUnits = updatedLocation.getUnits().stream()
					.map(oldUnit -> createUnitWithUpdatedVersion(oldUnit, queryResult.getOrDefault(
							StringUtils.removeEnd(oldUnit.getId(), TP_FEATURE_ID_POSTFIX), Collections.emptySet())))
					.collect(Collectors.toList());

			updatedLocation.setUnits(updatedUnits);
			return updatedLocation;

		} catch (URISyntaxException | IOException e) {
			Throwable causeToReport = getLog().isDebugEnabled() ? e : null;
			getLog().warn("Unable to retrieve repository contents. Skipping version update for location "
					+ repositoryLocation, causeToReport);
			return locationToUpdate;
		}
	}
	
	private static Unit createUnitWithUpdatedVersion(Unit oldUnit, Set<Version> availableVersions) {
		Unit updatedUnit = new Unit(oldUnit);
		String newVersion = availableVersions.stream().sorted(Collections.reverseOrder()).findFirst()
				.map(Version::toString).orElse(oldUnit.getVersion());
		updatedUnit.setVersion(newVersion);
		return updatedUnit;
	}

}
