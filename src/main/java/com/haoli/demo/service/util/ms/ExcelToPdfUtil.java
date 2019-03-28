package com.haoli.demo.service.util.ms;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.FontUnderline;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontStyle;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class ExcelToPdfUtil {
    
    public static void main(String[] args) throws Exception {
        String source = "C:\\Users\\10063731\\Desktop\\cip\\test file\\CIPPRE环境消息提醒测试.xlsx";
        String dest = "C:\\Users\\10063731\\Desktop\\cip\\777.pdf";
        String fontPath = "C:\\Windows\\Fonts\\STSONG.TTF";
        ExcelToPdfUtil pe = new ExcelToPdfUtil(fontPath);
        pe.convert(source, dest);
    }
    
    String anchorName = "anchorName";

    BaseFont baseFont;
    
    String fontPath;
    
    protected boolean setting = false;
    
    public ExcelToPdfUtil() {
    	
    }
    
    public ExcelToPdfUtil(String fontPath) {
    	this.fontPath = fontPath;
    }
    
    public ExcelToPdfUtil(BaseFont baseFont) {
    	this.baseFont = baseFont;
    }
    
	public void convert(String source, String dest) throws Exception {
        FileInputStream in = new FileInputStream(new File(source));
        FileOutputStream out = new FileOutputStream(new File(dest));
		//使用itext新建pdf文件，设置为A4纸大小
        Document document = new Document();
        document.setPageSize(PageSize.A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document, out);
        writer.setPageEvent(new PDFPageEvent(fontPath));
        document.open();
        //获取想要转换成pdf的excel文件
        Workbook wb = WorkbookFactory.create(in);
        int totalSheets = wb.getNumberOfSheets();
        List<PdfPTable> pdfTableList = new ArrayList<PdfPTable>();
        for(int i = 0; i<totalSheets; i++) {
        	//获取当前sheet页
        	Sheet sheet = wb.getSheetAt(i);
        	//讲excel的sheet页内容转换为pdf文件可用的形式，即PdfTable形式
        	PdfPTable pdfTable = this.parseContent(sheet, wb);
        	pdfTable.setKeepTogether(true);
        	pdfTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
        	pdfTableList.add(pdfTable);
        }
        //将多个sheet的内容合并到一个pdf文件中
        for(int i=0; i<pdfTableList.size(); i++) {
        	PdfPTable pdfTable = pdfTableList.get(i);
        	document.add(pdfTable);
        }
        document.close();
    }
	
	/**
	 *讲excel的sheet页内容转换为pdf 
	 */
	public PdfPTable parseContent(Sheet sheet, Workbook wb) throws Exception{
		//存储每一行转为pdf时所需的所有pdfCell的信息
		List<List<PdfPCell>> cellsList = new ArrayList<List<PdfPCell>>();
		//用户存储所有行的宽度，以确定哪一行最长
		List<Integer> widthList = new ArrayList<Integer>();
		//存储每一行的所有列的宽度数组信息
		List<float[]> columnWidthList = new ArrayList<float[]>();
		//获取该excel sheet的总行数
		int rowlength = sheet.getLastRowNum();
        //遍历excel sheet页的每一行
        for (int i = 0; i <= rowlength; i++) {
        	List<PdfPCell> cells = new ArrayList<PdfPCell>();
            Row row = sheet.getRow(i);
            if(row == null) {
            	continue;
            }
            int rowIndex = row.getRowNum();
            //获取该行一共有多少列
            int columNo = row.getLastCellNum();
            //创建每一列的宽度信息存储数组
            float[] columnWidthArray = new float[columNo];
            //遍历每一列
            for (int j = 0; j < columNo; j++) {
            	//获取单元格
            	Cell cell = row.getCell(j);
                if(cell == null) {
                	continue;
                }
                int columnIndex = cell.getColumnIndex();
                //获取每一个单元格的宽度
                float columnWidth = getPOIColumnWidth(cell, sheet);
                columnWidthArray[columnIndex] = columnWidth;
                //判断该单元格是否是在融合单元格里（而且不是融合单元格的第一个单元格）,如果是，则跳过该单元格（因为该单元格被合并了，还不是第一个，所以没用）
                if(isContainedInMergedCells(columnIndex, rowIndex, sheet)){
                    continue;
                }
                cell.setCellType(Cell.CELL_TYPE_STRING);
                //判断该单元格是否属于融合单元格的第一个单元格，如果是，则返回整个融合区域的坐标信息，如果不是，则返回null
                CellRangeAddress range = getCellMergedErea(row.getRowNum(), cell.getColumnIndex(), sheet);
                //如果当前单元格属于融合单元格，则计算整个融合区域的长度和高度，如果不是融合单元格，则为单个单元格，长度和宽度均为1
                int mergedEreaRowSpan = 1;
                int mergedEreaColumnSpan = 1;
                if (range != null) {
                	mergedEreaRowSpan = range.getLastRow() - range.getFirstRow() + 1;
                	mergedEreaColumnSpan = range.getLastColumn() - range.getFirstColumn() + 1;
                }
                //PDF单元格
                PdfPCell pdfpCell = new PdfPCell();
                pdfpCell.setBackgroundColor(new BaseColor(getBackgroundColorByExcel(cell.getCellStyle())));
                pdfpCell.setColspan(mergedEreaColumnSpan);
                pdfpCell.setRowspan(mergedEreaRowSpan);
                pdfpCell.setVerticalAlignment(getVAlignByExcel(cell.getCellStyle().getVerticalAlignment()));
                pdfpCell.setHorizontalAlignment(getHAlignByExcel(cell.getCellStyle().getAlignment()));
                pdfpCell.setPhrase(getPhrase(cell, wb));
                pdfpCell.setFixedHeight(this.getPixelHeight(row.getHeightInPoints()));
                this.addBorderByExcel(pdfpCell, cell.getCellStyle(), wb);
                this.addImageByPOICell(pdfpCell , cell , columnWidth);
                cells.add(pdfpCell);
                //直接跳到下一个单元格,继续进行转换操作
                j += mergedEreaColumnSpan - 1;
            }
            columnWidthList.add(columnWidthArray);
            cellsList.add(cells);
            int totalWidth = 0;
            for(int p=0; p<columnWidthArray.length; p++) {
            	totalWidth += columnWidthArray[p];
            }
            widthList.add(totalWidth);

        }
        //获取所有行里拥有最大长度的那一行
        int maxWidth = widthList.get(0);
        int maxIndex = 0;
        for(int q = 1; q<widthList.size(); q++) {
        	if(widthList.get(q) > maxWidth) {
        		maxWidth = widthList.get(q);
        		maxIndex = q;
        	}
        }
        //生成pdfTable用来转换excel文件内容
		PdfPTable table = new PdfPTable(columnWidthList.get(maxIndex));
		table.setWidthPercentage(100);
		for(int rowIndex=0; rowIndex < cellsList.size(); rowIndex++) {
			int width = widthList.get(rowIndex);
			int rowSpan = 0;
			PdfPCell cell = null;
			List<PdfPCell> cells = cellsList.get(rowIndex);
	        for (PdfPCell pdfpCell : cells) {
	        	rowSpan = pdfpCell.getRowspan();
	        	cell = pdfpCell;
	            table.addCell(pdfpCell);
	        }
	        //如果当前行小于最大长度，则新建一个空cell补充，防止错位
	        if(width < maxWidth) {
	        	PdfPCell pcell = new PdfPCell();
	        	pcell.setColspan(maxWidth - width);
	        	pcell.setRowspan(rowSpan);
	        	table.addCell(pcell);
	        }
		}
        return table;
    }
    
    
    protected Phrase getPhrase(Cell cell, Workbook wb) throws Exception{
        Anchor anchor = new Anchor(cell.getStringCellValue() , getFontByExcel(cell.getCellStyle(), wb));
        anchor.setName(anchorName);
        this.setting = true;
        return anchor;
    }
    
    protected void addImageByPOICell(PdfPCell pdfpCell , Cell cell , float cellWidth) throws BadElementException, MalformedURLException, IOException{
       Excelmage poiImage = new Excelmage().getCellImage(cell);
       if(poiImage == null) {
    	   return;
       }
       byte[] bytes = poiImage.getBytes();
       if(bytes != null){
           pdfpCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
           pdfpCell.setHorizontalAlignment(Element.ALIGN_CENTER);
           Image image = Image.getInstance(bytes);
           pdfpCell.setImage(image);
       }
    }
    
    protected float getPixelHeight(float poiHeight){
        float pixel = poiHeight / 28.6f * 26f;
        return pixel;
    }
    
    /**
     * Description: 获取Excel的列宽像素(无法精确实现,期待有能力的朋友进行改善此处)
     * @return 像素宽
     */
    protected int getPOIColumnWidth(Cell cell , Sheet sheet) {
        int colWidthpoi = sheet.getColumnWidth(cell.getColumnIndex());
        int widthPixel = 0;
        if (colWidthpoi >= 416) {
            widthPixel = (int) (((colWidthpoi - 416.0) / 256.0) * 8.0 + 13.0 + 0.5);
        } else {
            widthPixel = (int) (colWidthpoi / 416.0 * 13.0 + 0.5);
        }
        return widthPixel;
    }
    
    /**
     * 判断该单元格是否属于融合单元格的第一个单元格，如果是，则返回整个融合区域的坐标信息，如果不是，则返回null
     */
    protected CellRangeAddress getCellMergedErea(int rowIndex, int colIndex, Sheet sheet) {
        CellRangeAddress result = null;
        int num = sheet.getNumMergedRegions();
        for (int i = 0; i < num; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            if (range.getFirstColumn() == colIndex && range.getFirstRow() == rowIndex) {
                result = range;
            }
        }
        return result;
    }
    
    /**
     * 判断excel的一个单元格是否属于该excel所有融合的单元格之一(且不是融合单元格的第一个单元格)
     */
    protected boolean isContainedInMergedCells(int colIndex , int rowIndex, Sheet sheet){
        boolean result = false;
        //获取合并单元格的总数量
        int num = sheet.getNumMergedRegions();
        for (int i = 0; i < num; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            if (firstRow < rowIndex && lastRow >= rowIndex) {
                if(firstColumn <= colIndex && lastColumn >= colIndex){
                    result = true;
                }
            }
        }
        return result;
    }

    protected Font getFontByExcel(CellStyle style, Workbook wb) throws Exception {
       BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
       Font result = new Font(baseFont , 8 , Font.NORMAL);
       //字体样式索引
       short index = style.getFontIndex();
       org.apache.poi.ss.usermodel.Font font = wb.getFontAt(index);
       //字体颜色
       int colorIndex = font.getColor();
       if(font.getBoldweight() == org.apache.poi.ss.usermodel.Font.BOLDWEIGHT_BOLD){
           result.setStyle(Font.BOLD);
       }
       HSSFColor color = HSSFColor.getIndexHash().get(colorIndex);
       if(color != null){
           int rbg = this.getRGB(color);
           result.setColor(new BaseColor(rbg));
       }
       //下划线
       FontUnderline underline = FontUnderline.valueOf(font.getUnderline());
       if(underline == FontUnderline.SINGLE){
           String ulString = FontStyle.UNDERLINE.getValue();
           result.setStyle(ulString);
       }
       return result;
    }
    
    protected int getBackgroundColorByExcel(CellStyle style) {
        Color color = style.getFillForegroundColorColor();
        return this.getRGB(color);
    }
    
    protected void addBorderByExcel(PdfPCell cell , CellStyle style, Workbook wb) {
        cell.setBorderColorLeft(new BaseColor(this.getBorderRBG(wb,style.getLeftBorderColor())));
        cell.setBorderColorRight(new BaseColor(this.getBorderRBG(wb,style.getRightBorderColor())));
        cell.setBorderColorTop(new BaseColor(this.getBorderRBG(wb,style.getTopBorderColor())));
        cell.setBorderColorBottom(new BaseColor(this.getBorderRBG(wb,style.getBottomBorderColor())));
    }
    
    protected int getVAlignByExcel(short align) {
        int result = 0;
        if (align == CellStyle.VERTICAL_BOTTOM) {
            result = Element.ALIGN_BOTTOM;
        }
        if (align == CellStyle.VERTICAL_CENTER) {
            result = Element.ALIGN_MIDDLE;
        }
        if (align == CellStyle.VERTICAL_JUSTIFY) {
            result = Element.ALIGN_JUSTIFIED;
        }
        if (align == CellStyle.VERTICAL_TOP) {
            result = Element.ALIGN_TOP;
        }
        return result;
    }

    protected int getHAlignByExcel(short align) {
        int result = 0;
        if (align == CellStyle.ALIGN_LEFT) {
            result = Element.ALIGN_LEFT;
        }
        if (align == CellStyle.ALIGN_RIGHT) {
            result = Element.ALIGN_RIGHT;
        }
        if (align == CellStyle.ALIGN_JUSTIFY) {
            result = Element.ALIGN_JUSTIFIED;
        }
        if (align == CellStyle.ALIGN_CENTER) {
            result = Element.ALIGN_CENTER;
        }
        return result;
    }
    
    public int getRGB(Color color){
        int result = 0x00FFFFFF;
        
        int red = 0;
        int green = 0;
        int blue = 0;
        
        if (color instanceof HSSFColor) {
            HSSFColor hssfColor = (HSSFColor) color;
            short[] rgb = hssfColor.getTriplet();
            red = rgb[0];
            green = rgb[1];
            blue = rgb[2];
        }
        
        if (color instanceof XSSFColor) {
            XSSFColor xssfColor = (XSSFColor) color;
            byte[] rgb = xssfColor.getRgb();
            if(rgb != null) {
                red = (rgb[0] < 0) ? (rgb[0] + 256) : rgb[0];
                green = (rgb[1] < 0) ? (rgb[1] + 256) : rgb[1];
                blue = (rgb[2] < 0) ? (rgb[2] + 256) : rgb[2];
            }
        }
        
        if(red != 0 || green != 0 || blue != 0){
            result = new java.awt.Color(red, green, blue).getRGB();
        }
        return result;
    }
    
    public int getBorderRBG(Workbook wb  , short index){
        int result = 0;
        
        if(wb instanceof HSSFWorkbook){
            HSSFWorkbook hwb = (HSSFWorkbook)wb;
            HSSFColor color =  hwb.getCustomPalette().getColor(index);
            if(color != null){
                result = getRGB(color);
            }
        }
        
        if(wb instanceof XSSFWorkbook){
            XSSFColor color = new XSSFColor();
            color.setIndexed(index);
            result = getRGB(color);
        }
        
        return result;
    }
    
    @SuppressWarnings("finally")
    public static byte[] scale(byte[] bytes , double width, double height) {
        BufferedImage bufferedImage = null;
        BufferedImage bufTarget = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            bufferedImage = ImageIO.read(bais);
            double sx =  width / bufferedImage.getWidth();
            double sy =  height / bufferedImage.getHeight();
            int type = bufferedImage.getType();
            if (type == BufferedImage.TYPE_CUSTOM) {
                ColorModel cm = bufferedImage.getColorModel();
                WritableRaster raster = cm.createCompatibleWritableRaster((int)width, (int)height);
                boolean alphaPremultiplied = cm.isAlphaPremultiplied();
                bufTarget = new BufferedImage(cm, raster, alphaPremultiplied, null);
            } else {
                bufTarget = new BufferedImage((int)width, (int)height, type);
            }
            
            Graphics2D g = bufTarget.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawRenderedImage(bufferedImage, AffineTransform.getScaleInstance(sx, sy));
            g.dispose();
            
            if(bufTarget != null){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufTarget, "png", baos);
                byte[] result = baos.toByteArray();
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    private static class PDFPageEvent extends PdfPageEventHelper{
    	
        protected PdfTemplate template;
        
        public BaseFont baseFont;
        
        public String fontPath;
        
        public PDFPageEvent(String fontPath) {
        	this.fontPath = fontPath;
        }
        
        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try{
                this.template = writer.getDirectContent().createTemplate(100, 100);
                BaseFont baseFontChinese = BaseFont.createFont(this.fontPath, BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
                this.baseFont = new Font(baseFontChinese , 8, Font.NORMAL).getBaseFont();
            } catch(Exception e) {
                throw new ExceptionConverter(e);
            }
        }
        
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            //在每页结束的时候把“第x页”信息写道模版指定位置
            PdfContentByte byteContent = writer.getDirectContent();
            String text = "第" + writer.getPageNumber() + "页";
            float textWidth = this.baseFont.getWidthPoint(text, 8);
            float realWidth = document.right() - textWidth;
            //
            byteContent.beginText();
            byteContent.setFontAndSize(this.baseFont , 10);
            byteContent.setTextMatrix(realWidth , document.bottom());
            byteContent.showText(text);
            byteContent.endText();
            byteContent.addTemplate(this.template , realWidth , document.bottom());
        }
    }
    
}

