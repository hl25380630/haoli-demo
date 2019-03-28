package com.haoli.demo.service.util.ms;

import java.io.File;

public class PptToPdfUtil {
	
    public static void main(String[] args) throws Exception {
        String source = "C:\\Users\\10063731\\Desktop\\cip\\11M-fake.pptx";
        String pptImgsPath = "C:\\Users\\10063731\\Desktop\\cip\\pptImgs";
        String dest = "C:\\Users\\10063731\\Desktop\\cip\\result.pdf";
        PptToPdfUtil pe = new PptToPdfUtil();
        pe.pptxToPdf(source ,pptImgsPath, dest);
    }
	
	public void pptToPdf(String source, String pptImgsPath, String dest) throws Exception {
		PptToImageUtil piu = new PptToImageUtil();
		String imageFolderPath = piu.pptToImage(source, pptImgsPath);
		ImgsToPdfUtil ipu = new ImgsToPdfUtil();
		ipu.imgsToPdf(imageFolderPath, dest);
		File imgFolderPath = new File(pptImgsPath);
		this.deleteAll(imgFolderPath);

	}
	
	public void pptxToPdf(String source, String pptImgsPath, String dest) throws Exception {
		PptToImageUtil piu = new PptToImageUtil();
		String imageFolderPath = piu.pptxToImage(source, pptImgsPath);
		ImgsToPdfUtil ipu = new ImgsToPdfUtil();
		ipu.imgsToPdf(imageFolderPath, dest);
		File imgFolderPath = new File(pptImgsPath);
		this.deleteAll(imgFolderPath);

	}
	
	
	public void deleteAll(File dir) {
		if (dir.isFile()) {
			dir.delete();
			return;
		} else {
			File[] files = dir.listFiles();
			for (File file : files) {
				deleteAll(file);
			}
		}
		dir.delete();
	}

}
