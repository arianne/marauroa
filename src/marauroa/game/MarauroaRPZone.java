/* $Id: MarauroaRPZone.java,v 1.20 2004/03/16 00:00:43 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.game;

import java.util.*;

import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import marauroa.marauroad;

public class MarauroaRPZone implements RPZone
{
  private Map objects;
  private Perception perception;
  private JDBCRPObjectDatabase rpobjectDatabase;
	
  private static Random rand=new Random();
  
  public MarauroaRPZone()
	{
		rand.setSeed(new Date().getTime());
		objects=new LinkedHashMap();
		perception=new Perception(Perception.DELTA);
		
		try
		  {
          rpobjectDatabase=JDBCRPObjectDatabase.getDatabase();
          }
        catch(GameDatabaseException.NoDatabaseConfException e)
          {
          marauroad.trace("MarauroaRPZone::MarauroaRPZone","!",e.getMessage());
          System.exit(1);
          }
	}
  
  public void add(RPObject object) throws RPObjectInvalidException
	{
		try
		{
			RPObject.ID id=new RPObject.ID(object);
			objects.put(id,object);
			modify(object);
		}
		catch(Attributes.AttributeNotFoundException e)
		{
			marauroad.trace("MarauroaRPZone::add","X",e.getMessage());
			throw new RPObjectInvalidException(e.getAttribute());
		}
	}
  
  public void modify(RPObject object)
	{
		perception.modified(object);
	}
	
  public RPObject remove(RPObject.ID id) throws RPObjectNotFoundException
	{
		if(objects.containsKey(id))
		{
			RPObject object=(RPObject)objects.remove(id);
			perception.removed(object);
			return object;
		}
		else
		{
			throw new RPObjectNotFoundException(id);
		}
	}
  
  public RPObject get(RPObject.ID id) throws RPObjectNotFoundException
	{
		if(objects.containsKey(id))
		{
			return (RPObject)objects.get(id);
		}
		
		throw new RPObjectNotFoundException(id);
	}
  
  public boolean has(RPObject.ID id)
	{
		if(objects.containsKey(id))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
  
  public RPObject create()
	{
        return new RPObject(rpobjectDatabase.getValidRPObjectID());
	}
	
  public Iterator iterator()
	{
		return objects.values().iterator();
	}
	
  public Perception getPerception(RPObject.ID id, byte type)
	{
		if(type==Perception.DELTA)
		{
			return perception;
		}
		else
		{
			Perception p=new Perception(Perception.TOTAL);
			p.modifiedList=new ArrayList(objects.values());
			
			return p;
		}
	}
	
  public long size()
	{
		return objects.size();
	}
  
  public void print(PrintStream out)
	{
		Iterator it=iterator();
		
		while(it.hasNext())
		{
			RPObject object=(RPObject)it.next();
			out.println(object);
		}
	}
  
  public void nextTurn()
	{
		perception=new Perception(Perception.DELTA);
	}
	
	public Document toXML()
	{
		Document xml_doc = null;
		try
		{
			xml_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element zone_elem = xml_doc.createElement("rp_zone");
			Element objects_elem = xml_doc.createElement("objects");
			Iterator iter_obj = iterator();
			while(iter_obj.hasNext())
			{
				Element rp_obj_xml = xml_doc.createElement("rp_object");
				RPObject rp_object = (RPObject)iter_obj.next();
				rp_object.toXML(rp_obj_xml);
				objects_elem.appendChild(rp_obj_xml);
			}
			zone_elem.appendChild(objects_elem);
			if(perception!=null)
			{
				Element perc_elem = xml_doc.createElement("perception");
				perception.toXML(perc_elem);
				zone_elem.appendChild(perc_elem);
			}
			xml_doc.appendChild(zone_elem);
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		return(xml_doc);
	}
	
	public void fromXML(Document document)
	{
		if(document!=null)
		{
			Element zone_elem = document.getDocumentElement();
			NodeList nl_objects = zone_elem.getElementsByTagName("objects");
			if(nl_objects.getLength()==1)
			{
				Element elem_objects = (Element)nl_objects.item(0);
				NodeList nl_rp_objects = elem_objects.getElementsByTagName("rp_object");
				for (int i = 0; i < nl_rp_objects.getLength(); i++)
				{
					Element rp_obj_elem = (Element)nl_rp_objects.item(i);
					RPObject rp_object = new RPObject();
					rp_object.fromXML(rp_obj_elem);
					try
					{
						add(rp_object);
					}
					catch (RPZone.RPObjectInvalidException e)
					{
						marauroad.trace("MarauroaRPZone","X",e.getMessage());
					}
				}
			}
			else
			{
				marauroad.trace("MarauroaRPZone","E","Wrong zone xml document - count of <objects> element is not 1");
			}
			NodeList nl_perceptions = zone_elem.getElementsByTagName("perception");
			if(nl_perceptions.getLength()==1)
			{
				perception = new Perception((byte)0);
				perception.fromXML((Element)nl_perceptions.item(0));
			}
			else
			{
				marauroad.trace("MarauroaRPZone","E","Wrong zone xml document - count of <perception> element is "+nl_perceptions.getLength());
				perception = null;
			}
		}
	}
	
	public void saveToFile(String file)
	{
		Document document = toXML();
		try
		{
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty("indent","yes");
			try
			{
				DOMSource source = new DOMSource(document);
				StreamResult result = new StreamResult(new File(file));
				transformer.transform(source,result);
			}
			catch (TransformerException e)
			{
				e.printStackTrace();
			}
		}
		catch (TransformerFactoryConfigurationError e)
		{
			e.printStackTrace();
		}
		catch (TransformerConfigurationException e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadFromFile(String file)
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(file));
			fromXML(document);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
	}
	
}

