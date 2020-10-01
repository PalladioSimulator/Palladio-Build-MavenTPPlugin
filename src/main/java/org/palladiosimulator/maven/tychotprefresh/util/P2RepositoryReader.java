package org.palladiosimulator.maven.tychotprefresh.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.maven.settings.Proxy;
import org.osgi.framework.Version;
import org.osgi.util.promise.PromiseFactory;

import aQute.bnd.http.HttpClient;
import aQute.bnd.service.url.ProxyHandler;
import aQute.p2.api.Artifact;
import aQute.p2.provider.P2Impl;

public class P2RepositoryReader implements Closeable {

	private final ExecutorService executor = Executors.newFixedThreadPool(4);

	private final URI repositoryURI;

	private final Optional<Proxy> proxy;

	public P2RepositoryReader(String location, Optional<Proxy> proxy) throws URISyntaxException {
		repositoryURI = createURI(location);
		this.proxy = proxy;
	}

	public Map<String, Set<Version>> getArtifacts() throws IOException {
		try (HttpClient client = createHttpClient()) {
			P2Impl p2 = new P2Impl(client, repositoryURI, new PromiseFactory(executor));
			Collection<Artifact> artifacts = p2.getAllArtifacts();
			return artifacts.stream()
					.collect(Collectors.groupingBy(a -> a.id, Collectors.mapping(a -> a.version, Collectors.toSet())));
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	protected HttpClient createHttpClient() {
		HttpClient client = new HttpClient();
		boolean isHttpProxy = proxy.map(Proxy::getProtocol).map(String::toLowerCase).map("http"::equals).orElse(false);
		if (isHttpProxy) {
			Proxy mavenProxy = proxy.get();
			java.net.Proxy javaProxy = new java.net.Proxy(java.net.Proxy.Type.HTTP,
					new InetSocketAddress(mavenProxy.getHost(), mavenProxy.getPort()));
			PasswordAuthentication javaProxyAuth = createProxyAuth(mavenProxy);
			Collection<String> nonProxyHostWildcards = Optional.ofNullable(mavenProxy.getNonProxyHosts())
					.map(h -> Arrays.asList(h.split("[|]"))).orElse(Collections.emptyList());
			Collection<String> nonProxyHostRegexs = nonProxyHostWildcards.stream().map(this::wildcardToRegex)
					.collect(Collectors.toList());
			client.addProxyHandler(new ProxyHandler() {
				@Override
				public ProxySetup forURL(URL url) throws Exception {
					ProxySetup setup = new ProxySetup();
					if (matches(url.getHost(), nonProxyHostRegexs)) {
						setup.proxy = java.net.Proxy.NO_PROXY;
					} else {
						setup.proxy = javaProxy;
						setup.authentication = javaProxyAuth;
					}
					return setup;
				}
			});
		}
		return client;
	}
	
	protected String wildcardToRegex(String wildcardPattern) {
		return wildcardPattern.replace(".", "\\.").replace("*", ".*");
	}
	
	protected boolean matches(String host, Collection<String> patterns ) {
		for (String pattern : patterns) {
			if (host.matches(pattern)) {
				return true;
			}
		}
		return false;
	}

	private static URI createURI(String location) throws URISyntaxException {
		return new URI(location);
	}

	private static PasswordAuthentication createProxyAuth(Proxy mavenProxy) {
		if (isNotEmpty(mavenProxy.getUsername()) && isNotEmpty(mavenProxy.getPassword())) {
			return new PasswordAuthentication(mavenProxy.getUsername(), mavenProxy.getPassword().toCharArray());
		}
		return null;
	}

	private static boolean isNotEmpty(String value) {
		return value != null && !"".equals(value.trim());
	}

	@Override
	public void close() throws IOException {
		executor.shutdown();
	}

}
