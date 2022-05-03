package com.dma.web;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;

public class Test10 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Path path = Paths.get("/home/dma/dma/prj007/tableLabel.csv");
		
		String fileName = path.getFileName().toString();
		fileName.startsWith("tableLabel");
		
	}

}
