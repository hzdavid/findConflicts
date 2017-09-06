package findconflicts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import findconflicts.display.vo.LogConflict;
import findconflicts.domain.ArtifactWrapper;
import findconflicts.domain.ClzWrapper;

/**
 * log conflicts collector
 * 
 * @author david
 *
 */
public class LogConflictsCollector {

	// org.slf4j:log4j-over-slf4j,org.slf4j:slf4j-log4j12
	private List<ArtifactWrapper> stackOverflowArtifacts1 = new ArrayList<ArtifactWrapper>();
	// org.slf4j:jcl-over-slf4j,org.slf4j:slf4j-jcl
	private List<ArtifactWrapper> stackOverflowArtifacts2 = new ArrayList<ArtifactWrapper>();
	// org.slf4j:jcl-over-slf4j, commons-logging:commons-logging
	private List<ArtifactWrapper> jCL_ConflictsArtifacts = new ArrayList<ArtifactWrapper>();
	// org/slf4j/impl/StaticLoggerBinder.class
	private List<ClzWrapper> sLF4J_Multiple_StaticLoggerBinder = new ArrayList<ClzWrapper>();

	// collect log related artifact
	public void collect(ArtifactWrapper artifactWrapper) { 
		if (artifactWrapper.artifact.getArtifactId().endsWith("log4j-over-slf4j")) {
			stackOverflowArtifacts1.add(artifactWrapper);
		}
		if (artifactWrapper.artifact.getArtifactId().endsWith("slf4j-log4j12")) {
			stackOverflowArtifacts1.add(artifactWrapper);
		}
		if (artifactWrapper.artifact.getArtifactId().endsWith("jcl-over-slf4j")) {
			stackOverflowArtifacts2.add(artifactWrapper);
			jCL_ConflictsArtifacts.add(artifactWrapper);
		}
		if (artifactWrapper.artifact.getArtifactId().endsWith("slf4j-jcl")) {
			stackOverflowArtifacts2.add(artifactWrapper);
		}
		if (artifactWrapper.artifact.getArtifactId().endsWith("commons-logging")) {
			jCL_ConflictsArtifacts.add(artifactWrapper);
		}
		return;
	}

	// collect log related class
	public void collect(ClzWrapper clz) {
		if (clz.className.equals("org/slf4j/impl/StaticLoggerBinder.class")) {
			sLF4J_Multiple_StaticLoggerBinder.add(clz);
		}
		return;
	}

	public List<LogConflict> getLogConflicts() {
		List<LogConflict> conflicts = new ArrayList<LogConflict>();
		Integer number = 1;
		if (stackOverflowArtifacts1.size() >= 2) {
			String a = "log4j-over-slf4j";
			boolean finda = false;
			String b = "slf4j-log4j12";
			boolean findb = false;
			for (Iterator<ArtifactWrapper> iterator = stackOverflowArtifacts1.iterator(); iterator.hasNext();) {
				ArtifactWrapper artifactWrapper = (ArtifactWrapper) iterator.next();
				if (artifactWrapper.artifact.getArtifactId().endsWith(a)) {
					finda = true;
				}
				if (artifactWrapper.artifact.getArtifactId().endsWith(b)) {
					findb = true;
				}
			}
			if (finda && findb) {
				for (Iterator<ArtifactWrapper> iterator = stackOverflowArtifacts1.iterator(); iterator.hasNext();) {
					ArtifactWrapper artifactWrapper = (ArtifactWrapper) iterator.next();
					LogConflict conflict = new LogConflict();
					conflict.setNumber(number);
					conflict.setLogConflictType(Constants.lOG_CONFLICT_TYPE_StackOverflow);
					conflict.setGroupId(artifactWrapper.artifact.getGroupId());
					conflict.setArtifactId(artifactWrapper.artifact.getArtifactId());
					conflict.setVersion(artifactWrapper.artifact.getVersion());
					conflict.setOriginFrom(Util.formatOriginFrom(artifactWrapper.originFrom));
					conflicts.add(conflict);
				}
				number++;
			}
		}
		if (stackOverflowArtifacts2.size() >= 2) {
			String a = "jcl-over-slf4j";
			boolean finda = false;
			String b = "slf4j-jcl";
			boolean findb = false;
			for (Iterator<ArtifactWrapper> iterator = stackOverflowArtifacts2.iterator(); iterator.hasNext();) {
				ArtifactWrapper artifactWrapper = (ArtifactWrapper) iterator.next();
				if (artifactWrapper.artifact.getArtifactId().endsWith(a)) {
					finda = true;
				}
				if (artifactWrapper.artifact.getArtifactId().endsWith(b)) {
					findb = true;
				}
			}
			if (finda && findb) {
				for (Iterator<ArtifactWrapper> iterator = stackOverflowArtifacts2.iterator(); iterator.hasNext();) {
					ArtifactWrapper artifactWrapper = (ArtifactWrapper) iterator.next();
					LogConflict conflict = new LogConflict();
					conflict.setNumber(number);
					conflict.setLogConflictType(Constants.lOG_CONFLICT_TYPE_StackOverflow);
					conflict.setGroupId(artifactWrapper.artifact.getGroupId());
					conflict.setArtifactId(artifactWrapper.artifact.getArtifactId());
					conflict.setVersion(artifactWrapper.artifact.getVersion());
					conflict.setOriginFrom(Util.formatOriginFrom(artifactWrapper.originFrom));
					conflicts.add(conflict);
				}
				number++;
			}
		}
		if (jCL_ConflictsArtifacts.size() >= 2) {
			String a = "jcl-over-slf4j";
			boolean finda = false;
			String b = "commons-logging";
			boolean findb = false;
			for (Iterator<ArtifactWrapper> iterator = jCL_ConflictsArtifacts.iterator(); iterator.hasNext();) {
				ArtifactWrapper artifactWrapper = (ArtifactWrapper) iterator.next();
				if (artifactWrapper.artifact.getArtifactId().endsWith(a)) {
					finda = true;
				}
				if (artifactWrapper.artifact.getArtifactId().endsWith(b)) {
					findb = true;
				}
			}
			if (finda && findb) {
				for (Iterator<ArtifactWrapper> iterator = jCL_ConflictsArtifacts.iterator(); iterator.hasNext();) {
					ArtifactWrapper artifactWrapper = (ArtifactWrapper) iterator.next();
					LogConflict conflict = new LogConflict();
					conflict.setNumber(number);
					conflict.setLogConflictType(Constants.lOG_CONFLICT_TYPE_JCL_Conflicts);
					conflict.setGroupId(artifactWrapper.artifact.getGroupId());
					conflict.setArtifactId(artifactWrapper.artifact.getArtifactId());
					conflict.setVersion(artifactWrapper.artifact.getVersion());
					conflict.setOriginFrom(Util.formatOriginFrom(artifactWrapper.originFrom));
					conflicts.add(conflict);
				}
				number++;
			}
		}
		if (sLF4J_Multiple_StaticLoggerBinder.size() >= 2) {
			for (Iterator<ClzWrapper> iterator = sLF4J_Multiple_StaticLoggerBinder.iterator(); iterator.hasNext();) {
				ClzWrapper clzWrapper = (ClzWrapper) iterator.next();
				LogConflict conflict = new LogConflict();
				conflict.setNumber(number);
				conflict.setLogConflictType(Constants.lOG_CONFLICT_TYPE_SLF4J_Multiple_StaticLoggerBinder);
				conflict.setGroupId(clzWrapper.artifactWrapper.artifact.getGroupId());
				conflict.setArtifactId(clzWrapper.artifactWrapper.artifact.getArtifactId());
				conflict.setVersion(clzWrapper.artifactWrapper.artifact.getVersion());
				conflict.setOriginFrom(Util.formatOriginFrom(clzWrapper.artifactWrapper.originFrom));
				conflicts.add(conflict);
			}
		}
		return conflicts;
	}
}
