package findconflicts.sort;

import java.util.Comparator;

/**
 * how to rank the result to show
 * 
 * @author david
 *
 */
public class ClassConflictsComparator implements Comparator<String> {

	//  rank by the length 
	public int compare(String obj1, String obj2) {
		if (obj1 == null || obj2 == null) {
			return 0;
		}
		int a = obj1.length();
		int b = obj2.length();
		if (a < b) {
			return -1;
		} else if (a == b) {
			return 0;
		} else {
			return 1;
		}
	}

}
