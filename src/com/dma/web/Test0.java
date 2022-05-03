package com.dma.web;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class Test0 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		long recCount = 20;
		long qs_recCount = 20;
		
		double d0 = Double.parseDouble(String.valueOf(recCount));
		double d1 = Double.parseDouble(String.valueOf(qs_recCount));
		
		double num = (d0/d1) * 100;
		NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(3);
//		nf.setMinimumFractionDigits(5);	    
		nf.setRoundingMode(RoundingMode.UP);
	    num = Double.parseDouble(nf.format(num));
		
		
		
	}

}
