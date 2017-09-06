package findconflicts;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;

import findconflicts.display.table.TableGenerator;

/**
 * Utility
 * 
 * @author david
 *
 */
public class Util {

	// format the dependency like the configuration at pom.xml
	public static String formatOriginFrom(Dependency originFrom) {
		if (originFrom == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("<groupId>");
		builder.append(makeShort(originFrom.getGroupId(), Constants.GROUPID_MAX_WIDTH));
		builder.append("</groupId>");
		builder.append(TableGenerator.CELL_NEW_LINE);
		builder.append("<artifactId>");
		builder.append(makeShort(originFrom.getArtifactId(), Constants.ARTIFACTID_MAX_WIDTH));
		builder.append("</artifactId>");
		builder.append(TableGenerator.CELL_NEW_LINE);
		builder.append("<version>");
		builder.append(originFrom.getVersion());
		builder.append("</version>");
		builder.append(TableGenerator.CELL_NEW_LINE);
		return builder.toString();
	}

	public static String getId(Artifact artifact) {
		return artifact.getGroupId().concat(":").concat(artifact.getArtifactId()).concat(":").concat(artifact.getVersion());
	}

	// make short of a string to avoid too large string displaying at your screen
	public static String makeShort(String str, int maxWidth) {
		if (str == null) {
			return str;
		}
		if (str.length() > maxWidth) {
			StringBuilder sb = new StringBuilder(str);
			for (int i = 1; i <= 10; i++) {
				if (sb.toString().length() > i * maxWidth) {
					sb.insert(i * maxWidth, TableGenerator.CELL_NEW_LINE);
				} else {
					break;
				}
			}
			return sb.toString();
		}
		return str;
	}

}
