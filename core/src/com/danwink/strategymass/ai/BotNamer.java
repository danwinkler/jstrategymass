package com.danwink.strategymass.ai;

import java.util.Random;

public class BotNamer
{
	public static String[] titles = new String[] {
		"Sir",
		"Lady",
		"King",
		"Duke",
		"Duchess",
		"King",
		"Queen",
		"Earl"
	};
	
	public static String[] suffixes = new String[] {
		"the Brave",
		"the Weak",
		"the Strong",
		"Softsword",
		"the Fat",
		"the Ugly",
		"the Fearsome"
	};
	
	public static String[] nameA = new String[] {
		"Glo",
		"Jo",
		"Rhe",
		"Frei",
		"Ham"
	};
	
	public static String[] nameB = new String[] {
		"don",
		"vere",
		"pon",
		"wen",
		"man",
		"mond",
		"son",
		"n"
	};
	
	public static String getRandom( String[] list, Random r )
	{
		return list[r.nextInt( list.length )];
	}
	
	public static String getName()
	{
		Random r = new Random();
		String title = getRandom( titles, r );
		String a = getRandom( nameA, r );
		String b = getRandom( nameB, r );
		String suf = getRandom( suffixes, r );
		
		return title + " " + a + b + " " + suf;
	}
}
