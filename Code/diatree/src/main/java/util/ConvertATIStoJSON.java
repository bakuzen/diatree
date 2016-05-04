package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ConvertATIStoJSON {
	
	ArrayList<Instance> data;
	Instance current;
	String currentType;
	private HashMap<String,JSONObject> jsonIntents;
	private String domain = "atis";
	String path = "/home/casey/corpora/ATIS/atoms/";
	
	public void run() throws SQLException, JSONException, IOException {
		data = new ArrayList<Instance>();
		jsonIntents = new HashMap<String,JSONObject>();
		
		gatherData(path+"trainAtis.atoms");
//		gatherData(path+"devAtis.train.atoms");
		combineData();
		
//		db = new Domain();
//		db.createNewDomain(domain);
//		db.setDomain(domain);
		
		convertToJSON();
		
		writeFiles();

//		DomainModel model = new DomainModel(db, domain);
//		model.train();
	}
	
	
	private void combineData() {
		
		
		
		for (Instance in : data) {
			
			ArrayList<Integer> toRemove = new ArrayList<Integer>();
			Integer prev = null;
			for (Integer n : in.getIndeces()) {
				if (prev != null) {
					if (in.getSlotsMap().get(n).equals(in.getSlotsMap().get(prev))) {
						toRemove.add(prev);
						in.getWordsMap().put(n, in.getWordsMap().get(prev) + " " + in.getWordsMap().get(n));
					}
				}
				prev = n;
			}
			for (Integer n : toRemove) {
				in.getSlotsMap().remove(n);
				in.getWordsMap().remove(n);
			}
		}
		
	}


	private void writeFiles() throws IOException, JSONException {
		for (String s : jsonIntents.keySet()) {
			if (((JSONArray)jsonIntents.get(s).get("concepts")).length() < 2) continue;
			FileWriter f = new FileWriter("domains/" + domain +"/json/" + s + ".json");
			f.write(jsonIntents.get(s).toString(2));
			f.close();
		}
		
	}


	private void convertToJSON() throws SQLException, JSONException {
		
//		offerIntent("goal");
		offerIntent("intent");
		
		for (Instance in : data) {
//			deal with goal
			
//			offerConcept("goal", in.goal);
			offerConcept("intent", in.goal);
			offerProperty("intent", in.goal, in.goal);
//			deal with everything else
			for (Integer n : in.getIndeces()) {
				String word = in.getWordsMap().get(n).replace("'", "");
				String slot = in.getSlotsMap().get(n);
				if ("oslot".equals(slot)) continue;
				
				String[] Res = slot.split("[\\p{Punct}\\s]+");
				
				
				for (int i=0; i<Res.length; i++) {
					String r = Res[i];
					offerIntent(r);
					offerConcept(r, word);
					offerProperty(r, word, word);
					offerExample(r, word, word);
				}
			}
		}
		
	}


	private void offerExample(String intent, String concept, String ex) throws JSONException {
		if (!jsonIntents.get(intent).has("examples"))
			jsonIntents.get(intent).put("examples", new JSONArray());
		JSONArray examples = (JSONArray) jsonIntents.get(intent).get("examples");
		JSONObject example = new JSONObject();
		example.put("concept", concept);
		example.put("example", ex);
		examples.put(examples.length(), example);
	}


	private void offerIntent(String intent) throws JSONException {
		if (jsonIntents.containsKey(intent)) 
			return;
		jsonIntents.put(intent, new JSONObject());
		jsonIntents.get(intent).put("intent", intent);
		jsonIntents.get(intent).put("concepts", new JSONArray());
		
	}

	private void offerConcept(String intent, String concept) throws JSONException {
		JSONArray concepts = (JSONArray) jsonIntents.get(intent).get("concepts");
		for (int i=0; i<concepts.length(); i++) {
			JSONObject c = (JSONObject) concepts.get(i);
			if (c.getString("concept").equals(concept)) return;
		}
		JSONObject jsonConcept = new JSONObject();		
		jsonConcept.put("concept", concept);
		jsonConcept.put("properties", new JSONArray());
		concepts.put(concepts.length(), jsonConcept);
	}
	
	private void offerProperty(String intent, String concept, String property) throws JSONException {
		JSONArray concepts = (JSONArray) jsonIntents.get(intent).get("concepts");
		for (int i=0; i<concepts.length(); i++) {
			JSONObject jsonConcept = (JSONObject) concepts.get(i);
			if (jsonConcept.getString("concept").equals(concept)) {
				JSONArray props = (JSONArray) jsonConcept.get("properties");
				for (int j=0; j<props.length(); j++) {
					JSONObject p = (JSONObject) props.get(j);
					if (property.contains(p.getString("property"))) return;
				}
				for (String s : Arrays.asList(property.split("\\s+"))) {
					JSONObject p = new JSONObject();
					p.put("property", s);
					props.put(props.length(), p);
				}
			}
		}
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
			line = scan.nextLine().toLowerCase();
			process(line);
		}
		scan.close();	

	}
	
	public static void main(String[] args) {
		try {
			new ConvertATIStoJSON().run();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (JSONException e) {
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
