package me.mrCookieSlime.Slimefun.Android.ScriptComparators;

import java.util.Comparator;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Android.ProgrammableAndroid;

public class ScriptDownloadSorter implements Comparator<Config> {

	private ProgrammableAndroid android;
	
	public ScriptDownloadSorter(ProgrammableAndroid programmableAndroid) {
		this.android = programmableAndroid;
	}

	@Override
	public int compare(Config c1, Config c2) {
		return c2.getInt("downloads") - c1.getInt("downloads");
	}

}
