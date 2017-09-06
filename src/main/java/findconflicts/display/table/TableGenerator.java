package findconflicts.display.table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * generator tabular format at java console
 * 
 * @author david
 *
 */
public class TableGenerator {
	private int PADDING_SIZE = 2;
	public static String CELL_NEW_LINE = "<br>";
	public static String NEW_LINE = "\n";
	public static String TABLE_JOINT_SYMBOL = "+";
	public static String TABLE_V_SPLIT_SYMBOL = "|";
	public static String TABLE_H_SPLIT_SYMBOL = "-";

	public String generateTable(List<?> data) {
		return this.generateTable(data, new DefaultValueRewrite());
	}

	public String generateTable(List<?> data, ValueRewrite valueRewrite) {
		if (data == null || data.size() == 0) {
			return null;
		}
		Object obj = data.get(0);
		Field[] fields = obj.getClass().getDeclaredFields();
		List<String> headersList = new ArrayList<String>();
		for (int i = 0; i < fields.length; i++) {
			headersList.add(fields[i].getName());
		}
		List<List<String>> rowsList = new ArrayList<List<String>>();
		for (Iterator<?> iterator = data.iterator(); iterator.hasNext();) {
			Object object = (Object) iterator.next();
			Map<Field, String[]> fieldValueData = new HashMap<Field, String[]>();
			int maxrow = 1;
			for (int i = 0; i < fields.length; i++) {
				Object value;
				try {
					fields[i].setAccessible(true);
					value = fields[i].get(object);
					if (valueRewrite != null) {
						value = valueRewrite.rewrite(fields[i], value);
					}
					if (value != null) {
						String[] values = value.toString().split(CELL_NEW_LINE);
						fieldValueData.put(fields[i], values);
						if (values.length > maxrow) {
							maxrow = values.length;
						}
					} else {
						fieldValueData.put(fields[i], new String[] { "" });
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			for (int i = 1; i <= maxrow; i++) {
				List<String> row = new ArrayList<String>();
				for (int j = 0; j < fields.length; j++) {
					String[] filedValue = fieldValueData.get(fields[j]);
					if (filedValue.length >= i) {
						row.add(filedValue[i - 1]);
					} else {
						row.add("");
					}
				}
				rowsList.add(row);
			}
		}
		return this.generateTable(headersList, rowsList);
	}

	private String generateTable(List<String> headersList, List<List<String>> rowsList) {
		StringBuilder stringBuilder = new StringBuilder();
		int rowHeight = 1;
		Map<Integer, Integer> columnMaxWidthMapping = getMaximumWidhtofTable(headersList, rowsList);
		stringBuilder.append(NEW_LINE);
		stringBuilder.append(NEW_LINE);
		createRowLine(stringBuilder, headersList.size(), columnMaxWidthMapping);
		stringBuilder.append(NEW_LINE);
		for (int headerIndex = 0; headerIndex < headersList.size(); headerIndex++) {
			fillCell(stringBuilder, headersList.get(headerIndex), headerIndex, columnMaxWidthMapping);
		}
		stringBuilder.append(NEW_LINE);
		createRowLine(stringBuilder, headersList.size(), columnMaxWidthMapping);
		for (List<String> row : rowsList) {
			for (int i = 0; i < rowHeight; i++) {
				stringBuilder.append(NEW_LINE);
			}
			for (int cellIndex = 0; cellIndex < row.size(); cellIndex++) {
				fillCell(stringBuilder, row.get(cellIndex), cellIndex, columnMaxWidthMapping);
			}
		}
		stringBuilder.append(NEW_LINE);
		createRowLine(stringBuilder, headersList.size(), columnMaxWidthMapping);
		stringBuilder.append(NEW_LINE);
		stringBuilder.append(NEW_LINE);
		return stringBuilder.toString();
	}

	private void fillSpace(StringBuilder stringBuilder, int length) {
		for (int i = 0; i < length; i++) {
			stringBuilder.append(" ");
		}
	}

	private void createRowLine(StringBuilder stringBuilder, int headersListSize, Map<Integer, Integer> columnMaxWidthMapping) {
		for (int i = 0; i < headersListSize; i++) {
			if (i == 0) {
				stringBuilder.append(TABLE_JOINT_SYMBOL);
			}
			for (int j = 0; j < columnMaxWidthMapping.get(i) + PADDING_SIZE * 2; j++) {
				stringBuilder.append(TABLE_H_SPLIT_SYMBOL);
			}
			stringBuilder.append(TABLE_JOINT_SYMBOL);
		}
	}

	private Map<Integer, Integer> getMaximumWidhtofTable(List<String> headersList, List<List<String>> rowsList) {
		Map<Integer, Integer> columnMaxWidthMapping = new HashMap<Integer, Integer>();
		for (int columnIndex = 0; columnIndex < headersList.size(); columnIndex++) {
			columnMaxWidthMapping.put(columnIndex, 0);
		}
		for (int columnIndex = 0; columnIndex < headersList.size(); columnIndex++) {
			if (headersList.get(columnIndex).length() > columnMaxWidthMapping.get(columnIndex)) {
				columnMaxWidthMapping.put(columnIndex, headersList.get(columnIndex).length());
			}
		}
		for (List<String> row : rowsList) {
			for (int columnIndex = 0; columnIndex < row.size(); columnIndex++) {

				if (row.get(columnIndex).length() > columnMaxWidthMapping.get(columnIndex)) {
					columnMaxWidthMapping.put(columnIndex, row.get(columnIndex).length());
				}
			}
		}
		for (int columnIndex = 0; columnIndex < headersList.size(); columnIndex++) {

			if (columnMaxWidthMapping.get(columnIndex) % 2 != 0) {
				columnMaxWidthMapping.put(columnIndex, columnMaxWidthMapping.get(columnIndex) + 1);
			}
		}
		return columnMaxWidthMapping;
	}

	private int getOptimumCellPadding(int cellIndex, int datalength, Map<Integer, Integer> columnMaxWidthMapping, int cellPaddingSize) {
		if (datalength % 2 != 0) {
			datalength++;
		}
		if (datalength < columnMaxWidthMapping.get(cellIndex)) {
			cellPaddingSize = cellPaddingSize + (columnMaxWidthMapping.get(cellIndex) - datalength) / 2;
		}
		return cellPaddingSize;
	}

	private void fillCell(StringBuilder stringBuilder, String cell, int cellIndex, Map<Integer, Integer> columnMaxWidthMapping) {
		int cellPaddingSize = getOptimumCellPadding(cellIndex, cell.length(), columnMaxWidthMapping, PADDING_SIZE);
		if (cellIndex == 0) {
			stringBuilder.append(TABLE_V_SPLIT_SYMBOL);
		}
		fillSpace(stringBuilder, cellPaddingSize);
		stringBuilder.append(cell);
		if (cell.length() % 2 != 0) {
			stringBuilder.append(" ");
		}
		fillSpace(stringBuilder, cellPaddingSize);
		stringBuilder.append(TABLE_V_SPLIT_SYMBOL);
	}

}
