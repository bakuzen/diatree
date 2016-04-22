package util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import model.Constants;
import model.DomainModel;
import model.db.Domain;

public class InsertJsonData {
	
	private Domain db;
	private String domain = "sigdial";
	private String filePath = "domains/"+domain+"/json/";
	
	
	public static void main(String[] args) {
		try {
			new InsertJsonData().run();
		} 
		catch (SQLException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public InsertJsonData() {
		
	}
	
	public InsertJsonData(String d, String p) {
		this.filePath = p;
		this.domain = d;
	}

	public void run() throws SQLException, IOException {
		
//		String testFile = Constants.BASE_FILE_PATH + domain + "/test";
//		writer = new FileWriter(testFile);
		
		destroy(); //remove files if they already exist
		
		db = new Domain();
		
		db.createNewDomain(domain);
		db.setDomain(domain);
		
		try { 
			walk(filePath);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
//			destroy();
		}

		DomainModel model = new DomainModel(db, domain);
		model.train();
		
//		writer.close();
		
	}

	private void destroy() {
		new File(Constants.BASE_FILE_PATH + domain + "/" + domain + ".db").delete();
//		new File(Constants.BASE_FILE_PATH + domain).delete();		
	}

	private void walk(String filePath2) throws Exception {
		for (File file : new File(filePath2).listFiles()) {
			if (file.isDirectory()) walk(file.getAbsolutePath());
			else if (file.getName().endsWith(".json")) 
				handleJSONFile(file.getAbsolutePath());
		}
	}

	private void handleJSONFile(String absolutePath) throws Exception {
			System.out.println("Inserting " + absolutePath);
			
			JSONTokener tok = new JSONTokener(new FileReader(absolutePath));
			JSONObject json = new JSONObject(tok);
			
			if (absolutePath.endsWith("intent.json")) {
				handleSequences(json);
				return;
			}
			
			String intent = json.getString("intent");
			JSONArray concepts = json.getJSONArray("concepts");
			if (concepts.length() < 2) {
				System.out.println("Warning! intent " + intent + " only has one concept.");
			}
			db.offerNewIntent(intent);
			
			for (int i=0; i<concepts.length(); i++) {
				JSONObject concept = new JSONObject(new JSONTokener(concepts.get(i).toString()));
				JSONArray properties = concept.getJSONArray("properties");
				String conceptName = concept.getString("concept");
				db.offerNewConcept(conceptName);
				db.offerNewConceptIntentAttachment(conceptName, intent);
				for (int j=0; j<properties.length(); j++) {
					JSONObject property = new JSONObject(properties.get(j).toString());
					String propertyName = property.getString("property");
					db.offerNewProperty(propertyName);
					db.offerNewPropertyConceptAttachment(propertyName, conceptName);
				}
			}
			if (json.has("examples")) {
				JSONArray examples = json.getJSONArray("examples");
				for (int i=0; i<examples.length(); i++) {
					JSONObject example = new JSONObject(new JSONTokener(examples.get(i).toString()));
					String conceptName = example.getString("concept");
					String exampleString = example.getString("example");
					db.addExampleForConcept(conceptName, exampleString);
//					break;
				}
			}
	}

	private void handleSequences(JSONObject json) throws JSONException, SQLException {
		
		JSONArray rootChildren = json.getJSONArray("intents");
		
		db.offerNewIntent("intent");
//		db.offerNewConcept("intent");
//		db.offerNewConceptIntentAttachment("intent", "intent");
		
		for (int i=0; i<rootChildren.length(); i++) {
			JSONObject intent = new JSONObject(new JSONTokener(rootChildren.get(i).toString()));
			JSONArray children = intent.getJSONArray("children");
			String name = intent.getString("name");
			db.offerNewConcept(name);
			db.offerNewIntent(name);
			db.offerNewConceptIntentAttachment(name, "intent");
			for (int j=0; j<children.length(); j++) {
				
				db.offerNewConcept(name);
				db.offerNewIntent(name);
				db.offerNewConceptIntentAttachment(name, "intent");
				JSONObject childJSON = new JSONObject(children.get(j).toString());
				
//				TODO: need to handle the filled case
				if (childJSON.has("filled")) {
					String filled= childJSON.getString("filled");
//					db.offerNewIntent(filled);
					continue;
				}
				
				String childName = childJSON.getString("child");
				
				boolean isCustomFunction = false;
				if (childName.startsWith("[") && childName.endsWith("]")) {
					childName = childName.replace("[", "").replace("]", "");
					isCustomFunction = true;
				}
				
				db.offerNewConcept(childName);
				db.offerNewIntent(childName);
				db.offerNewConceptIntentAttachment(childName, "intent");
				
				db.offerNewIntent(childName);
				db.offerNewIntentSequence(name, childName);
				
				if (isCustomFunction)
					db.offerNewCustomFunction(childName);				
			}
		}
		
	}
	

}
