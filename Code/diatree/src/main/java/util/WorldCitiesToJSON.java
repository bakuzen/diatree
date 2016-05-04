package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WorldCitiesToJSON {
	
	private static String filename = "/home/casey/Desktop/germanytop100.txt";
	private static String country = "de";
	private static int numCities = 0;
	
	
	public static void main(String[] args) {
		

		
		
		
		try {
			
			JSONObject intent = new JSONObject();
			intent.put("intent", "nach");
			JSONArray cities = new JSONArray();
			intent.put("concepts", cities);
			
			Scanner scan = new Scanner(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			scan.next();
			while (scan.hasNext()) {
				String line = scan.nextLine().toLowerCase();
//				if (line.startsWith(country+",")) 
					process(line, cities);
			}
			System.out.println("number of cities for " + country + ":" + numCities);
			scan.close();
			
			FileWriter f = new FileWriter("domains/sigdial/json/destination.json");
			f.write(intent.toString(2));
			f.close();
			
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	private static void process(String line, JSONArray cities) throws JSONException {
		String[] split = line.split("\t");
		if (split.length < 3) return;
		String name = split[1];
		name = name.replace("'", " ").trim();
		JSONObject concept = new JSONObject();
		concept.put("concept", name);
		JSONArray properties = new JSONArray();
		concept.put("properties", properties);
		
		
		String[] nameSplit = name.split("[\\s-]+");
		
		for (int i=0; i<nameSplit.length; i++) {
			JSONObject prop = new JSONObject();
			prop.put("property", nameSplit[i]);
			properties.put(properties.length(), prop);
		}
		
		cities.put(cities.length(), concept);
		
		
		numCities++;
	}

}
