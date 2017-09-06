package findconflicts.display.vo;

/**
 * class conflicts
 * @author david
 *
 */
public class ClassConflict {

	private Integer number;
	private String className;
	// private long size;
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

	public void setVersion(String version) {
		this.version = version;
	}

	 

	public String getOriginFrom() {
		return originFrom;
	}

	public void setOriginFrom(String originFrom) {
		this.originFrom = originFrom;
	}

	public String getClassName() {
		return className;
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

	public void setClassName(String className) {
		this.className = className;
	}

}
