/* $Id: SelectBehavior.java,v 1.1 2004/02/19 00:28:50 root777 Exp $ */
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


package the1001.client;


import javax.media.j3d.*;

import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickIntersection;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;
import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Class:       SelectBehavior
 *
 *
 */
public class SelectBehavior extends Behavior
{
  private PickCanvas canvas;
  private PickResult[] result;
  private Appearance look1, look2, look3, look4, look5;
  private TransformGroup[] sphTrans = new TransformGroup [6];
  private Sphere[] sph = new Sphere [6];
  private Transform3D spht3 = new Transform3D();
	private Map listeners;
	
  public SelectBehavior(Canvas3D canvas3D, BranchGroup branchGroup,
												float size)
	{
		canvas = new PickCanvas(canvas3D, branchGroup);
		canvas.setTolerance(0.05f);
		canvas.setMode(PickTool.BOUNDS);
		// Create an Appearance.
		look3 = new Appearance();
		Color3f objColor = new Color3f(0.5f, 0.0f, 0.0f);
		Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
		Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
		look3.setMaterial(new Material(objColor, black, objColor, white, 50.0f));
		look3.setCapability (Appearance.ALLOW_MATERIAL_WRITE);
		
		look2 = new Appearance ();
		look2.setMaterial(new Material(objColor, black, objColor, white, 50.0f));
		PolygonAttributes pa = new PolygonAttributes();
		pa.setPolygonMode(PolygonAttributes.POLYGON_LINE);
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		look2.setPolygonAttributes(pa);
		
		look1 = new Appearance();
		objColor = new Color3f(1.0f, 1.0f, 1.0f);
		look1.setMaterial(new Material(objColor, black, objColor, white, 50.0f));
		
		look4 = new Appearance();
		objColor = new Color3f(0.0f, 0.8f, 0.0f);
		look4.setMaterial(new Material(objColor, black, objColor, white, 50.0f));
		look5 = new Appearance();
		objColor = new Color3f(0.0f, 0.0f, 0.8f);
		look5.setMaterial(new Material(objColor, black, objColor, white, 50.0f));
		for (int i=0;i<6;i++)
		{
			switch (i)
			{
			case 0:
				sph[i] = new Sphere(size*1.15f, look3);
				break;
			case 1:
				sph[i] = new Sphere(size*1.1f, look4);
				break;
			default:
				sph[i] = new Sphere(size, look5);
				break;
			}
			sph[i].setPickable (false);
			sphTrans[i] = new TransformGroup ();
			sphTrans[i].setCapability (TransformGroup.ALLOW_TRANSFORM_READ);
			sphTrans[i].setCapability (TransformGroup.ALLOW_TRANSFORM_WRITE);
			
			// Add sphere, transform
			branchGroup.addChild (sphTrans[i]);
			sphTrans[i].addChild (sph[i]);
		}
  }
	
  public void initialize()
	{
		wakeupOn (new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED));
  }
	
  public void processStimulus (Enumeration criteria)
	{
		WakeupCriterion wakeup;
		AWTEvent[] event;
		int eventId;
		
		while (criteria.hasMoreElements())
		{
			try
			{
				wakeup = (WakeupCriterion) criteria.nextElement();
				if (wakeup instanceof WakeupOnAWTEvent)
				{
					event = ((WakeupOnAWTEvent)wakeup).getAWTEvent();
					for (int i=0; i<event.length; i++)
					{
						eventId = event[i].getID();
						if (eventId == MouseEvent.MOUSE_PRESSED)
						{
							int x = ((MouseEvent)event[i]).getX();
							int y = ((MouseEvent)event[i]).getY();
							canvas.setShapeLocation(x, y);
							
							Point3d eyePos = canvas.getStartPosition ();
							result = canvas.pickAllSorted();
							if (result != null)
							{
								PickIntersection pi =
									result[0].getClosestIntersection(eyePos);
								Object obj = result[0].getObject();
								if(obj!=null)
								{
									if(obj instanceof Node)
									{
										Object user_obj = ((Node)obj).getUserData();
										fireListener(user_obj);
									}
								}
								GeometryArray curGeomArray = pi.getGeometryArray();
								
								Vector3d v = new Vector3d();
								Point3d intPt = pi.getPointCoordinatesVW();
								v.set(intPt);
								spht3.setTranslation (v);
								sphTrans[0].setTransform (spht3);
								
								Point3d closestVert = pi.getClosestVertexCoordinatesVW();
								v.set(closestVert);
								spht3.setTranslation (v);
								sphTrans[1].setTransform (spht3);
								
								Point3d []ptw = pi.getPrimitiveCoordinatesVW();
								Point3d []pt = pi.getPrimitiveCoordinates();
								for (int k=0;k<pt.length;k++)
								{
									v.set(ptw[k]);
									spht3.setTranslation (v);
									sphTrans[k+2].setTransform (spht3);
								}
								
								Color4f iColor4 = null;
								Color3f iColor = null;
								
								if (curGeomArray != null)
								{
									int vf = curGeomArray.getVertexFormat();
									
									if (((vf &
													(GeometryArray.COLOR_3 |
														 GeometryArray.COLOR_4)) != 0) &&
												(null != (iColor4 =
																		pi.getPointColor())))
									{
										iColor =
											new Color3f(iColor4.x, iColor4.y, iColor4.z);
										
										// Change the point's color
										look3.setMaterial(new Material(iColor, new Color3f (0.0f, 0.0f, 0.0f), iColor, new Color3f(1.0f, 1.0f, 1.0f), 50.0f));
									}
								}
							}
						}
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				wakeupOn (new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED));
			}
		}
  }
	
	public ActionListener setActionListener(Object id, ActionListener al)
	{
		if(listeners == null)
		{
			listeners = new HashMap(3);
		}
		listeners.put(id,al);
		return(al);
	}
	
	public void removeActionListener(Object id)
	{
		if(listeners != null)
		{
			listeners.remove(id);
		}
	}
	
	private void fireListener(Object id)
	{
		if(listeners!=null)
		{
			ActionListener al = (ActionListener)listeners.get(id);
			if(al!=null)
			{
				al.actionPerformed(new ActionEvent(this,0,String.valueOf(id)));
			}
		}
	}
}

