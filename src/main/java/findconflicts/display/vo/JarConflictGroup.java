package findconflicts.display.vo;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;

import findconflicts.Util;
import findconflicts.domain.ArtifactWrapper;
/**
 * a group of jar conflicts
 * @author david
 *
 */
public class JarConflictGroup {
	private int number;
	private int conflitsClassNum = 0;
	private int totalClassNum = 0;
	private List<ArtifactWrapper> artifactWrappers = new ArrayList<ArtifactWrapper>();

	public JarConflictGroup() {
		super();
	}

	public String getGroupKey() {
		StringBuilder builder = new StringBuilder();
		if (artifactWrappers != null && artifactWrappers.size() > 0) {
			for (Iterator<ArtifactWrapper> iterator = artifactWrappers.iterator(); iterator.hasNext();) {
				ArtifactWrapper artifactWrapper = (ArtifactWrapper) iterator.next();
				Artifact artifact = artifactWrapper.artifact;
				builder.append(artifact.getGroupId().concat(artifact.getArtifactId()).concat(artifact.getVersion()));
			}
		}
		return builder.toString();
	}

	public void add(ArtifactWrapper artifactWrapper) {
		artifactWrappers.add(artifactWrapper);
	}

	public List<JarConflict> getJarConflicts() {
		List<JarConflict> jarConflicts = new ArrayList<JarConflict>();
		if (artifactWrappers != null && artifactWrappers.size() > 0) {
			for (Iterator<ArtifactWrapper> iterator = artifactWrappers.iterator(); iterator.hasNext();) {
				ArtifactWrapper artifactWrapper = (ArtifactWrapper) iterator.next();
				Artifact artifact = artifactWrapper.artifact;
				JarConflict jarConflict = new JarConflict();
				jarConflict.setNumber(number);
				jarConflict.setGroupId(artifact.getGroupId());
				jarConflict.setArtifactId(artifact.getArtifactId());
				jarConflict.setVersion(artifact.getVersion());
				jarConflict.setOriginFrom(Util.formatOriginFrom(artifactWrapper.originFrom));
				String conflictRatio = NumberFormat.getPercentInstance().format((float) conflitsClassNum / totalClassNum) + "(" + conflitsClassNum + "/" + totalClassNum + ")";
				jarConflict.setClassConflictRatio(conflictRatio);
				jarConflicts.add(jarConflict);
			}
		}
		return jarConflicts;
	}

	public float getConflictRatio() {
		if (totalClassNum > 0) {
			return (float) conflitsClassNum / totalClassNum;
		}
		return 0;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getConflitsClassNum() {
		return conflitsClassNum;
	}

	public void setConflitsClassNum(int conflitsClassNum) {
		this.conflitsClassNum = conflitsClassNum;
	}

	public int getTotalClassNum() {
		return totalClassNum;
	}

	public void setTotalClassNum(int totalClassNum) {
		this.totalClassNum = totalClassNum;
	}

	public List<ArtifactWrapper> getArtifactWrappers() {
		return artifactWrappers;
	}

	public void setArtifactWrappers(List<ArtifactWrapper> artifactWrappers) {
		this.artifactWrappers = artifactWrappers;
	}

}
