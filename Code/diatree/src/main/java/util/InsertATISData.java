package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeSet;

public class InsertATISData {
	
	ArrayList<Instance> data;
	TreeSet<String> attributes;
	Instance current;
	String currentType;
	String path = "/home/casey/corpora/ATIS/atoms/";
	
	public void run() throws FileNotFoundException {
		attributes = new TreeSet<String>();
		data = new ArrayList<Instance>();
		
		gatherData(path+"trainAtis.atoms");
		gatherData(path+"devAtis.train.atoms");
	}
	
	
	private void process(String line) {
		if (line.isEmpty()) return;
		if (line.equals(">>")) {
			if (current != null) {
				current.clearEnds();
				data.add(current);
			}
			current = new Instance();
		}
		else if (line.contains(">slot")) {
			currentType = "slot";
			return;
		}
		else if (line.contains(">pos")) {
			currentType = "pos";
			return;			
		}		
		else if (line.contains(">cass")) {
			currentType = "cass";
			return;			
		}			
		else if (line.contains(">word")) {
			currentType = "word";
			return;			
		}		
		else if (line.contains(">goal")) {
			currentType = "goal";
			return;			
		}
		else if (line.startsWith(">")) {
			currentType = null;
			return;			
		}
		
		if (currentType != null)
			current.add(currentType, line);			

	}
	

	
	private void gatherData(String string) throws FileNotFoundException {
		Scanner scan = new Scanner(new File(string));
		String line = "";
		while (scan.hasNext()){
			line = scan.nextLine();
			process(line);
		}
		scan.close();	

	}
	
	public static void main(String[] args) {
		try {
			new InsertATISData().run();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public class Instance {
		HashMap<String, HashMap<Integer,String>> maps = new HashMap<String, HashMap<Integer,String>>();
		String goal;
		
		public Instance() {
			maps.put("word", new HashMap<Integer,String>());
		}
		
		public void add(String type, String line) {
			if (type.equals("goal")) {
				this.goal = clean(line);
				return;
			}
			
			String[] split = line.split("\\s+");
			Integer i = Integer.parseInt(clean(split[0]));
			if (!maps.containsKey(type)) {
				maps.put(type, new HashMap<Integer,String>());
			}
			HashMap<Integer,String> temp = maps.get(type);
			temp.put(i, clean(split[1]));
			maps.put(type, temp);
		}
		
		public void clearEnds() {
			int start = 0;
			int end = 0;
			for (Integer i : getWordsMap().keySet()) {
				String word = getWordsMap().get(i);
				if (word.equals("END")) 
					end = i;
			}
			
			for (String type : maps.keySet()) {
				maps.get(type).remove(start);
				maps.get(type).remove(end);
			}
		}
		
		private String clean(String line) {
			String newLine = line.trim();
			newLine = newLine.replace("\"", "");			
			return newLine;
		}
		
		public String toString() {
			return goal + " " + maps;
		}

		public HashMap<Integer, String> getWordsMap() {
			return maps.get("word");
		}
		
		public HashMap<Integer, String> getSlotsMap() {
			return maps.get("slot");
		}
		
		public HashMap<Integer, String> getPOSMap() {
			return maps.get("pos");
		}		
		
		public HashMap<Integer, String> getCassMap() {
			return maps.get("cass");
		}	
		
		public TreeSet<Integer> getIndeces() {
			TreeSet<Integer> orderedWords = new TreeSet<Integer>();
			orderedWords.addAll(getWordsMap().keySet());
			return orderedWords;
		}		
		
		public String getSentence() {
			String temp = "";
			for (Integer i : getIndeces()) {
				temp += getWordsMap().get(i) + " ";				
			}
			return temp.trim();
		}
		
	}

}
