/* $Id: RPZone.java,v 1.16 2004/02/06 21:42:16 root777 Exp $ */
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public interface RPZone
{
  public static class RPObjectNotFoundException extends Exception
	{
		public RPObjectNotFoundException(RPObject.ID id)
		{
			super("RP Object ["+id+"] not found");
		}
	}
  
  public static class RPObjectInvalidException extends Exception
	{
		public RPObjectInvalidException(String attribute)
		{
			super("Object is invalid: It lacks of mandatory attribute ["+attribute+"]");
		}
	}
	
  public static class Perception
	{
		final public static byte DELTA=0;
		final public static byte TOTAL=1;
		
		public byte type;
		public List modifiedList;
		public List deletedList;
		
		public Perception(byte type)
		{
			this.type=type;
			modifiedList=new LinkedList();
			deletedList=new LinkedList();
		}
		
		public void modified(RPObject object)
		{
			if(!deletedList.contains(object))
			{
				if(modifiedList.contains(object))
				{
					modifiedList.remove(object);
				}
				
				modifiedList.add(object);
			}
		}
		
		public void removed(RPObject object)
		{
			deletedList.add(object);
		}
		
		public int size()
		{
			return (modifiedList.size()+deletedList.size());
		}
		
		public void clear()
		{
			modifiedList.clear();
			deletedList.clear();
		}
		
		public void toXML(Element xml_perception)
		{
			if(xml_perception!=null)
			{
				clear();
				Document xml_doc = xml_perception.getOwnerDocument();
				xml_perception.setAttribute("type",String.valueOf(type));
				Element mod_elem = xml_doc.createElement("modified");
				Iterator  it=modifiedList.iterator();
				while(it.hasNext())
				{
					RPObject entry=(RPObject)it.next();
					Element elem_rpobj = xml_doc.createElement("rp_object");
					entry.toXML(elem_rpobj);
					mod_elem.appendChild(elem_rpobj);
				}
				Element del_elem = xml_doc.createElement("deleted");
				it=deletedList.iterator();
				while(it.hasNext())
				{
					RPObject entry=(RPObject)it.next();
					Element elem_rpobj = xml_doc.createElement("rp_object");
					entry.toXML(elem_rpobj);
					del_elem.appendChild(elem_rpobj);
				}
				xml_perception.appendChild(mod_elem);
				xml_perception.appendChild(del_elem);
			}
		}
		
		public void fromXML(Element xml_perception)
		{
			if(xml_perception!=null)
			{
				type = Byte.parseByte(xml_perception.getAttribute("type"));
				NodeList nl = xml_perception.getElementsByTagName("deleted");
				if(nl.getLength()==1)
				{
					Element del_elem = (Element)nl.item(0);
					NodeList nl_objects = del_elem.getElementsByTagName("rp_object");
					for (int i = 0; i < nl_objects.getLength(); i++)
					{
						RPObject rp_object = new RPObject();
						rp_object.fromXML((Element)nl_objects.item(i));
						deletedList.add(rp_object);
					}
				}
				else
				{
					//can't be
				}
				nl = xml_perception.getElementsByTagName("modified");
				if(nl.getLength()==1)
				{
					Element mod_elem = (Element)nl.item(0);
					NodeList nl_objects = mod_elem.getElementsByTagName("rp_object");
					for (int i = 0; i < nl_objects.getLength(); i++)
					{
						RPObject rp_object = new RPObject();
						rp_object.fromXML((Element)nl_objects.item(i));
						modifiedList.add(rp_object);
					}
				}
				else
				{
					//can't be
				}
				
			}
		}
	}
  
  public void add(RPObject object) throws RPObjectInvalidException;
  public void modify(RPObject object) throws RPObjectNotFoundException;
  public RPObject remove(RPObject.ID id) throws RPObjectNotFoundException;
  public RPObject get(RPObject.ID id) throws RPObjectNotFoundException;
  public boolean has(RPObject.ID id);
  
  public RPObject create();
  public Iterator iterator();
  public long size();
  public Perception getPerception(RPObject.ID id, byte type);
  public void nextTurn();
	public Document toXML();
	public void fromXML(Document document);
}
