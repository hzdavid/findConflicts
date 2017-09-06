package findconflicts.display.vo;
/**
 * version conflicts
 * 
 * @author david
 *
 */
public class VersionConflict {

	private Integer number;
	private String groupId;
	private String artifactId;
	private String version;
	private String requiredVersion;
	private String conflictReason;
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


	 


	public void setVersion(String version) {
		this.version = version;
	}

	 
	 

	public String getOriginFrom() {
		return originFrom;
	}

	public void setOriginFrom(String originFrom) {
		this.originFrom = originFrom;
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

	public String getRequiredVersion() {
		return requiredVersion;
	}

	public void setRequiredVersion(String requiredVersion) {
		this.requiredVersion = requiredVersion;
	}

	public String getConflictReason() {
		return conflictReason;
	}

	public void setConflictReason(String conflictReason) {
		this.conflictReason = conflictReason;
	}

}
