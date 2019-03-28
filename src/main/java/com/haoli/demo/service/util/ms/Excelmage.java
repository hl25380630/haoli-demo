package com.haoli.demo.service.util.ms;

import java.awt.Dimension;
import java.util.List;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;

/**
 * excel里单元格的图片实体类
 * @author 李昊
 *
 */
public class Excelmage {
	
    protected Dimension dimension;
    
    protected byte[] bytes;
    
    protected ClientAnchor anchor;
    
    public Excelmage getCellImage(Cell cell) {
        Sheet sheet = cell.getSheet();
        if (sheet instanceof HSSFSheet) {
            HSSFSheet hssfSheet = (HSSFSheet) sheet;
            HSSFPatriarch hSSFPatriarch=  hssfSheet.getDrawingPatriarch();
            if(hSSFPatriarch == null) {
            	return null;
            }
            List<HSSFShape> shapes = hSSFPatriarch.getChildren();
            for (HSSFShape shape : shapes) { //对应xls类型的文件
                HSSFClientAnchor anchor = (HSSFClientAnchor) shape.getAnchor();
                if (shape instanceof HSSFPicture) {
                    HSSFPicture pic = (HSSFPicture) shape;
                    PictureData data = pic.getPictureData();
                    int row1 = anchor.getRow1();
                    int col1 = anchor.getCol1();
                    if(row1 == cell.getRowIndex() && col1 == cell.getColumnIndex()){
                        dimension = pic.getImageDimension();
                        this.anchor = anchor;
                        this.bytes = data.getData();
                    }
                }
            }
        }else if(sheet instanceof XSSFSheet) { //对应xlsx类型的文件
        	XSSFSheet xssfSheet = (XSSFSheet)sheet;
        	List<POIXMLDocumentPart> list = xssfSheet.getRelations();
            for (POIXMLDocumentPart part : list) {
  	          if (part instanceof XSSFDrawing) {
  	              XSSFDrawing drawing = (XSSFDrawing) part;
  	              List<XSSFShape> shapes = drawing.getShapes();
  	              for (XSSFShape shape : shapes) {
  	                  XSSFPicture picture = (XSSFPicture) shape;
  	                  XSSFClientAnchor anchor = picture.getPreferredSize();
  	                  CTMarker marker = anchor.getFrom();
  	                  if(marker.getRow() == cell.getRowIndex() && marker.getCol() == cell.getColumnIndex()) {
  	  	                  this.anchor = anchor;
  	  	                  this.bytes = picture.getPictureData().getData();
  	  	                  return this;
  	                  }
  	              }
  	          }
            }
        }
        return this;
    }
    
    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public byte[] getBytes() {
        return bytes;
    }
    
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
    
    public ClientAnchor getAnchor() {
        return anchor;
    }

    public void setAnchor(ClientAnchor anchor) {
        this.anchor = anchor;
    }
}

