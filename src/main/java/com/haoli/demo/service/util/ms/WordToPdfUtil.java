package com.haoli.demo.service.util.ms;

import java.io.OutputStream;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.org.apache.poi.util.IOUtils;

public class WordToPdfUtil {
	
	public static void main(String[] args) throws Exception {
//		Docx4J.pdfViaFO();
		String source = "C:\\Users\\10063731\\Desktop\\cip\\cip审批流程技术培训文档.docx";
		String dest = "C:\\Users\\10063731\\Desktop\\cip\\666.pdf";
		WordToPdfUtil wp = new WordToPdfUtil();
		wp.convertDocxToPDF(source, dest);
	}
	
	
	/**
	 * word文档转换为pdf文档
	 * @param source 目标文件地址
	 * @param dest 转换后文件存储地址
	 */
	public void convertDocxToPDF(String source, String dest) throws Exception {  
    	OutputStream os = new java.io.FileOutputStream(dest);  
    	WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(source));
    	Mapper fontMapper = new IdentityPlusMapper();
    	wordMLPackage.setFontMapper(fontMapper);
    	fontMapper.put("Calibri",PhysicalFonts.get("STSong"));
    	fontMapper.put("Times-Roman",PhysicalFonts.get("STSong"));
    	fontMapper.put("Times-Bold",PhysicalFonts.get("STSong"));
    	fontMapper.put("华文宋体",PhysicalFonts.get("STSong"));
        FOSettings foSettings = Docx4J.createFOSettings();
        foSettings.setWmlPackage(wordMLPackage);
        Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
        IOUtils.closeQuietly(os);  
    	
    }  

}
