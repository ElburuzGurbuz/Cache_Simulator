package HW3;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Scanner;

//Ahmet Elburuz Gürbüz 150116024

public class AhmetElburuzGurbuz {
	public static void main(String[] args) throws IOException {
		cacheLab caches = new cacheLab(Integer.parseInt(args[1]),Integer.parseInt(args[3]),
									   Integer.parseInt(args[5]),Integer.parseInt(args[7]),
									   Integer.parseInt(args[9]),Integer.parseInt(args[11]));
		caches.traceFile("test_large.trace");
		cache.writer(cacheLab.L1I, "L1I.txt");
		cache.writer(cacheLab.L1D, "L1D.txt");
		cache.writer(cacheLab.L2, "L2.txt");
	}
}
class cacheLab {
	int L1s;
	int L1S;
	int L1E;
	int L1b;
	int L1B;
	int L2s;
	int L2S;
	int L2E;
	int L2b;
	int L2B;
	static cache L1I;
	static cache L1D;
	static cache L2;

	public cacheLab(int L1s, int L1E, int L1b, int L2s, int L2E, int L2b) {
		L1I = new cache(L1s, L1E, L1b);
		L1D = new cache(L1s, L1E, L1b);
		L2 = new cache(L2s, L2E, L2b);
		this.L1s = L1s;
		this.L1E = L1E;
		this.L1b = L1b;
		this.L2s = L2s;
		this.L2E = L2E;
		this.L2b = L2b;
	}

	public static void traceFile(String filename) throws IOException {
		Scanner sc = null;
		sc = new Scanner(new File(filename));
		String address;
		String operation;
		String data = "";
		String size = null;

		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			Scanner sc2 = new Scanner(line);
			operation = sc2.next();
			address = sc2.next();
			address = address.substring(0, address.length() - 1);
			size = sc2.next();

			if (size.substring(size.length() - 1).equals(",")) {
				size = size.substring(0, size.length() - 1);
				data = sc2.next();
			} else {
				data = null;
			}
			trace t = new trace(operation, address, size, data);
			System.out.println(t.operation + " " + t.address + " " + t.size + " " + t.data);
			Operations(t);
			if (Integer.parseInt(t.size) == 0) {
				System.out.println("No operation size is zero.");
			}
			System.out.println();
			// sc2.close();
		}
		System.out.println(
				"\nL1I-hits:" + L1I.HitCounter + " L1I-misses:" + L1I.MissCounter + " L1I-evictions:" + L1I.Eviction);
		System.out.println(
				"L1D-hits:" + L1D.HitCounter + " L1D-misses:" + L1D.MissCounter + " L1D-evictions:" + L1D.Eviction);
		System.out
				.println("L2-hits:" + L2.HitCounter + " L2-misses:" + L2.MissCounter + " L2-evictions:" + L2.Eviction);
		System.out.println();
		// sc.close();
	}

	public static void Operations(trace trace) throws IOException {
		if (Integer.parseInt(trace.size) > 0) {
			if (trace.operation.equals("I") || trace.operation.equals("L")) {
				Instruction_Or_Load(trace.address, trace.operation);
			} else {
				Store_Or_Modify(trace.address, trace.size, trace.data, trace.operation);
			}
		}
	}

	public static void Instruction_Or_Load(String address, String traceOperation) throws IOException {
		boolean searchInL1_I = false, searchInL1_D = false, searchInL2 = false;
		int s1, s2;

		if (traceOperation.equals("I")) {
			s1 = setIndexValue(address, L1I);
			searchInL1_I = searchInCache(address, L1I);
			if (searchInL1_I == true) {
				L1I.HitCounter++;
				System.out.print("L1 hit,");
			} else {
				System.out.print("L1 miss,");
				L1I.MissCounter++;
				writeCache(address, L1I);
			}
		} else {
			s1 = setIndexValue(address, L1D);
			searchInL1_D = searchInCache(address, L1D);
			if (searchInL1_D == true) {
				L1D.HitCounter++;
				System.out.print("L1 hit, ");
			} else {
				L1D.MissCounter++;
				System.out.print("L1 miss,");
				writeCache(address, L1D);

			}
		}
		s2 = setIndexValue(address, L2);
		searchInL2 = searchInCache(address, L2);
		if (searchInL2 == true) {
			L2.HitCounter++;
			System.out.println("L2 hit.");
		} else {
			L2.MissCounter++;
			System.out.println("L2 miss.");
			writeCache(address, L2);
		}
		if (L2.S > 1) {
			System.out.print("Place in L2 set " + s2 + ",");
		} else {
			System.out.print("Place in L2,");
		}
		if (traceOperation.equals("I")) {
			if (L1I.S > 1) {
				System.out.print("L1I set" + s1);
				System.out.println();
			} else {
				System.out.print("L1I.");
				System.out.println();
			}
		} else {
			if (L1D.S > 1) {
				System.out.print("L1D set" + s1);
				System.out.println();
			} else {
				System.out.print("L1D.");
				System.out.println();
			}
		}
	}

	public static void Store_Or_Modify(String address, String size, String data, String traceOperation)
			throws IOException {
		boolean searchInL1_D, searchInL2;

		searchInL1_D = searchInCache(address, L1D);
		searchInL2 = searchInCache(address, L2);

		if (searchInL1_D == true) {
			String staleData = null;
			String first = null;
			String last = null;
			String modifyData = null;
			int s1 = setIndexValue(address, L1D);
			int t1 = calTag(address, L1D);
			int dec_add = Integer.parseInt(address, 16);
			int start = dec_add % L1D.B;

			for (int i = 0; i < L1D.E; i++) {
				if (L1D.cacheSet[s1].line[i].tag == t1 && L1D.cacheSet[s1].line[i].v == true) {
					L1D.HitCounter++;
					System.out.print("L1D hit, ");
					staleData = L1D.cacheSet[s1].line[i].data;
					first = staleData.substring(0, (2 * start));
					last = staleData.substring(2 * start + (2 * Integer.parseInt(size)), staleData.length());
					modifyData = first + data + last;
					L1D.cacheSet[s1].line[i].data = modifyData;
				}
			}
		} else {
			if (traceOperation.equals("M")) {
				L1D.MissCounter++;
				System.out.print("L1D miss, ");
				writeCache(address, L1D);
			}
		}

		if (searchInL2 == true) {
			String staleData = null;
			String first = null;
			String last = null;
			String modifyData = null;
			int s2 = setIndexValue(address, L2);
			int t2 = calTag(address, L2);
			int dec_add = Integer.parseInt(address, 16);
			int start = dec_add % L2.B;

			for (int i = 0; i < L2.E; i++) {
				if (L2.cacheSet[s2].line[i].tag == t2 && L2.cacheSet[s2].line[i].v == true) {
					L2.HitCounter++;
					System.out.println("L2 hit. ");
					staleData = L2.cacheSet[s2].line[i].data;
					first = staleData.substring(0, (2 * start));
					last = staleData.substring(2 * start + (2 * Integer.parseInt(size)), staleData.length());
					modifyData = first + data + last;
					L2.cacheSet[s2].line[i].data = modifyData;
				}
			}

		} else {
			if (traceOperation.equals("M")) {
				L2.MissCounter++;
				System.out.println("L2 miss ");
				writeCache(address, L2);
			}
		}

		updateRAM(address, data);

		if (traceOperation.equals("M")) {
			System.out.print("Modify in ");
			if (searchInL1_D == true) {
				System.out.print("L1D, ");
			}
			if (searchInL2 == true) {
				System.out.print("L2, ");
			}
			System.out.println("RAM ");
		} else if (traceOperation.equals("S")) {
			System.out.print("Store in ");
			if (searchInL1_D == true) {
				System.out.print("L1D, ");
			}
			if (searchInL2 == true) {
				System.out.print("L2, ");
			}
			System.out.println("RAM ");
		}
	}

	public static boolean searchInCache(String add, cache cache1) {
		int s1 = setIndexValue(add, cache1);
		int t1 = calTag(add, cache1);
		for (int i = 0; i < cache1.E; i++) {
			if (cache1.cacheSet[s1].line[i].tag == t1 && cache1.cacheSet[s1].line[i].v == true) {
				return true;
			}
		}
		return false;
	}

	public static void writeCache(String address, cache c) throws IOException {
		int sw = setIndexValue(address, c);
		int tw = calTag(address, c);
		int cnt = 0;
		for (int i = 0; i < c.E; i++) {
			if (c.cacheSet[sw].line[i].v == false) {
				cnt++;
				c.cacheSet[sw].line[i].tag = tw;
				c.cacheSet[sw].line[i].index = sw;
				c.cacheSet[sw].line[i].data = TakeDataRam("ram.txt", address, c);
				c.cacheSet[sw].line[i].v = true;
				c.timer++;
				c.cacheSet[sw].line[i].time = c.timer;
				c.cacheSet[sw].lineMinTime = returnMinTimeIndex(c, sw);

				break;
			}
		}
		if (cnt == 0) {
			c.cacheSet[sw].line[c.cacheSet[sw].lineMinTime].data = TakeDataRam("ram.txt", address, c);
			c.cacheSet[sw].line[c.cacheSet[sw].lineMinTime].tag = tw;
			c.cacheSet[sw].line[c.cacheSet[sw].lineMinTime].index = sw;
			c.cacheSet[sw].line[c.cacheSet[sw].lineMinTime].v = true;
			c.timer++;
			c.cacheSet[sw].line[c.cacheSet[sw].lineMinTime].time = c.timer;
			c.cacheSet[sw].lineMinTime = returnMinTimeIndex(c, sw);
			c.Eviction++;
		}
	}

	public static int returnMinTimeIndex(cache c, int setIndex) {
		int min = c.cacheSet[setIndex].line[0].time;
		int minindex = 0;
		for (int i = 0; i < c.E; i++) {
			if (c.cacheSet[setIndex].line[i].time < min) {
				min = c.cacheSet[setIndex].line[i].time;
				minindex = i;
			}
		}
		return minindex;

	}

	public static String TakeDataRam(String RAMfile, String address, cache c) throws IOException {
		int decimalAddress = Integer.parseInt(address, 16);
		int r = decimalAddress % c.B;
		int go = decimalAddress - r;
		RandomAccessFile file = new RandomAccessFile(RAMfile, "r");
		file.seek(go * 3); // EB 2A ... 3 er 3 er gidiyor
		byte[] bytes = new byte[(c.B) * 3];
		file.read(bytes);
		String s = new String(bytes);
		return s;
	}

	public static void updateRAM(String address, String data) throws IOException {
		String modifyData = "";
		for (int i = 0; i < data.length(); i += 2) {
			modifyData += data.substring(i, i + 2);
			modifyData += " ";
		}

		RandomAccessFile file = new RandomAccessFile("ram.txt", "rw");
		file.seek(Integer.parseInt(address, 16) * 3);
		file.write(modifyData.getBytes());

	}

	public static int calTag(String address, cache c) {
		int tagval = 0;
		int dec_add = Integer.parseInt(address, 16);
		int[] bin = decimaltoBinary(dec_add, c);
		int[] tagbin = new int[bin.length - c.s - c.b];
		for (int i = 0; i < tagbin.length; i++) {
			tagbin[i] = bin[i];
		}
		String n = Arrays.toString(tagbin).replaceAll("\\[|\\]|,|\\s", "");
		tagval = Integer.parseInt(n, 2);
		return tagval;

	}

	public static int setIndexValue(String address, cache c) {
		int setnumber = 0;
		int dec_add = Integer.parseInt(address, 16);
		int[] bin = decimaltoBinary(dec_add, c);
		int[] setbin = new int[c.s];

		for (int i = bin.length - (c.s + c.b), j = 0; i < (bin.length - c.b); i++, j++) {
			setbin[j] = bin[i];

		}

		if (c.s > 0) {
			String n = Arrays.toString(setbin).replaceAll("\\[|\\]|,|\\s", "");
			setnumber = Integer.parseInt(n, 2);
		} else {
			setnumber = 0;
		}

		return setnumber;

	}

	public static int[] decimaltoBinary(int decimal, cache c) {
		int counter = 0;
		int number = decimal;
		while (number > 0) {
			number = number / 2;
			counter++;
		}
		int size = 0;
		if (counter > (c.b + c.s)) {
			size = counter;
		} else {
			size = (c.b + c.s) + 1;
		}
		int[] binaryNum = new int[size];
		int i = 0;
		while (decimal > 0) {
			binaryNum[i] = decimal % 2;
			decimal = decimal / 2;
			i++;

		}

		int[] swapped = new int[size];
		int j = 0;
		for (i = swapped.length - 1; i >= 0; i--) {
			swapped[j] = binaryNum[i];
			j++;

		}

		return swapped;
	}
}



class cache {
	int s = 0;
	int S = 0;
	int b = 0;
	int E = 0;

	int HitCounter = 0;
	int MissCounter = 0;
	int Eviction = 0;
	int B = 0;
	set[] cacheSet = null;
	int timer = 0;

	public cache(int s, int E, int b) {
		this.s = s;
		this.b = b;
		this.E = E;
		this.B = (int) Math.pow(2, b);
		this.S = (int) Math.pow(2, s);
		cacheSet = new set[S];
		for (int i = 0; i < S; i++) {
			cacheSet[i] = new set(E);
		}

	}

	public static void writer(cache l, String filename) throws IOException {
		FileWriter fw = new FileWriter(filename);
		for (int i = 0; i < l.S; i++)
			for (int j = 0; j < l.E; j++)
				fw.write(l.cacheSet[i].line[j].tag + " " + l.cacheSet[i].line[j].time + " " + l.cacheSet[i].line[j].v
						+ " " + l.cacheSet[i].line[j].data + "\n");
		fw.close();
	}

	public static void writeToPosition(String filename, String data1, long position) throws IOException {
		RandomAccessFile writer = new RandomAccessFile(filename, "rw");
		writer.seek(position);
		writer.writeChars(data1);
		writer.close();
	}

}


class line {
	String data = null;
	int tag = 0;
	int time = 0;
	boolean v = false;
	int index;

	public line(String data, int tag, int time, boolean v, int index) {
		this.data = data;
		this.tag = tag;
		this.time = time;
		this.v = v;
		this.index = index;
	}

}

class set {
	int E = 0;
	line line[] = null;

	int lineMinTime = 0;

	public set(int E) {
		this.E = E;
		this.line = new line[E];

		for (int i = 0; i < E; i++) {
			this.line[i] = new line(null, 0, 0, false, i);
		}
	}
}

class trace {
	String address = null;
	String operation = null;
	String size = null;
	String data = null;

	public trace(String operation, String address, String size, String data) {
		this.address = address;
		this.operation = operation;
		this.size = size;
		this.data = data;

	}

	public static char[] convertToBinary(String hexaDecimal, int arrayDimension) {
		char[] newStr = new char[arrayDimension];
		int index = arrayDimension - 1;
		for (int i = hexaDecimal.length() - 1; i >= 0; i--) {

			int numberOfDigit = 0;
			int bit = 0;
			if (hexaDecimal.charAt(i) >= 48 && hexaDecimal.charAt(i) <= 57) {
				bit = hexaDecimal.charAt(i) % 48;
			} else if (hexaDecimal.charAt(i) == 32) {
				continue;
			} else {
				bit = hexaDecimal.charAt(i) % 87;
			}

			int division = bit;
			int remainder = 0;
			if (bit >= 2) {
				division = bit / 2;

				remainder = bit % 2;

				newStr[index] = ((char) (remainder + 48));
				numberOfDigit++;
				index--;
			}

			for (int j = 1; j > 0; j++) {
				if (division >= 2) {
					remainder = division % 2;
					newStr[index] = ((char) (remainder + 48));
					index--;
					numberOfDigit++;
				}
				if (division >= 2) {
					division = division / 2;
				} else {
					newStr[index] = ((char) (division + 48));

					index--;
					numberOfDigit++;

					while (numberOfDigit < 4) {
						newStr[index] = ((char) (48));
						index--;
						numberOfDigit++;
					}
					if (index != 0) {

						break;
					}
					if (index == 0) {
						break;
					}
				}

			}

		}
		return newStr;
	}
}