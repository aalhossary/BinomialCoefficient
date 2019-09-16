package binomialCoefficient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class provides a way to access a table generated with the binomial
 * coefficient by the underlying indexes. It also determines the number of
 * unique combinations with the binomial coefficient. The binomial coefficient
 * is used to calculate the total number of unique combinations for a given set
 * of numbers (N), when grouped by K items at a time. Total number of unique
 * combinations = N! / ( K! (N - K)! ).
 * 
 * This class uses 32 bit integers and so is limited to 2^31 or 2,147,483,648
 * before it overflows.
 * 
 * This class was designed and originally written by Robert G. Bryan in April,
 * 2011. Updated on 4/2015 to fix cases involving N choose 1 and also includes a
 * new version called BinCoeffL that works with long values.
 * 
 * It is in the public domain. Even though it has been tested, the user assumes
 * full responsibility for any bugs or anomalies.
 * 
 * Retrieved from <a href=
 * "https://tablizingthebinomialcoeff.wordpress.com/">https://tablizingthebinomialcoeff.wordpress.com/</a>
 * 
 * @param <T>
 */
public class BinCoeff<T> {
	/** Total number of items. Equal to N. */
	private int N_NumItems;
	/** # of items in a group. Equal to K. */
	private int K_GroupSize;
	/** Total number of index tables. Equal to K - 1. */
	private int indexTablesCount;
	/** Total number of index tables minus 1. Equal to K - 2. */
	private int indexTablesCountMinus1;
	/** Total number of index tables minus 2. Equal to K - 3. */
	private int indexTablesCountMinus2;
	/** Total number of unique combinations. */
	private int totalCombos;
	/**
	 * Holds the indexes used to access the bin coeff table. This object is a list
	 * with each element containing an array of ints.
	 */
	private List<int[]> indexes;

	/**
	 * Holds a list of the objects the user wants to create. This table is
	 * optionally created if the user wants this class to manage the table data.
	 */
	private List<T> tableData;

	/**
	 * This constructor builds the index tables used to retrieve the index to the
	 * binomial coefficient table.
	 * 
	 * @param n         the number of items.
	 * @param k         the number of items in a group.
	 * @param initTable
	 */
	public BinCoeff(int n, int k, boolean initTable /* = false */) {
		long N1, K1, totalCombosL;
		// Validate the inputs.
		if (k < 1) {
			throw new IllegalArgumentException("BinCoeff:BinCoeff - input arg K < 1.");
		}
		if (n <= k) {
			throw new IllegalArgumentException("BinCoeff:BinCoeff - input arg N <= K.");
		}
		// Get the total number of unique combinations.
		indexTablesCount = k - 1;
		indexTablesCountMinus1 = indexTablesCount - 1;
		indexTablesCountMinus2 = indexTablesCountMinus1 - 1;
		N_NumItems = n;
		K_GroupSize = k;
		// indexTablesCount = K_GroupSize - 1; // redundant line. to delete later
		N1 = n;
		K1 = k;
		totalCombosL = getBinCoeff(N1, K1);
		if (totalCombosL > Integer.MAX_VALUE) {
			throw new RuntimeException("BinCoeff:BinCoeff - Total # of combos > 2GB.");
		}
		totalCombos = (int) totalCombosL;
		buildIndexes();
		if (initTable)
			initializeTable();
	}

	/**
	 * This function gets the total number of unique combinations based upon N and
	 * K. Total number of unique combinations = N! / ( K! (N - K)! ). For example,
	 * to get the total number of unique combinations for a 52 card deck in groups
	 * of 7, it should return 133,784,560.
	 * 
	 * @param n the total number of items.
	 * @param k the size of the group.
	 * @return
	 */
	public static int getBinCoeff(int n, int k) {
		if (k == 1)
			return n;
		int startNum, totalCombinations;
		startNum = n - k + 1; // N! / (N-K)!
		totalCombinations = startNum++;
		for (int i = startNum; i <= n; i++) {
			totalCombinations *= i;
		}
		int divisor = 2;
		for (int i = 3; i <= k; i++) {
			divisor *= i;
		}
		totalCombinations /= divisor;
		return totalCombinations;
	}

	/**
	 * This function gets the total number of unique combinations based upon N and
	 * K. N is the total number of items. K is the size of the group. Total number
	 * of unique combinations = N! / ( K! (N - K)! ). This function is less
	 * efficient, but is more likely to not overflow when N and K are large. Taken
	 * from: http://blog.plover.com/math/choose.html
	 * 
	 * 
	 * @param n
	 * @param k
	 * @return
	 */
	public static long getBinCoeff(long n, long k) {
		long r = 1;
		long d;
		if (k > n)
			return 0;
		for (d = 1; d <= k; d++) {
			r *= n--;
			r /= d;
		}
		return r;
	}

	/**
	 * This function creates each index that is used to obtain the index to the
	 * binomial coefficient table based upon the underlying K indexes.
	 */
	private void buildIndexes() {
		// If this is an N choose 1 case, then simply return since the indexes are not
		// used for these cases.
		if (K_GroupSize == 1)
			return;
		this.indexes = new ArrayList<int[]>(indexTablesCount);
		// Create the arrays used for each index.
		for (int i = 0; i < indexTablesCount; i++) {
			indexes.add(new int[N_NumItems - i]);
		}
		// Get the indexes values for the least significant index.
		int[] indexArrayLeast = indexes.get(indexTablesCountMinus1);
		int value = 1;
		int incValue = 2;
		for (int i = 2; i < indexArrayLeast.length; i++) {
			indexArrayLeast[i] = value;
			value += incValue++;
		}
		// Get the index values for the remaining indexes.
		value = 1;
		incValue = 2;
		int startIndex = 3;
		int endIndex = N_NumItems - indexTablesCountMinus2;
		for (int i = indexTablesCountMinus2; i >= 0; i--) {
			int[] indexArrayPrev = indexes.get(i + 1);
			int[] indexArray = indexes.get(i);
			indexArray[startIndex] = 1;
			for (int j = startIndex + 1; j < endIndex; j++) {
				indexArray[j] = indexArray[j - 1] + indexArrayPrev[j - 1];
			}
			startIndex++;
			endIndex++;
		}
	}

	/**
	 * This function returns the proper index to an entry in the sorted binomial
	 * coefficient table from the underlying values in KIndexes. For example, for
	 * the 13 chooose 5 example which corresponds to 5 card poker hand ranks, then
	 * AKQJT (which is the greatest hand in the table) would be passed as value 12,
	 * 11, 10, 9, and 8, and the return value would be 1286, which is the highest
	 * element. Note that if the Sorted flag is false, then the values in KIndexes
	 * will be put into sorted order and returned that way. The sorted flag must be
	 * set to false if KIndexes is not in descending order.
	 * 
	 * @param sorted
	 * @param kIndexes
	 * @return
	 */
	public int getIndex(boolean sorted, int[] kIndexes) {
		// Handle the N choose 1 case.
		if (K_GroupSize == 1) {
			return kIndexes[0];
		}
		int index = 0;
		if (!sorted) {
			sortDescending(kIndexes);
		}
		for (int i = 0; i < K_GroupSize - 1; i++) {
			int[] indexArray = indexes.get(i);
			index += indexArray[kIndexes[i]];
		}
		index += kIndexes[K_GroupSize - 1];
		return index;
	}

	/**
	 * This function returns the proper K indexes from an index to the sorted
	 * binomial coefficient table. This is the reverse of the GetIndex function. The
	 * correct K indexes are returned in descending order in KIndexes.
	 * 
	 * @param index
	 * @param kIndexes
	 */
	public void getKIndexes(int index, int[] kIndexes) {
		// Handle the N choose 1 case.
		if (K_GroupSize == 1) {
			kIndexes[0] = index;
			return;
		}
		int remValue = index;
		for (int i = 0; i < K_GroupSize - 1; i++) {
			int[] indexArray = indexes.get(i);
			for (int j = indexArray.length - 1; j >= 0; j--) {
				if (remValue >= indexArray[j]) {
					kIndexes[i] = j;
					remValue -= indexArray[j];
					break;
				}
			}
		}
		kIndexes[K_GroupSize - 1] = remValue;
	}

	/**
	 * This function returns the proper K indexes from an index to the sorted
	 * binomial coefficient table. This is the reverse of the GetIndex function. The
	 * correct K indexes are returned in descending order in KIndexes.
	 * 
	 * @param index
	 * @param kIndexes
	 */
	public void getKIndexes(int index, List<Long> kIndexes) {
		// Handle the N choose 1 case.
		if (K_GroupSize == 1) {
			kIndexes.set(0, Long.valueOf(index));
			return;
		}
		int remValue = index;
		for (int i = 0; i < K_GroupSize - 1; i++) {
			int[] indexArray = indexes.get(i);
			for (int j = indexArray.length - 1; j >= 0; j--) {
				if (remValue >= indexArray[j]) {
					kIndexes.set(i, Long.valueOf(j));
					remValue -= indexArray[j];
					break;
				}
			}
		}
		kIndexes.set(K_GroupSize - 1, Long.valueOf(remValue));
	}

	public void initializeTable() {
		// This function creates an array of the type specified by the user.
		tableData = new ArrayList<T>(totalCombos);
	}

	public List<T> getTable() {
		// This access function is provided so that the user can work on it when needed.
		return tableData;
	}

	public void addItem(T obj) {
		// Adds the specified object to the end of the list.
		tableData.add(obj);
	}

	/**
	 * Adds the specified object to the table at the specified Index. This function
	 * is less efficient than the one above. It is provided for flexibility to init
	 * the table in a non-linear order.
	 * 
	 * @param index
	 * @param obj
	 */
	public void addItem(int index, T obj) {
		int n;
		if (index >= tableData.size()) {
			n = index - tableData.size() + 1;
			for (int i = 0; i < n; i++) {
				tableData.add(obj);
			}
		} else
			tableData.set(index, obj);
	}

	/**
	 * Adds the specified object to the table based upon the K indexes.
	 * 
	 * @param sorted
	 * @param kIndexes
	 * @param obj
	 */
	public void addItem(boolean sorted, int[] kIndexes, T obj) {
		int index = getIndex(sorted, kIndexes);
		addItem(index, obj);
	}

	/**
	 * Gets the specified object stored in tableData.
	 * 
	 * @param index
	 * @return
	 */
	public T getItem(int index) {
		return tableData.get(index);
	}

	/**
	 * Gets the specified object in tableData based upon the K indexes.
	 * 
	 * @param sorted
	 * @param kIndexes
	 * @return
	 */
	public T getItem(boolean sorted, int[] kIndexes) {
		int index = getIndex(sorted, kIndexes);
		return tableData.get(index);
	}

	/**
	 * This function writes out the K indexes in sorted order.
	 * 
	 * @param filePath       path & name of file.
	 * @param dispChars      if not null, then the string in DispChars is displayed
	 *                       instead of the numeric value of the corresponding K
	 *                       index.
	 * @param sep            String used to separate each individual K index in a
	 *                       group.
	 * @param groupSep       String used to separate each KIndex group.
	 * @param maxCharsInLine maximum number of chars in an output line.
	 * @param ascOrder       true means the K indexes are written out in ascending
	 *                       order.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void outputKIndexes(String filePath, String[] dispChars, String sep, String groupSep, int maxCharsInLine,
			boolean ascOrder) throws FileNotFoundException, IOException {
		int startPos, endPos, inc, prevLength = 0;
		int maxCharsInN = N_NumItems / 10 + 1;
		int[] kIndex = new int[K_GroupSize];
		String s, s1;
		s1 = "%" + maxCharsInN + "s";
		StringBuilder sb = new StringBuilder();
		PrintStream out = new PrintStream(filePath);
		// Set to output in ascending or descending order depending on how AscOrder is
		// set.
		if (ascOrder) {
			startPos = 0;
			endPos = totalCombos;
			inc = 1;
		} else {
			startPos = totalCombos - 1;
			endPos = -1;
			inc = -1;
		}
		// Output the K Indexes
		for (int i = startPos; i != endPos; i += inc) {
			getKIndexes(i, kIndex);
			for (int j = 0; j < K_GroupSize; j++) {
				final int n = kIndex[j];
				if (dispChars != null)
					s = dispChars[n];
				else {
					// s1 = "{0, " + maxCharsInN + "}";
					s = String.format(s1, n);
				}
				// S = String.Format("{0,MaxCharsInN}", n);
				sb.append(s);
				if (j < indexTablesCount)
					sb.append(sep);
			}
			if (sb.length() >= maxCharsInLine) {
				out.println(sb.substring(0, prevLength));
				sb.delete(0, prevLength);
			}
			sb.append(groupSep);
			prevLength = sb.length();
		}
		out.println(sb.toString());
		out.close();
	}

	/**
	 * This function writes out the K indexes in sorted order.
	 * 
	 * @param dispChars if not null, then the string in DispChars is displayed
	 *                  instead of the numeric value of the corresponding K index.
	 * @param sep       String used to separate each individual K index in a group.
	 * @param ascOrder  true means the K indexes are written out in ascending order.
	 * @param outList   appends the results to this list.
	 */
	public void outputKIndexes(String dispChars, String sep, boolean ascOrder, List<String> outList) {
		int n;
		int startPos, endPos, inc;
		int maxCharsInN = N_NumItems / 10 + 1;
		int[] kIndex = new int[K_GroupSize];
		String s, s1;
		s1 = "%" + maxCharsInN + "s";
		StringBuilder sb = new StringBuilder();
		// Set to output in ascending or descending order depending on how AscOrder is
		// set.
		if (ascOrder) {
			startPos = 0;
			endPos = totalCombos;
			inc = 1;
		} else {
			startPos = totalCombos - 1;
			endPos = -1;
			inc = -1;
		}
		// Output the K Indexes
		for (int i = startPos; i != endPos; i += inc) {
			// if (Loop == 998)
			// n = 0; // debug
			getKIndexes(i, kIndex);
			for (int j = K_GroupSize - 1; j >= 0; j--) {
				n = kIndex[j];
				if (dispChars != null)
					s = dispChars.substring(n, n + 1);
				else {
					s = String.format(s1, n);
				}
				// S = String.Format("{0,MaxCharsInN}", n);
				sb.append(s);
				if (j < indexTablesCount)
					sb.append(sep);
			}
			outList.add(sb.toString());
			sb.setLength(0);
		}
	}

	/** This access function is provided for testing purposes. */
	public List<int[]> getInternalIndexes() {
		return indexes;
	}

	public static void sortDescending(int[] kIndexes) {
		Integer[] integers = new Integer[kIndexes.length];
		for (int i = 0; i < integers.length; i++) {
			integers[i] = kIndexes[i];
		}
		Arrays.sort(integers, (e1, e2) -> (e2 - e1));
		for (int i = 0; i < integers.length; i++) {
			kIndexes[i] = integers[i];
		}
	}
	public static void sortDescending(long[] kIndexes) {
		Long[] longs = new Long[kIndexes.length];
		for (int i = 0; i < longs.length; i++) {
			longs[i] = kIndexes[i];
		}
		Arrays.sort(longs, (e1, e2) -> Long.signum((e2 - e1)));
		for (int i = 0; i < longs.length; i++) {
			kIndexes[i] = longs[i];
		}
	}

	public static void main(String[] args) {
		int n = 7, k = 3;
		BinCoeff<Long> binCoeff = new BinCoeff<>(n, k, true);
		int ncr = getBinCoeff(n, k);
		for (int i = ncr - 1; i >= 0; i--) {
			int[] kIndexes = new int[k];
			binCoeff.getKIndexes(i, kIndexes);
			System.out.format("%2d\t%s\n", i, Arrays.toString(kIndexes));
		}
	}

}
