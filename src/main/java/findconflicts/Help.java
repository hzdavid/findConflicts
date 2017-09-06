package findconflicts;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * helper
 * 
 * @author david
 *
 */
@Mojo(name = "help", requiresProject = false)
public class Help extends AbstractMojo {

	public void execute() throws MojoExecutionException {
		StringBuilder builder = new StringBuilder();
		builder.append("Usage: mvn fc:go -DgroupId=xx,yy -DartifactId=aa,bb -Dshow.class.conflicts=true -DversionCheckConfig=${basedir}/versionCheck.pb \n");
		builder.append("Pparameters:\n");
		builder.append("  groupId             which groupId will be under checking . if null ,all groupdId. if multiply, use comma to  split it like xx,yy  \n");
		builder.append("  artifactId          which artifactId will be under checking . if null ,all artifactId. if multiply, use comma to split it  like aa,bb.\n");
		builder.append("  show.class.conflicts           whether you'd like to show class conflicts. the default is false\n");
		builder.append("  versionCheckConfig  the version check config file,like https://github.com/hzdavid/findConflicts/tree/master/src/main/resources/versionCheck.pb \n");
		builder.append("Author: david \n");
		builder.append("If you have any suggestions,you can send me email at hzdavid2009@gmail.com.\n");
		this.getLog().info("\n" + builder.toString());

	}

}
