package findconflicts.display.table;

import java.lang.reflect.Field;

/**
 * rewrite the value ,especially change the format of the value 
 * @author david
 *
 */
public interface ValueRewrite {

	public Object rewrite(Field field,Object value);
}
