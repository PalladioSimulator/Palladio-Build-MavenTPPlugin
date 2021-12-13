# Tycho Target Platform Refresh Plugin

The refresh plugin is an extension of the maven lifecycle that builds the target platform for a tycho-based build dynamically before the actual dependency resolution. The plugin provides
* merging of multiple [target platform definition files](https://wiki.eclipse.org/PDE/Target_Definitions) into a single one
* <s>refreshing of versions of features mentioned in the target platform definitions</s> (deprecated: use version `0.0.0` instead)
* filtering target platform locations based on tags
* attaching of target platform via a virtual maven project

## Usage
To use the extension, you have to create a `.mvn` folder in the root of your project and put the following `extensions.xml` in it (replace `1.2.3` with most recent version):
```
<?xml version="1.0" encoding="UTF-8"?>
<extensions>
	<extension>
		<groupId>org.palladiosimulator</groupId>
		<artifactId>tycho-tp-refresh-maven-plugin</artifactId>
		<version>1.2.3</version>
	</extension>
</extensions>
```
If you already defined extensions such as the tycho pomless build, just place the extension below.

The whole configuration of the extension is done by placing properties in the root project (or one of its parents). The following properties are available. The prefix `org.palladiosimulator.maven.tychotprefresh.` is omitted in the table. The last `n` part of a property is a placeholder for an integer value greater than -1.

| Property             | Example Value                                                  | Required | Meaning |
| -------------------- | -------------------------------------------------------------- | -------- | ------------ |
| tpproject.artifactId | target-platform-merged-temporary                               | yes      | The artifact id of the project that will contain the generated target platform definition. |
| tpproject.groupId    | org.palladiosimulator                                          | yes      | The group id of the project that will contain the generated target platform definition. |
| tpproject.version    | 1.0.0                                                          | yes      | The version of the project that will contain the generated target platform definition. |
| tpproject.classifier | merged-temporary                                               | yes      | The classifier for the artifact containing the generated target platform definition. |
| tpproject.type       | target                                                         | yes      | The type of the artifact containing the generated target platform definition. |
| tplocation.n         | org.palladiosimulator:target-platform-base:0.1.2:oxygen:target | no       | Locations for target platform definition files. Can be an artifact descriptor or a file path. |
| filter.n             | release                                                        | no       | The location annotated with this keyword will be included in the merged target platform definition.
| disable              | true                                                           | no       | Disables the target platform processing by the extension. |

In order to use the generated target platform, adhere to the [Tycho reference](https://wiki.eclipse.org/Tycho/Target_Platform#Target_files) and use the project coordinates you defined by the properties.

## Target Platform Definition Files
In order to use the filter <s>and refresh</s> abilities of the extension, you have to add additional properties to your target definition files. These additions are not compatible with the Eclipse editor and will lead to undefined behavior if you open them with the editor. The best approach is to use a plain text editor after the initial definition of the target platform.

The additional properties `filter` <s>and `refresh`</s> are placed in the location tag of the target platform as shown below.
```
<location
  includeAllPlatforms="false"
  includeConfigurePhase="true"
  includeMode="planner"
  includeSource="false"
  type="InstallableUnit"
  filter="nightly"
  refresh="true">
```
The `filter` property takes a keyword that can be referred to in the properties of the POM. If a filter is set but there is no property referring to the mentioned keyword, the whole location will be omitted.

<s>The `refresh` keyword takes either `true` or `false` to indicate if the versions of the contained units shall be updated to the latest available version based on the current contents of the update site.</s>

Please note that the remaining properties of the location tag shown above are always set to the values shown above. You cannot change them.

## Debugging
The generated target platform definition is stored in a temporary file that will be deleted after the build. Another copy is stored in the target folder of the root project. Please note that this copy will be deleted if you execute the `clean` goal. In order to have the target platform file available even if using `clean`, you have to add the following plugin to your POM file.

```
<plugin>
	<groupId>org.palladiosimulator</groupId>
	<artifactId>tycho-tp-refresh-maven-plugin</artifactId>
	<version>1.2.3</version>
	<executions>
		<execution>
			<id>target-platform-copy</id>
			<goals>
				<goal>copy</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

This will copy the target platform file in the `generate-resources` phase into the target folder of your root project.
