package findconflicts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import findconflicts.display.table.TableGenerator;
import findconflicts.display.vo.ClassConflict;
import findconflicts.display.vo.JarConflict;
import findconflicts.display.vo.JarConflictGroup;
import findconflicts.display.vo.LogConflict;
import findconflicts.display.vo.VersionConflict;
import findconflicts.domain.ArtifactWrapper;
import findconflicts.domain.ClzWrapper;
import findconflicts.sort.ClassConflictsComparator;
import findconflicts.sort.JarConflictGroupComparator;

/**
 * find the conflicts of your projects(pom,jar,war)
 * 
 * @author david
 *
 */
@Mojo(name = "go", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class FindConflicts extends AbstractMojo {
	// the version check config file
	@Parameter(property = "versionCheckConfig")
	private String versionCheckConfig;
	// which groupId will be under checking . if null ,all groupdId. if multiply, use comma split it .
	@Parameter(property = "groupId")
	private String groupId;
	// which artifactId will be under checking . if null ,all artifactId. if multiply, use comma split it .
	@Parameter(property = "artifactId")
	private String artifactId;
	// whether showClassConflicts
	@Parameter(property = "show.class.conflicts", defaultValue = "flase")
	private boolean showClassConflicts;
	@Component
	private MavenProject project;

	public void execute() throws MojoExecutionException {
		try {
			this.execute1();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void execute1() throws MojoExecutionException {
		this.getLog().info("FindConflicts is working...");
		// preparing before collecting classes & artifacts
		LogConflictsCollector logDepCollector = new LogConflictsCollector();
		VersionConflictCollector versionConflictCollector = new VersionConflictCollector();
		if (versionCheckConfig != null) {
			try {
				versionConflictCollector.init(versionCheckConfig);
			} catch (FileNotFoundException e) {
				this.getLog().info("versionCheckConfig:" + versionCheckConfig + " doesn't exist.");
			} catch (Exception e) {
			}
		}
		Set<String> groupIdsToCheck = null;
		if (groupId != null) {
			String[] a = groupId.split(",");
			if (a.length > 0) {
				groupIdsToCheck = new HashSet<String>();
				for (int i = 0; i < a.length; i++) {
					groupIdsToCheck.add(a[i].trim());
				}
			}
		}
		Set<String> artifactIdsToCheck = null;
		if (artifactId != null) {
			String[] a = artifactId.split(",");
			if (a.length > 0) {
				artifactIdsToCheck = new HashSet<String>();
				for (int i = 0; i < a.length; i++) {
					artifactIdsToCheck.add(a[i].trim());
				}
			}
		}
		int totalJarNum = 0;
		int totalClassNum = 0;
		// key:the id of an artifact, value:the classNum
		Map<String, Integer> totalClassNumMap = new HashMap<String, Integer>();
		// data is used to store the the information of class, key: the className , value is the class information of its.
		Map<String, List<ClzWrapper>> data = new HashMap<String, List<ClzWrapper>>();
		// get the final artifacts
		Set<Artifact> artifacts = this.getProject().getArtifacts();
		for (Iterator<Artifact> iterator = artifacts.iterator(); iterator.hasNext();) {
			Artifact artifact = (Artifact) iterator.next();
			if (!artifact.isOptional()) {
				if ("jar".equals(artifact.getType())) {
					if (groupIdsToCheck != null && !groupIdsToCheck.contains(artifact.getGroupId())) {
						continue;
					}
					if (artifactIdsToCheck != null && !artifactIdsToCheck.contains(artifact.getArtifactId())) {
						continue;
					}
					totalJarNum++;
					ArtifactWrapper artifactWrapper = new ArtifactWrapper();
					artifactWrapper.artifact = artifact;
					artifactWrapper.originFrom = this.getOriginFrom(artifact);
					logDepCollector.collect(artifactWrapper);
					versionConflictCollector.collect(artifactWrapper);
					JarFile jf;
					try {
						jf = new JarFile(artifact.getFile());
						Enumeration<JarEntry> jfs = jf.entries();
						while (jfs.hasMoreElements()) {
							JarEntry jfn = jfs.nextElement();
							String fileName = jfn.getName();
							if (fileName.endsWith(".class")) {
								// ignore inner class
								if (fileName.indexOf("$") == -1) {
									ClzWrapper clzWrapper = new ClzWrapper();
									clzWrapper.className = fileName;
									clzWrapper.artifactWrapper = artifactWrapper;
									clzWrapper.size = jfn.getSize();
									if (data.get(fileName) == null) {
										List<ClzWrapper> clzInfos = new ArrayList<ClzWrapper>();
										clzInfos.add(clzWrapper);
										data.put(fileName, clzInfos);
									} else {
										data.get(fileName).add(clzWrapper);
									}
									logDepCollector.collect(clzWrapper);
									String id = Util.getId(artifact);
									if (totalClassNumMap.get(id) == null) {
										totalClassNumMap.put(id, 1);
									} else {
										totalClassNumMap.put(id, totalClassNumMap.get(id) + 1);
									}
									totalClassNum++;
								}
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		// iterator each conflicts
		Set<String> totalConflictJarNum = new HashSet<String>();
		int totalConflictClassNum = 0;
		Set<String> set = data.keySet();
		List<String> list = new ArrayList<String>(set);
		Collections.sort(list, new ClassConflictsComparator());
		Iterator<String> iter = list.iterator();
		List<JarConflictGroup> jarConflictGroups = new ArrayList<JarConflictGroup>();
		Set<String> jarConflictGroupKeys = new HashSet<String>();
		Map<String, Integer> jarConglictGroupConflitsClassNumMap = new HashMap<String, Integer>();// key:jarConflictsGroupKey, value:conflitsClassNum
		List<ClassConflict> classConflicts = new ArrayList<ClassConflict>();
		int classConflictNum = 1;
		while (iter.hasNext()) {
			String className = (String) iter.next();
			List<ClzWrapper> clzInfos = data.get(className);
			if (clzInfos.size() == 1) {
				// no conflicts
				continue;
			}
			long clzSize = clzInfos.get(0).size;
			boolean isConflicts = false;
			// only conflicts if the size of class is not equal,
			for (Iterator<ClzWrapper> iterator = clzInfos.iterator(); iterator.hasNext();) {
				ClzWrapper clzInfo = (ClzWrapper) iterator.next();
				if (clzInfo.size != clzSize) {
					isConflicts = true;
					break;
				}
			}
			if (isConflicts) {
				JarConflictGroup jarConflictGroup = new JarConflictGroup();
				for (Iterator<ClzWrapper> iterator = clzInfos.iterator(); iterator.hasNext();) {
					ClzWrapper clzInfo = (ClzWrapper) iterator.next();
					// jar conflicts
					jarConflictGroup.add(clzInfo.artifactWrapper);
					totalConflictJarNum.add(Util.getId(clzInfo.artifactWrapper.artifact));
					// class conflicts
					ClassConflict classConflict = new ClassConflict();
					classConflict.setClassName(clzInfo.className);
					classConflict.setGroupId(clzInfo.artifactWrapper.artifact.getGroupId());
					classConflict.setArtifactId(clzInfo.artifactWrapper.artifact.getArtifactId());
					classConflict.setVersion(clzInfo.artifactWrapper.artifact.getVersion());
					classConflict.setOriginFrom(Util.formatOriginFrom(clzInfo.artifactWrapper.originFrom));
					classConflict.setNumber(classConflictNum);
					classConflicts.add(classConflict);
					totalConflictClassNum++;
				}
				classConflictNum++;
				String jarConflictsGroupKey = jarConflictGroup.getGroupKey();
				if (jarConglictGroupConflitsClassNumMap.get(jarConflictsGroupKey) == null) {
					jarConglictGroupConflitsClassNumMap.put(jarConflictsGroupKey, clzInfos.size());
				} else {
					jarConglictGroupConflitsClassNumMap.put(jarConflictsGroupKey, clzInfos.size() + jarConglictGroupConflitsClassNumMap.get(jarConflictsGroupKey));
				}
				if (!jarConflictGroupKeys.contains(jarConflictsGroupKey)) {
					jarConflictGroupKeys.add(jarConflictsGroupKey);
					jarConflictGroups.add(jarConflictGroup);
				}
			}
		}
		// jarConflicts
		for (Iterator<JarConflictGroup> iterator = jarConflictGroups.iterator(); iterator.hasNext();) {
			JarConflictGroup jarConflictGroup = (JarConflictGroup) iterator.next();
			jarConflictGroup.setConflitsClassNum(jarConglictGroupConflitsClassNumMap.get(jarConflictGroup.getGroupKey()));
			int groupTotalClass = 0;
			List<ArtifactWrapper> artifactWrappers = jarConflictGroup.getArtifactWrappers();
			if (artifactWrappers != null && artifactWrappers.size() > 0) {
				for (Iterator<ArtifactWrapper> iterator_1 = artifactWrappers.iterator(); iterator_1.hasNext();) {
					ArtifactWrapper artifactWrapper = (ArtifactWrapper) iterator_1.next();
					Artifact artifact = artifactWrapper.artifact;
					groupTotalClass += totalClassNumMap.get(Util.getId(artifact));
				}
				jarConflictGroup.setTotalClassNum(groupTotalClass);
			}
		}
		if (jarConflictGroups.size() > 0) {
			Collections.sort(jarConflictGroups, new JarConflictGroupComparator());
			int number = 1;
			List<JarConflict> jarConflicts = new ArrayList<JarConflict>();
			for (Iterator<JarConflictGroup> iterator = jarConflictGroups.iterator(); iterator.hasNext();) {
				JarConflictGroup jarConflictGroup = (JarConflictGroup) iterator.next();
				jarConflictGroup.setNumber(number++);
				jarConflicts.addAll(jarConflictGroup.getJarConflicts());
			}
			this.getLog().warn("*********************************************Jar Conflicts****************************************************");
			this.getLog().warn((new TableGenerator()).generateTable(jarConflicts));
			this.getLog().info("Jar Conflicts Total: jar conflicts ratio:" + totalConflictJarNum.size() + "/" + totalJarNum + "=" + NumberFormat.getPercentInstance().format((float) totalConflictJarNum.size() / totalJarNum));
			this.getLog().info("Jar Conflicts Solution Hint: choose one artifact of the conflicts, and exclude other artifacts at pom.xml  according to originFrom.");
		} else {
			this.getLog().info("No jar conflicts found!");
		}
		if (showClassConflicts) {
			if (classConflicts.size() > 0) {
				this.getLog().warn("*********************************************Class Conflicts****************************************************");
				this.getLog().warn((new TableGenerator()).generateTable(classConflicts));
				this.getLog().info("Class Conflicts Total: class conflicts ratio:" + totalConflictClassNum + "/" + totalClassNum + "=" + NumberFormat.getPercentInstance().format((float) totalConflictClassNum / totalClassNum));
				this.getLog().info("Class Conflicts Solution Hint: choose one artifact of the conflicts, and exclude other artifacts at pom.xml  according to originFrom.");
			} else {
				this.getLog().info("No class conflicts found!");
			}
		}
		List<LogConflict> logConflicts = logDepCollector.getLogConflicts();
		if (logConflicts != null && logConflicts.size() > 0) {
			this.getLog().warn("*********************************************Log Conflicts****************************************************");
			this.getLog().warn((new TableGenerator()).generateTable(logConflicts));
			this.getLog().info("Log Conflicts Solution Hint: choose one artifact of the conflicts, and exclude other artifacts at pom.xml  according to originFrom.");
			this.getLog().info("As for the conflicts of SLF4J, you can refer to this offical article:https://www.slf4j.org/codes.html#version_mismatch");
		} else {
			this.getLog().info("No log conflicts found!");
		}

		List<VersionConflict> versionConflicts = versionConflictCollector.getVersionConflict();
		if (versionConflicts != null && versionConflicts.size() > 0) {
			this.getLog().warn("*********************************************Version Conflicts****************************************************");
			this.getLog().warn((new TableGenerator()).generateTable(versionConflicts));
			this.getLog().info("Version Conflicts Solution Hint: update the version of the artifact according to requiredVersion");
		} else {
			this.getLog().info("No version conflicts found!");
		}
		this.getLog().info("FindConflicts finished!");

	}

	private Dependency getOriginFrom(Artifact artifcat) {
		List<String> depTrails = artifcat.getDependencyTrail();
		int size = depTrails.size();
		for (int i = size - 1; i > 0; i--) {
			String dep = depTrails.get(i);
			String[] a = dep.split(":");
			String groupId = a[0];
			String artifactId = a[1];
			String version = a[3];
			List<Dependency> dependencies = this.getProject().getDependencies();
			for (Iterator<Dependency> iterator3 = dependencies.iterator(); iterator3.hasNext();) {
				Dependency dependency = (Dependency) iterator3.next();
				if (dependency.getGroupId().equals(groupId)) {
					if (dependency.getArtifactId().equals(artifactId)) {
						if (dependency.getVersion().equals(version)) {
							// find the originFrom(the depdency that developers declare mannually at pom.xml)
							return dependency;

						}
					}
				}
			}
		}
		return null;
	}

	public MavenProject getProject() {
		return project;
	}

}
