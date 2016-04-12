package util;

import org.junit.Test;

public class LevenshteinDistanceTest {
	
	@Test 
	public void test() {
		System.out.println(LevenshteinDistance.getProbability("friend", "french"));
		System.out.println(LevenshteinDistance.getProbability("friend", "friend"));
		System.out.println(LevenshteinDistance.getProbability("cow", "french"));
		System.out.println(LevenshteinDistance.getProbability("feed", "french"));
		System.out.println(LevenshteinDistance.getProbability("bench", "french"));
		
	}

}
