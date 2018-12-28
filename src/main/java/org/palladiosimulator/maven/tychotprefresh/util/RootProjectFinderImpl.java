package org.palladiosimulator.maven.tychotprefresh.util;

import java.util.Optional;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = IRootProjectFinder.class)
public class RootProjectFinderImpl implements IRootProjectFinder {

    @Override
    public MavenProject findRootProject(MavenSession session) throws MavenExecutionException {
        Optional<MavenProject> rootProject = session.getAllProjects().stream().filter(MavenProject::isExecutionRoot)
                .findFirst();
        return rootProject.orElseThrow(() -> new MavenExecutionException("Could not find root project.",
                Optional.ofNullable(session.getCurrentProject()).map(MavenProject::getFile).orElse(null)));
    }

}
