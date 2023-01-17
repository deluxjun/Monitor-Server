package com.speno.xmon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegTest {
	public static void main(String[] args) {
		String text = "123123 now 1231412412";
		
		Pattern p = Pattern.compile("now");
		Matcher m = p.matcher(text);
		
		System.out.println(m.find());
		
		System.out.println(Pattern.matches("now", text));
		System.out.println(Pattern.matches("a*b", "aaaaab"));
	}
}
