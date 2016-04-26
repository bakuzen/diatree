


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import org.junit.Test;

import model.db.Domain;
import sium.nlu.stat.Distribution;
import util.InsertJsonData;
import model.Constants;
import model.DomainModel;
import model.Frame;

public class FoodDomainTest {
	
	DomainModel model;
	
	@Test public void test() {
		Domain db = new Domain();
		String d = "t_food";
		try {
			create(db, d);
			train(db, d);
//			eval();
		} 
		catch (SQLException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			destroy(d);
		}
	}

	private void eval() throws SQLException {
		
			
//		System.out.println("gold: restaurant=japanese, area=here, time=evening");
		model.newUtterance();
		model.addIncrement("i");
		assertTrue(model.getPredictedFrame().getArgMaxFrame().size() == 0);
		model.addIncrement("need");
		assertTrue(model.getPredictedFrame().getArgMaxFrame().size() == 0);
		model.addIncrement("indian");
		assertTrue(model.getPredictedFrame().getArgMaxFrame().size() == 1);
		model.addIncrement("food");
		assertTrue(model.getPredictedFrame().getArgMaxFrame().size() == 1);
		model.addIncrement("around");
		assertTrue(model.getPredictedFrame().getArgMaxFrame().size() == 1);
		model.addIncrement("the");
		assertTrue(model.getPredictedFrame().getArgMaxFrame().size() == 1);
		model.addIncrement("uni");
		assertTrue(model.getPredictedFrame().getArgMaxFrame().size() == 2);
		assertTrue(model.getPredictedFrame().getArgMaxFrame().getValueForIntent("food").equals("indian"));
		assertTrue(model.getPredictedFrame().getArgMaxFrame().getValueForIntent("location").equals("university"));
		
	}

	private void train(Domain db, String d) throws SQLException {
		model = new DomainModel(db, d);
		model.train();
	}

	private void destroy(String d) {
		if (model != null)
			model.close();
		new File(Constants.BASE_FILE_PATH + d + "/" + d + ".db").delete();
		new File(Constants.BASE_FILE_PATH + d).delete();
	}

	private void create(Domain db, String d) throws SQLException, IOException {

		
		new InsertJsonData(d, "src/test/java/").run();
		
		// does the database now exist?
		assertTrue(new File(Constants.BASE_FILE_PATH + d + "/" + d + ".db").exists());
		
		db.setDomain(d);
		
		// have the three concepts been added to the right intent, and do the corresponding queries work?
		assertTrue(db.getNumberOfConceptsForIntent(db.getIntentID("food")) == 4);
	
		assertTrue(db.offerWord("japanese") == db.offerWord("japanese"));
		assertTrue(db.offerWord("japanese") > db.offerWord("thai"));	
		
	}
	

}
