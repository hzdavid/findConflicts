package findconflicts.sort;

import java.util.Comparator;

import findconflicts.display.vo.JarConflictGroup;

/**
 * how to rank the result to show
 * @author david
 *
 */
public class JarConflictGroupComparator implements Comparator<JarConflictGroup> {

	// rank by the conflict ratio
	public int compare(JarConflictGroup a, JarConflictGroup b) {

		if (a.getConflictRatio() > b.getConflictRatio()) {
			return -1;
		} else if (a.getConflictRatio() == b.getConflictRatio()) {
			return 0;
		} else {
			return 1;
		}

	}

}
