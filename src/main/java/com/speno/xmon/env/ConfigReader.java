package com.speno.xmon.env;

// Java stuff
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
// Xml Parser
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

// EIMS

// Logs based on agent message file
public class ConfigReader{
    private String		m_file;
    private ConfigIF	m_c;
    
	// Constructor
	public ConfigReader(String file, ConfigIF c){
		m_file = file;
		m_c = c;
	}
	
	// Reads a config file and sends simple, nested results to caller
	public String parse() {
	  BufferedReader fi = null;
		// Open file and parse
		try{
			File file = new File(m_file);
			if(file.exists()){
				if (isUnicode())
				  fi = new BufferedReader(new InputStreamReader(new FileInputStream(m_file), "Unicode"));
				else
				  fi = new BufferedReader(new InputStreamReader(new FileInputStream(m_file)));
				ConfigParser cp = new ConfigParser(m_c);
				SAXParserFactory factory = SAXParserFactory.newInstance();
    			SAXParser parser = factory.newSAXParser();
    			parser.parse(new InputSource(fi), cp);
    			fi.close();
			}
			else{
				FileNotFoundException e = new FileNotFoundException(m_file);
				throw e;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			if (fi != null) {
				try {
					fi.close();
				} catch (IOException e1) {//noting to do}
			}
			return "asysConfigReader::parse--> " + e.getMessage();
		}
		}
		return "";
	}

        /**
	* This method tests if a xml file format is Unicode
	* @return true if is a Unicode XML file otherwise false
	*/
	protected boolean isUnicode() {
		boolean isUnicode = true;
	  	BufferedReader fi = null;
		try{
		 	ConfigParser cp = new ConfigParser(m_c);
	      		SAXParserFactory factory = SAXParserFactory.newInstance();
		  	//factory.setValidating(false);
	      		SAXParser parser = factory.newSAXParser();
		  	fi = new BufferedReader(new InputStreamReader(new FileInputStream(m_file), "Unicode"));
			parser.parse(new InputSource(fi), cp);
	                fi.close();
		}
		catch (Exception e){
			isUnicode = false;
			if (fi != null){
				try {fi.close();} catch(Exception se) {}
			}
		}
		return isUnicode;
	 }
	 	
	// Inner class to get XML pieces
	class ConfigParser extends DefaultHandler {
		private ConfigIF m_c;

		// Constructor
		public ConfigParser(ConfigIF c) {
			m_c = c;
		}

		// Routines called from parser - serially
		public void startDocument() throws SAXException {
			
		}

		public void endDocument() throws SAXException {

		}

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			Hashtable attrs = new Hashtable();
			for (int i = 0; i < attributes.getLength(); i++) {
				attrs.put(attributes.getQName(i).toUpperCase(),	attributes.getValue(i));
			}
			m_c.startParms(qName.toUpperCase(), attrs);
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			m_c.endParms(qName.toUpperCase());
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
		}
	}

	
}