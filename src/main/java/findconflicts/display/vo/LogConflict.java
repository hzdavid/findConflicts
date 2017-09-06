package findconflicts.display.vo;

/**
 * log conflicts
 * 
 * @author david
 *
 */
public class LogConflict {

	private Integer number;
	private String logConflictType;
	private String groupId;
	private String artifactId;
	private String version;
	private String originFrom;

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getOriginFrom() {
		return originFrom;
	}

	public void setOriginFrom(String originFrom) {
		this.originFrom = originFrom;
	}

	public String getLogConflictType() {
		return logConflictType;
	}

	public void setLogConflictType(String logConflictType) {
		this.logConflictType = logConflictType;
	}

}
