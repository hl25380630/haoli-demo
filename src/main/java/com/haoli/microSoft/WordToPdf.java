package com.haoli.microSoft;

import java.io.OutputStream;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.org.apache.poi.util.IOUtils;


public class WordToPdf {
	
	public static void main(String[] args) throws Exception {
		Docx4J.pdfViaFO();
		String source = "C:\\Users\\10063731\\Desktop\\cip\\cip审批流程技术培训文档.docx";
		String dest = "C:\\Users\\10063731\\Desktop\\cip\\666.pdf";
		WordToPdf wp = new WordToPdf();
		wp.convertDocxToPDF(source, dest);
	}
	
    
	public void convertDocxToPDF(String source, String dest) throws Exception {  
    	OutputStream os = new java.io.FileOutputStream(dest);  
    	WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(source));
    	Mapper fontMapper = new IdentityPlusMapper();
    	wordMLPackage.setFontMapper(fontMapper);
    	fontMapper.put("Calibri",PhysicalFonts.get("SimSun"));
        FOSettings foSettings = Docx4J.createFOSettings();
        foSettings.setWmlPackage(wordMLPackage);
        Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
        IOUtils.closeQuietly(os);  
    	
    }  
    
    
    
    

}
