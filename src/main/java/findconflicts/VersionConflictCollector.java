package findconflicts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;

import findconflicts.display.vo.VersionConflict;
import findconflicts.domain.ArtifactWrapper;

/**
 * version conflicts collector
 * 
 * @author david
 *
 */
public class VersionConflictCollector {
	private final static String LARGE = ">";
	private final static String LARGE_EQUAL = ">=";
	private final static String SMALL = ">";
	private final static String SMALL_EQUAL = ">=";
	private List<IfRegular> ifRegulars = new ArrayList<IfRegular>();
	private List<Regular> regulars = new ArrayList<Regular>();

	private Set<String> relatedArtifacts = new HashSet<String>();// vallue : groupId:artifactId:version

	private List<ArtifactWrapper> artifactWrappersToCheck = new ArrayList<ArtifactWrapper>();

	// read the versionCheckConfigFile, parse the version check regular
	public void init(String versionCheckConfigFile) throws FileNotFoundException, Exception {
		String ifRegularExp = "^if(.*?)then(.*?)$";
		String regularExp = "^(.*?):(.*?)(>=|<=|>|<)(.*?)$";
		Pattern ifPattern = Pattern.compile(ifRegularExp);
		Pattern pattern = Pattern.compile(regularExp);
		File file = new File(versionCheckConfigFile);
		if (!(file.isFile() && file.exists())) {
			throw new FileNotFoundException();
		}
		InputStreamReader read = new InputStreamReader(new FileInputStream(file));
		BufferedReader bufferedReader = new BufferedReader(read);
		String lineTxt = null;
		try {
			while ((lineTxt = bufferedReader.readLine()) != null) {
				Matcher matcher1 = ifPattern.matcher(lineTxt);
				if (matcher1.find() && matcher1.groupCount() == 2) {
					IfRegular ifRegular = new IfRegular();
					{
						String ifExpr = matcher1.group(1);
						Matcher matcher2 = pattern.matcher(ifExpr);
						if (matcher2.find() && matcher2.groupCount() == 4) {
							String groupId = matcher2.group(1).trim();
							String artifactId = matcher2.group(2).trim();
							String compareChar = matcher2.group(3).trim();
							String version = matcher2.group(4).trim();
							relatedArtifacts.add(groupId.concat(":").concat(artifactId));
							Regular regular = new Regular();
							regular.groupId = groupId;
							regular.artifactId = artifactId;
							regular.compareChar = compareChar;
							regular.version = version;
							ifRegular.ifRegular = regular;
						}
					}
					{
						String thenExpr = matcher1.group(2);
						Matcher matcher2 = pattern.matcher(thenExpr);
						if (matcher2.find() && matcher2.groupCount() == 4) {
							String groupId = matcher2.group(1).trim();
							String artifactId = matcher2.group(2).trim();
							String compareChar = matcher2.group(3).trim();
							String version = matcher2.group(4).trim();
							relatedArtifacts.add(groupId.concat(":").concat(artifactId));
							Regular regular = new Regular();
							regular.groupId = groupId;
							regular.artifactId = artifactId;
							regular.compareChar = compareChar;
							regular.version = version;
							ifRegular.thenRegular = regular;
						}
					}
					this.ifRegulars.add(ifRegular);
				} else {
					Matcher matcher2 = pattern.matcher(lineTxt);
					if (matcher2.find() && matcher2.groupCount() == 4) {
						String groupId = matcher2.group(1).trim();
						String artifactId = matcher2.group(2).trim();
						String compareChar = matcher2.group(3).trim();
						String version = matcher2.group(4).trim();
						relatedArtifacts.add(groupId.concat(":").concat(artifactId));
						Regular regular = new Regular();
						regular.groupId = groupId;
						regular.artifactId = artifactId;
						regular.compareChar = compareChar;
						regular.version = version;
						this.regulars.add(regular);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				read.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// collect relate artifact
	public void collect(ArtifactWrapper artifactWrapper) {
		Artifact artifact = artifactWrapper.artifact;
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		if (relatedArtifacts.contains(groupId.concat(":").concat(artifactId))) {
			artifactWrappersToCheck.add(artifactWrapper);
		}
		return;
	}

	public List<VersionConflict> getVersionConflict() {
		List<VersionConflict> conflicts = new ArrayList<VersionConflict>();
		Integer number = 1;
		for (Iterator<ArtifactWrapper> iterator = artifactWrappersToCheck.iterator(); iterator.hasNext();) {
			ArtifactWrapper artifactWrapper = (ArtifactWrapper) iterator.next();
			// regular scan
			for (Iterator<Regular> iterator2 = regulars.iterator(); iterator2.hasNext();) {
				Regular regular = (Regular) iterator2.next();
				try {
					if (!this.match(regular, artifactWrapper)) {
						VersionConflict conflict = new VersionConflict();
						conflict.setNumber(number++);
						conflict.setGroupId(artifactWrapper.artifact.getGroupId());
						conflict.setArtifactId(artifactWrapper.artifact.getArtifactId());
						conflict.setVersion(artifactWrapper.artifact.getVersion());
						conflict.setOriginFrom(Util.formatOriginFrom(artifactWrapper.originFrom));
						conflict.setRequiredVersion(regular.compareChar.concat(regular.version));
						conflicts.add(conflict);
					}
				} catch (CanotMatchException e) {

				}
			}
			// if regular scan
			for (Iterator<IfRegular> iterator2 = ifRegulars.iterator(); iterator2.hasNext();) {
				IfRegular obj = (IfRegular) iterator2.next();
				Regular ifRegular = obj.ifRegular;
				Regular thenRegular = obj.thenRegular;
				try {
					if (this.match(ifRegular, artifactWrapper)) {
						for (Iterator<ArtifactWrapper> iterator_1 = artifactWrappersToCheck.iterator(); iterator_1.hasNext();) {
							ArtifactWrapper artifactWrapper_1 = (ArtifactWrapper) iterator_1.next();
							try {
								if (!this.match(thenRegular, artifactWrapper_1)) {
									VersionConflict conflict = new VersionConflict();
									conflict.setNumber(number++);
									conflict.setGroupId(artifactWrapper_1.artifact.getGroupId());
									conflict.setArtifactId(artifactWrapper_1.artifact.getArtifactId());
									conflict.setVersion(artifactWrapper_1.artifact.getVersion());
									conflict.setOriginFrom(Util.formatOriginFrom(artifactWrapper_1.originFrom));
									conflict.setRequiredVersion(thenRegular.compareChar.concat(thenRegular.version));
									conflict.setConflictReason(ifRegular.groupId.concat(":").concat(ifRegular.artifactId).concat(":").concat(ifRegular.compareChar).concat(ifRegular.version));
									conflicts.add(conflict);
								}
							} catch (CanotMatchException e) {

							}
						}
					}
				} catch (CanotMatchException e) {

				}
			}
		}

		return conflicts;
	}

	private boolean match(Regular regular, ArtifactWrapper artifactWrapper) throws CanotMatchException {
		boolean b1 = regular.groupId.equals(artifactWrapper.artifact.getGroupId());
		boolean b2 = regular.artifactId.equals(artifactWrapper.artifact.getArtifactId());
		if (b1 && b2) {
			String currentVersion = artifactWrapper.artifact.getVersion();
			String targetVersion = regular.version;
			// compare version
			String[] a = currentVersion.split("\\.");
			int[] rangeArray=new int[]{10000000,1000000,100000,10000,1000,100,10,1};
			int aSum = 0;// add the part of the version
			for (int i = 0; i < a.length; i++) {
				try {
					int range=0;
					if(i<=rangeArray.length-1){
						range=rangeArray[i];
					}
					aSum += Integer.parseInt(a[i])*range;
				} catch (NumberFormatException e) {
				}
			}
			String[] b = targetVersion.split("\\.");
			int bSum = 0;// add the part of the version
			for (int i = 0; i < b.length; i++) {
				try {
					int range=0;
					if(i<=rangeArray.length-1){
						range=rangeArray[i];
					}
					bSum += Integer.parseInt(b[i])*range;
				} catch (NumberFormatException e) {
				}
			}

			if (LARGE.equals(regular.compareChar)) {
				if (!(aSum > bSum)) {
					return false;
				} else {
					return true;
				}
			}
			if (LARGE_EQUAL.equals(regular.compareChar)) {
				if (!(aSum >= bSum)) {
					return false;
				} else {
					return true;
				}
			}
			if (SMALL.equals(regular.compareChar)) {
				if (!(aSum < bSum)) {
					return false;
				} else {
					return true;
				}
			}
			if (SMALL_EQUAL.equals(regular.compareChar)) {
				if (!(aSum <= bSum)) {
					return false;
				} else {
					return true;
				}
			}
			throw new CanotMatchException();
		} else {
			throw new CanotMatchException();
		}

	}

	private static class CanotMatchException extends Exception {
		private static final long serialVersionUID = 5261277604002409841L;

	}

	private static class IfRegular {
		Regular ifRegular;
		Regular thenRegular;
	}

	private static class Regular {
		String groupId;
		String artifactId;
		String compareChar;
		String version;
	}

	public List<IfRegular> getIfRegulars() {
		return ifRegulars;
	}

	public void setIfRegulars(List<IfRegular> ifRegulars) {
		this.ifRegulars = ifRegulars;
	}

	public List<Regular> getRegulars() {
		return regulars;
	}

	public void setRegulars(List<Regular> regulars) {
		this.regulars = regulars;
	}

	public Set<String> getRelatedArtifacts() {
		return relatedArtifacts;
	}

	public void setRelatedArtifacts(Set<String> relatedArtifacts) {
		this.relatedArtifacts = relatedArtifacts;
	}

	public List<ArtifactWrapper> getArtifactWrappersToCheck() {
		return artifactWrappersToCheck;
	}

	public void setArtifactWrappersToCheck(List<ArtifactWrapper> artifactWrappersToCheck) {
		this.artifactWrappersToCheck = artifactWrappersToCheck;
	}
 
}
