package com.ppinera.popcorntv.utils;

import java.io.File;
import java.io.FilenameFilter;

public class ExtGenericFilter implements FilenameFilter {

	private String ext;

	public ExtGenericFilter(String ext) {
		this.ext = ext;
	}

	public boolean accept(File dir, String name) {
		return (name.endsWith(ext));
	}
}