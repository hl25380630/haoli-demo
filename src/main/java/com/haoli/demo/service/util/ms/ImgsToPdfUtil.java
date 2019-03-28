package com.haoli.demo.service.util.ms;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

public class ImgsToPdfUtil {
	
    public static void main(String[] args) throws Exception {
        String imageFolderPath = "C:\\Users\\10063731\\Desktop\\cip\\pptImg";
        String dest = "C:\\Users\\10063731\\Desktop\\cip\\pptimg.pdf";
        ImgsToPdfUtil pe = new ImgsToPdfUtil();
        pe.imgsToPdf(imageFolderPath ,dest);
    }

	public void imgsToPdf(String imageFolderPath, String pdfPath) throws Exception{
		FileOutputStream fos = new FileOutputStream(pdfPath);
		Document doc = new Document(null, 0, 0, 0, 0);
		doc.open();
		// 写入PDF文档
		PdfWriter.getInstance(doc, fos);
		// 获取图片文件夹对象
		File[] files = new File(imageFolderPath).listFiles();
		// 循环获取图片文件夹内的图片
		for (File file : files) {
			String fileName = file.getName();
			String fileType = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());
            String reg = "^(png|jpg|jpeg|gif|tif)$";
            if (!fileType.toLowerCase().matches(reg)){
                continue;
            }
			String imagePath = imageFolderPath + "/" + fileName;
			BufferedImage img = ImageIO.read(new File(imagePath));
			// 根据图片大小设置文档大小
			doc.setPageSize(new Rectangle(img.getWidth(), img.getHeight()));
			// 实例化图片
			Image image = Image.getInstance(imagePath);
			// 添加图片到文档
			doc.open();
			doc.add(image);
		}
		doc.close();
	}
}
