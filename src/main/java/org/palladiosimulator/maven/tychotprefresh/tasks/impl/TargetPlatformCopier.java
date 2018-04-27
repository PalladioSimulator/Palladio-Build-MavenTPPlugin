package org.palladiosimulator.maven.tychotprefresh.tasks.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.project.MavenProject;
import org.palladiosimulator.maven.tychotprefresh.tasks.TargetPlatformTaskBase;
import org.palladiosimulator.maven.tychotprefresh.tasks.TaskDependencies;
import org.palladiosimulator.maven.tychotprefresh.tasks.TaskExecutionException;
import org.palladiosimulator.maven.tychotprefresh.tp.model.TargetPlatformFile;
import org.palladiosimulator.maven.tychotprefresh.tp.parser.TargetPlatformParser;

public class TargetPlatformCopier extends TargetPlatformTaskBase {

	private final MavenProject rootProject;
	private final String destinationName;

	public TargetPlatformCopier(TaskDependencies dependencies, MavenProject destinationProject,
			String destinationName) {
		super(dependencies, "Copying target platform file copy to build output directory.");
		this.rootProject = destinationProject;
		this.destinationName = destinationName;
	}

	@Override
	public Collection<TargetPlatformFile> process(Collection<TargetPlatformFile> input) throws TaskExecutionException {
		Validate.notNull(input);
		Validate.isTrue(input.size() == 1);

		TargetPlatformFile tp = input.iterator().next();
		File outputDir = new File(rootProject.getBuild().getDirectory());
		File destinationFile = new File(outputDir, destinationName);
		writeTpToBuildOutputDir(tp, destinationFile);

		return input;
	}

	protected void writeTpToBuildOutputDir(TargetPlatformFile tp, File destinationFile) {
		try {
			FileUtils.forceMkdir(destinationFile.getParentFile());
			TargetPlatformParser.serialize(tp, destinationFile);
		} catch (ParserConfigurationException | TransformerException | IOException e) {
			new TaskExecutionException(e);
		}
	}

}
