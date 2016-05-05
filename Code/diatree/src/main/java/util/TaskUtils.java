package util;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;

import model.Constants;
import model.DomainModel;
import model.db.Domain;
import module.INLUModule;
import module.TaskModule;

public class TaskUtils {
	
	LinkedList<Task> taskStack;
	TaskModule module;
	
	String[] messages = {
			"essen in der Mensa",
			"joggen gehen",
			"Lehrer ist krank"
	};
	
	public TaskUtils() {
		taskStack = new LinkedList<Task>();
	}
	
	
	public TaskUtils(TaskModule taskModule) {
		this();
		this.module = taskModule;
	}


	private class Task implements Comparable<Task> {
		
		
		public String domain;
		public HashMap<String,LinkedList<String>> intentsConcepts;
		public int used;
		
		public Task() {
			used = 1;
			intentsConcepts = new HashMap<String,LinkedList<String>>();
		}
		
		public String toString() {
			return domain + ":" + intentsConcepts.toString();
		}
		
		@Override
		public int compareTo(Task o) {
			return this.toString().compareTo(o.toString()); 
		}
	
		
	}
	
	public Task generateNewTask() {

		
		try {
			
			Task t = new Task();
		    Domain db = new Domain();
		    db.setDomain("sigdial");
			Random rand = new Random();
			List<String> rootIntents = db.getChildIntentsForIntent(Constants.ROOT_NAME);
			t.domain = rootIntents.get(rand.nextInt(rootIntents.size()));
			List<String> intents = db.getChildIntentsForIntent(t.domain);
			for (String intent : intents) {
				t.intentsConcepts.put(intent, new LinkedList<String>());
				if (intent.equals(Constants.MESSAGE)) {
					t.intentsConcepts.get(intent).add(messages[rand.nextInt(messages.length)]);
					continue;
				}
				List<String> concepts = db.getConceptsForIntent(intent);
//				if (concepts.isEmpty()) continue;
				int oneTwoOrThree = rand.nextInt(2) + 1;
				if (oneTwoOrThree > concepts.size()) oneTwoOrThree = 1;
				for (int i=0; i<oneTwoOrThree; i++) {
					
					String concept = concepts.get(rand.nextInt(concepts.size()));
					while (t.intentsConcepts.get(intent).contains(concept))
							concept = concepts.get(rand.nextInt(concepts.size()));
					t.intentsConcepts.get(intent).add(concept);
				}
			}
			return t;			
		} catch (SQLException e) {
		
			e.printStackTrace();
		}
		return null;
		
	}
	
	public static void main (String[] args ) {
		TaskUtils task = new TaskUtils();
		task.nextTask();
	}
	
	
	public void nextTask() {
		
		Task task = null;
		
		if (taskStack.isEmpty()) {
			task = generateNewTask();
			taskStack.push(task);
		}
		else {
			Random rand = new Random();
			if (rand.nextInt(100) > 75) {
//				25% of the time, grab an already-seen task
				task = taskStack.get(rand.nextInt(taskStack.size()));
				task.used++;
			}
			else {
//				otherwise, generate a new one
				task = generateNewTask();
				taskStack.push(task);
			}
		}
		
		display(task);
		
	}


	private void display(Task task) {
		 EventQueue.invokeLater(new Runnable() {
		        
	            @Override
	            public void run() {
	        		JFrame frame = new JFrame();
	        		frame.setSize(1000, 1000);
	        		
	        		frame.addKeyListener(new KeyListener() {

						@Override
						public void keyTyped(KeyEvent e) {
							if (e.getID() == 400) {
		                    	frame.setVisible(false);
		                    	frame.dispose();
		                    	module.taskComplete();
							}
						}

						@Override
						public void keyPressed(KeyEvent e) {
						}

						@Override
						public void keyReleased(KeyEvent e) {
						}
	        				
	        				
	        		});
	        		
	        		String conceptString = getConceptString(task);
	        		
	        		String utt = "<html>";
	        		utt += "&nbsp;<img height=\"60\" width=\"60\" src=\"file:domains/sigdial/resources/" + task.domain + ".png\">&nbsp; "; 
	        		utt += conceptString + "&nbsp;</html>"; 
	        		
	        		JLabel text = new JLabel(utt);
	        		
	        		text.setFont(new Font(text.getFont().getName(), Font.PLAIN, 36));
	        		frame.getContentPane().add(text);
	        		frame.pack();
	        		frame.setLocationRelativeTo(null);
	        		frame.setVisible(true);
	            }

				private String getConceptString(Task task) {
					String c = "";
					
					for (String intent : task.intentsConcepts.keySet()) {
						List<String> concepts = task.intentsConcepts.get(intent);
						for (int i=0; i<concepts.size(); i++) {
							String concept = concepts.get(i);
							
							concept = concept.replace("_", " ");
							if (intent.equals(Constants.MESSAGE)) concept = "'" + concept + "'";
							
							if (i < concepts.size()-1) concept = concept + " oder ";
							else concept += ",  ";
							c += concept;
						}
					}
					c = c.trim();
					return c.substring(0, c.length()-1);
				}
	        });
		
	}
	
	

}
