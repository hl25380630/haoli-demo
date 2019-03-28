package com.haoli.demo.controller;

import java.io.InputStream;

import com.aspose.words.License;

public class TestController {
	
	
	  public static void main(String[] args) throws Exception
	    {
	        TestController tc = new TestController();
	        boolean flag = tc.getLicense();
		  	String source = "C:\\Users\\10063731\\Desktop\\cip\\test file\\CMP用户手册v2.0.docx";
		  	String dest="C:\\Users\\10063731\\Desktop\\cip\\test file\\caocao.pdf";
		  	com.aspose.words.Document doc = new com.aspose.words.Document(source);
	        doc.save(dest);

	    }
	  
	  public boolean getLicense() throws Exception {
		  boolean result = false;
		  InputStream is = TestController.class.getClassLoader().getResourceAsStream("license.xml");
		  License aposeLic = new License();
		  aposeLic.setLicense(is);
		  result = true;
		  return result;
	  }
}
