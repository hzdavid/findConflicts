package findconflicts.domain;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;

/**
 * artifact wrapper
 * 
 * @author david
 *
 */
public class ArtifactWrapper {
	public Artifact artifact;
	public Dependency originFrom;
}
