package findconflicts.display.table;

import java.lang.reflect.Field;

import findconflicts.Constants;
import findconflicts.Util;

/**
 * default implementation of ValueRewrite
 * 
 * @author david
 *
 */
public class DefaultValueRewrite implements ValueRewrite {
	public Object rewrite(Field field, Object value) {
		if (field.getName().equals("groupId")) {
			if (value != null) {
				return Util.makeShort(value.toString(), Constants.GROUPID_MAX_WIDTH);
			}
		}
		if (field.getName().equals("artifactId")) {
			if (value != null) {
				return Util.makeShort(value.toString(), Constants.ARTIFACTID_MAX_WIDTH);
			}
		}
		if (field.getName().equals("version")) {
			if (value != null) {
				return Util.makeShort(value.toString(), Constants.VERSION_MAX_WIDTH);
			}
		}
		if (field.getName().equals("className")) {
			if (value != null) {
				return Util.makeShort(value.toString(), Constants.CLASS_MAX_WIDTH);
			}
		}
		if (field.getName().equals("requiredVersion")) {
			if (value != null) {
				return Util.makeShort(value.toString(), Constants.VERSION_MAX_WIDTH);
			}
		}
		return value;
	}

}
