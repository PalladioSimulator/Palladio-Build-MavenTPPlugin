package org.palladiosimulator.maven.tychotprefresh.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.osgi.framework.Version;
import org.osgi.util.promise.PromiseFactory;

import aQute.bnd.http.HttpClient;
import aQute.p2.api.Artifact;
import aQute.p2.provider.P2Impl;

public class P2RepositoryReader implements Closeable {

	private final ExecutorService executor = Executors.newFixedThreadPool(4);

	private final URI repositoryURI;

	public P2RepositoryReader(String location) throws URISyntaxException {
		repositoryURI = createURI(location);
	}

	public Map<String, Set<Version>> getArtifacts() throws IOException {
		try (HttpClient client = new HttpClient()) {
			P2Impl p2 = new P2Impl(client, repositoryURI, new PromiseFactory(executor));
			Collection<Artifact> artifacts = p2.getAllArtifacts();
			return artifacts.stream()
					.collect(Collectors.groupingBy(a -> a.id, Collectors.mapping(a -> a.version, Collectors.toSet())));
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private static URI createURI(String location) throws URISyntaxException {
		return new URI(location);
	}

	@Override
	public void close() throws IOException {
		executor.shutdown();
	}

}
