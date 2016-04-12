package util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import model.Constants;
import model.DomainModel;
import model.db.Domain;

public class InsertJsonData {
	
	private Domain db;
	private String filePath = "domains/sigdial/json/";
	private String domain = "sigdial";
//	private FileWriter writer; 
	
	public static void main(String[] args) {
		try {
			new InsertJsonData().run();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
			JSONTokener tok = new JSONTokener(new FileReader(absolutePath));
			JSONObject json = new JSONObject(tok);
			
			String intent = json.getString("intent");
			db.offerNewIntent(intent);
			JSONArray concepts = json.getJSONArray("concepts");
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
				}
			}
	}
	

}
