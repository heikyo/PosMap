package com.app.posmap;

import android.content.SearchRecentSuggestionsProvider;

public class PosMapProvider extends SearchRecentSuggestionsProvider {
	
	private static final String AUTHORITY = "com.app.posmap.provider";
	private static final int MODE = DATABASE_MODE_QUERIES;
	
	public PosMapProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}
}
