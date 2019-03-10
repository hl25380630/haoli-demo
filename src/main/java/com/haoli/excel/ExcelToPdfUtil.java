package com.haoli.excel;

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
	
    String anchorName = "anchorName";

    protected boolean setting = false;
    
    public static void main(String[] args) throws Exception {
        FileInputStream in = new FileInputStream(new File("C:\\Users\\10063731\\Desktop\\cip\\CIP服务器说明-新增版.xlsx"));
        FileOutputStream out = new FileOutputStream(new File("C:\\Users\\10063731\\Desktop\\cip\\99.pdf"));
        ExcelToPdfUtil pe = new ExcelToPdfUtil();
        pe.convert(in ,out);
    }
    
	public void convert(FileInputStream in, FileOutputStream out) throws Exception {
		//使用itext新建pdf文件，设置为A4纸大小
        Document document = new Document();
        document.setPageSize(PageSize.A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document, out);
        writer.setPageEvent(new PDFPageEvent());
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
        	document.add(pdfTableList.get(i));
        }
        document.close();
    }
	
	public PdfPTable parseContent(Sheet sheet, Workbook wb) throws Exception{
		int rowlength = sheet.getLastRowNum();
        List<PdfPCell> cells = new ArrayList<PdfPCell>();
        float[] widths = null;
        float mw = 0;
        for (int i = 0; i < rowlength; i++) {
        	//excel表的每一行
            Row row = sheet.getRow(i);
            if(row == null) {
            	continue;
            }
            int columNo = row.getLastCellNum();
            float[] cws = new float[columNo];
            for (int j = 0; j < columNo; j++) {
            	Cell cell = row.getCell(j);
                if(cell == null) {
                	continue;
                }
                //获取每一个单元格的宽度
                float cw = getPOIColumnWidth(cell, sheet);
                cws[cell.getColumnIndex()] = cw;
                if(isUsed(cell.getColumnIndex(), row.getRowNum(), sheet)){
                    continue;
                }
                cell.setCellType(Cell.CELL_TYPE_STRING);
                CellRangeAddress range = getColspanRowspanByExcel(row.getRowNum(), cell.getColumnIndex(), sheet);
                //
                int rowspan = 1;
                int colspan = 1;
                if (range != null) {
                    rowspan = range.getLastRow() - range.getFirstRow() + 1;
                    colspan = range.getLastColumn() - range.getFirstColumn() + 1;
                }
                //PDF单元格
                PdfPCell pdfpCell = new PdfPCell();
                pdfpCell.setBackgroundColor(new BaseColor(getBackgroundColorByExcel(cell.getCellStyle())));
                pdfpCell.setColspan(colspan);
                pdfpCell.setRowspan(rowspan);
                pdfpCell.setVerticalAlignment(getVAlignByExcel(cell.getCellStyle().getVerticalAlignment()));
                pdfpCell.setHorizontalAlignment(getHAlignByExcel(cell.getCellStyle().getAlignment()));
                pdfpCell.setPhrase(getPhrase(cell, wb));
                pdfpCell.setFixedHeight(this.getPixelHeight(row.getHeightInPoints()));
                addBorderByExcel(pdfpCell, cell.getCellStyle(), wb);
                addImageByPOICell(pdfpCell , cell , cw);
                //
                cells.add(pdfpCell);
                j += colspan - 1;
            }
            float rw = 0;
            for (int j = 0; j < cws.length; j++) {
                rw += cws[j];
            }
            if (rw > mw ||  mw == 0) {
                widths = cws;
                mw = rw;
            }
        }
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100);
        for (PdfPCell pdfpCell : cells) {
            table.addCell(pdfpCell);
        }
        return table;
    }
    
    
    protected Phrase getPhrase(Cell cell, Workbook wb){
        Anchor anchor = new Anchor(cell.getStringCellValue() , getFontByExcel(cell.getCellStyle(), wb));
        anchor.setName(anchorName);
        this.setting = true;
        return anchor;
    }
    
    protected void addImageByPOICell(PdfPCell pdfpCell , Cell cell , float cellWidth) throws BadElementException, MalformedURLException, IOException{
       Excelmage poiImage = new Excelmage().getCellImage(cell);
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
    	int poiCWidth = sheet.getColumnWidth(cell.getColumnIndex());
        int colWidthpoi = poiCWidth;
        int widthPixel = 0;
        if (colWidthpoi >= 416) {
            widthPixel = (int) (((colWidthpoi - 416.0) / 256.0) * 8.0 + 13.0 + 0.5);
        } else {
            widthPixel = (int) (colWidthpoi / 416.0 * 13.0 + 0.5);
        }
        return widthPixel;
    }
    
    protected CellRangeAddress getColspanRowspanByExcel(int rowIndex, int colIndex, Sheet sheet) {
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
    
    protected boolean isUsed(int colIndex , int rowIndex, Sheet sheet){
        boolean result = false;
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

    protected Font getFontByExcel(CellStyle style, Workbook wb) {
       Font result = new Font(Resource.BASE_FONT_CHINESE , 8 , Font.NORMAL);
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
        
        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try{
                this.template = writer.getDirectContent().createTemplate(100, 100);
                this.baseFont = new Font(Resource.BASE_FONT_CHINESE , 8, Font.NORMAL).getBaseFont();
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

