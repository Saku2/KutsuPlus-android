import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ReadXMLFile {


	static boolean isValid(String txt)
	{
		for(int n=0;n<txt.length();n++)
		{
			if(Character.isDigit(txt.charAt(n)))
			{
				return false;
			}
		}
		if(txt.indexOf("talo")>0)
			return false;
		return true;
	}
	
	public static void main(String argv[]) {
        final Set<String> streets=new HashSet<String>();
		try {
            
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			

			DefaultHandler handler = new DefaultHandler() {
				boolean bway = false;
				//  way: <tag k="addr:street" v="Paasikivenkatu"/>
				// <tag k="name" v="Mäkelänkatu"/>
				// <tag k="name:fi" v="Mäkelänkatu"/>
				// <tag k="name:sv" v="Backasgatan"/>
				Set<String>potential=new HashSet<String>();
				boolean isStreet=false;
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {

					if (qName.equalsIgnoreCase("way")) {
						bway=true;
						isStreet=false;
					}
					if(bway)
					if (qName.equalsIgnoreCase("tag")) {
						String k=attributes.getValue("k");
					    if(k!=null)
					    {
					    	if(k.equals("name"))
					    		potential.add(attributes.getValue("v"));
					    	if(k.equals("name:fi"))
					    		potential.add(attributes.getValue("v"));
					    	if(k.equals("name:sv"))
					    		potential.add(attributes.getValue("v"));
					    	if(k.equals("addr:street"))
					    		potential.add(attributes.getValue("v"));
					    	/*if(k.equals("addr:housenumber"))
					    		notStreet=true;
					    	if(k.equals("building"))
					    		notStreet=true;
					    	if(k.equals("shop"))
					    		notStreet=true;
					    	if(k.equals("area"))
					    		notStreet=true;
					    	if(k.equals("parking"))
					    		notStreet=true;
					    	if(k.equals("amenity"))
					    		notStreet=true;
					    		*/
					    	if(k.equals("highway"))
					    		isStreet=true;
					    }
					}
					

				}
				public void endElement(String uri, String localName,
						String qName) throws SAXException {
				 
					if (qName.equalsIgnoreCase("way")) {
						bway=false;
						if(isStreet)
							for(String katu:potential)
								streets.add(katu);
						potential.clear();		
					}	
				 
					}
				 
					public void characters(char ch[], int start, int length) throws SAXException {
				 
						
				 
					}
				 
				};

			saxParser.parse("c:\\jo\\map.xml", handler);
			List<String> kadut=new ArrayList<String>();
			for(String katu:streets)
				if(katu.length()>1)
					if(isValid(katu))
				       kadut.add(katu);
			Collections.sort(kadut);
			for(String k:kadut)
				System.out.println(k);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}