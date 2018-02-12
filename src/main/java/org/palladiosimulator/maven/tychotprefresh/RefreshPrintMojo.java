package org.palladiosimulator.maven.tychotprefresh;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Requirement;

@Mojo(name = "echo")
public class RefreshPrintMojo extends AbstractMojo {

	@Requirement
	private TPRefresher tpRefresher;
	
	@Parameter(defaultValue = "${session}")
	private MavenSession session;
	
	@Parameter(defaultValue = "${project}")
	private MavenProject project;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			getLog().info(tpRefresher.simulateRefreshToString(session, project));
		} catch (MavenExecutionException e) {
			throw new MojoExecutionException("Could not simulate target platform refresh.", e);
		}
	}

}
