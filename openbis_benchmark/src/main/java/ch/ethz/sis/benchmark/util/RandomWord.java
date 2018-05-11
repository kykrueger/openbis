package ch.ethz.sis.benchmark.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomWord {
	
	private static final List<String> words = new ArrayList<String>();
	
	static {
		InputStream in = RandomWord.class.getResourceAsStream("google-10000-english.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		try {
			while((line = reader.readLine()) != null) {
				words.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private RandomWord() {}
	
	public static String getRandomWord() {
		return words.get(ThreadLocalRandom.current().nextInt(0, words.size()));
	}
}
