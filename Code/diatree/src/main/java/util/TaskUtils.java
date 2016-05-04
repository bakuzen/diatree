package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TaskUtils {
	
	public static void initialize() {
		Scanner scan;
		try {
			scan = new Scanner(new File("tasks.txt"));
			
			
			scan.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
	

}
