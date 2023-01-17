package com.speno.xmon.env;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class dom2Writer {

	public final static String setTypeAdd 	= "Add";
	public final static String setTypeDel 	= "Del";
	public final static String setTypeMod 	= "Mod";
	
	
	File pathProperties	= null;
	String rootPath		= "";
	
	public dom2Writer(File fi, String rootPath) {
		this.pathProperties = fi;
		this.rootPath = rootPath;
	}
	

	private Element getElement( Element currentElement, String string) {
		   List<Element> tempList = currentElement.getChildren();
		         
	         for(int i = 0; i < tempList.size(); i++) 
	         {
	             Element element = (Element) tempList.get(i);
	             
	             String name = element.getName();
	             if(name.equals(string)) 
	            	 return element;
	         }
	         return null;
	}

	
	public String getParentKey(String searchKey) {
		 FileInputStream in = null;
		try {
			in = new FileInputStream(pathProperties);

	         SAXBuilder builder		= new SAXBuilder();
	         Document doc			= builder.build(in);            
	         Element xmlRoot 		= doc.getRootElement().getChild("AmassServer");
	         
	         List<Element> tempList = xmlRoot.getChildren();
	         return this.getParentKey(tempList, searchKey);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (JDOMException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			try {
				if(in != null)  in.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}
	private String getParentKey(List<Element> tempList, String searchKey) {
        for(int i = 0; i < tempList.size(); i++) 
        {
            Element element = (Element) tempList.get(i);
            if(element.getName().equals(searchKey)) return element.getParentElement().getName();
            
            String t = this.getParentKey(element.getChildren(), searchKey);
            if( !t.equals("") ) 
            	return t;
        }
        return "";
	}


	/*
	 * types: Add, Del, Rep
	 * 
	 */
	public boolean setKeyValue(String parentKey, String key, String value, String type) {
		 FileInputStream in;
		 boolean bWrite = false;
			try {
				in = new FileInputStream(pathProperties);

		         SAXBuilder builder = new SAXBuilder();
		         Document doc = builder.build(in);            
		         Element xmlRoot = doc.getRootElement().getChild("AmassServer");
		         
		         List<Element> tempList = xmlRoot.getChildren();
		         
		         for(int i = 0; i < tempList.size(); i++) 
		         {
		             Element element = (Element) tempList.get(i);
		             if(!element.getName().equals(parentKey)) continue;

		             if(type.equals(setTypeAdd))
		             {
		            	 Element logText = new Element(key);
		            	 logText.setText(value);

		            	 element.addContent(logText);
		            	 bWrite = true;
		            	 break;
		             }

		             Element propElement = this.getElement(element, key);
		             if(propElement == null) return false;
		             if(type.equals(setTypeMod))
		             {
		            	 propElement.setText(value);
		            	 bWrite = true;
		            	 break;
		             }
		             if(type.equals(setTypeDel))
		             {
			             element.removeContent(propElement);
		            	 bWrite = true;
		            	 //break;
		             }
		         }
		         
		         if(bWrite)
		         {
	                 XMLOutputter xout = new XMLOutputter();
	                 Format fo = xout.getFormat();
	                 fo.setEncoding("euc-kr"); 
	                 fo.setIndent("\t");
	                 fo.setLineSeparator("\r\n");
	                 fo.setTextMode(Format.TextMode.TRIM);
	                 
	                 try 
	                 {            
	                     xout.setFormat(fo);
	                     xout.output(doc,  new FileWriter(pathProperties));
	                     return true;
	                 } catch (IOException e) {
	                     e.printStackTrace();
	                 }
		         }
			}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
	}







}
