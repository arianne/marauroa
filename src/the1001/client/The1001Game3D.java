/* $Id: The1001Game3D.java,v 1.2 2004/02/15 23:24:24 root777 Exp $ */
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

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;
import marauroa.game.Attributes;
import marauroa.game.RPObject;
import marauroa.marauroad;
import org.newdawn.j3d.loaders.ModelLoadingException;
import org.newdawn.j3d.loaders.md2.MD2Loader;
import org.newdawn.j3d.loaders.md2.MD2Model;
import org.newdawn.j3d.loaders.md2.MD2ModelInstance;
import the1001.RPCode;
import the1001.objects.Gladiator;

/**
 *@author Waldemar Tribus
 */
public class The1001Game3D
extends Canvas3D implements KeyListener, GameDataModelListenerIF
{
	
  private SimpleUniverse universe;
  private List models;
  private ModelAnimator animator;
	//  private String animations[] = {"pain","stand","jump","flip","salute","taunt",
//		"wave","point","crstand","crwalk","crattack",
//		"crpain","crdeath","death","run","attack"};
	//  private int currAnim;
//	private GameDataModel gdm;
	private Arena arena;
  
  public The1001Game3D(GameDataModel gdm)
  {
		super(SimpleUniverse.getPreferredConfiguration());
//		this.gdm = gdm;
		models = new ArrayList();
		animator = null;
//		currAnim = 0;
		BranchGroup scene = createSceneGraph();
		universe = new SimpleUniverse(this);
		universe.getViewingPlatform().setNominalViewingTransform();
		universe.addBranchGraph(scene);
		addKeyListener(this);
		if(gdm!=null)
		{
			gdm.addListener(this);
		}
  }
  
  private BranchGroup createSceneGraph()
  {
		BoundingSphere bounds =
		new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
		
		
		// Create the root of the branch graph
		BranchGroup root = new BranchGroup();
		
		BranchGroup button_scissor = createButton("scissor_Button.png","scissor_Button.png");
		TransformGroup tgs = new TransformGroup();
		Transform3D trs1 = new Transform3D();
		trs1.setScale(0.1);
		Transform3D trs2 = new Transform3D();
		Vector3f v3f = new Vector3f(-0.85f,0.6f,0.0f);
		trs2.set(v3f);
		trs2.mul(trs1);
		tgs.setTransform(trs2);
		tgs.addChild(button_scissor);
		MouseHandler mh = new MouseHandler(button_scissor,this);
		mh.setBounds(bounds);
		root.addChild(mh);
//		PickTool.setCapabilities(button_scissor, PickTool.INTERSECT_FULL);
		root.addChild(tgs);
//		root.addChild(mh);
		
		BranchGroup button_paper = createButton("paper_Button.png","paper_Button.png");
		tgs = new TransformGroup();
		trs1 = new Transform3D();
		trs1.setScale(0.1);
		trs2 = new Transform3D();
		v3f = new Vector3f(-0.85f,0.3f,0.0f);
		trs2.set(v3f);
		trs2.mul(trs1);
		tgs.setTransform(trs2);
		tgs.addChild(button_paper);
		root.addChild(tgs);
		
		BranchGroup button_stone = createButton("stone_Button.png","stone_Button.png");
		tgs = new TransformGroup();
		trs1 = new Transform3D();
		trs1.setScale(0.1);
		trs2 = new Transform3D();
		v3f = new Vector3f(-0.85f,0.0f,0.0f);
		trs2.set(v3f);
		trs2.mul(trs1);
		tgs.setTransform(trs2);
		tgs.addChild(button_stone);
		root.addChild(tgs);
		
		
		
		Background background = new Background(createSky());
		background.setBounds(bounds);
		root.addChild(background);
		
		arena = createArene();
		
		// Create the TransformGroup node and initialize it to the
		// identity. Enable the TRANSFORM_WRITE capability so that
		// our behavior code can modify it at run time. Add it to
		// the root of the subgraph.
		TransformGroup trans = new TransformGroup();
		trans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		//    objRoot.addChild(objTrans);
		
		// Create a simple Shape3D node; add it to the scene graph.
		trans.addChild(arena);
		trans.addChild(createSky());
		
		//    objTrans.addChild(new Cylinder(0.3f,0.6f));
		
		// Create a new Behavior object that will perform the
		// desired operation on the specified tranrotate.rotX(Math.PI/4.0d);
		// it into the scene graph.
		
		
//		Transform3D yAxis = new Transform3D();
//		Alpha rotationAlpha = new Alpha(-1, 16000);
//
//		RotationInterpolator rotator =
//		new RotationInterpolator(rotationAlpha, trans, yAxis,
//														 0.0f, (float) Math.PI*2.0f);
//		rotator.setSchedulingBounds(bounds);
//
//
		TransformGroup tg = new TransformGroup();
		Transform3D tr1 = new Transform3D();
		Vector3f v = new Vector3f(0.0f,-0.6f,-1.2f);
		tr1.set(v);
		Transform3D tr2 = new Transform3D();
		tr2.rotX(Math.PI/10);
		tr2.mul(tr1);
		tg.setTransform(tr2);
		tg.addChild(trans);
//		tg.addChild(rotator);
		
		
		
		TransformGroup objTrans = new TransformGroup();
		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		objTrans.addChild(tg);
		
		MouseRotate behavior = new MouseRotate();
	  behavior.setTransformGroup(objTrans);
	  objTrans.addChild(behavior);
	  behavior.setSchedulingBounds(bounds);
		
	  // Create the zoom behavior node
	  MouseZoom behavior2 = new MouseZoom();
	  behavior2.setTransformGroup(objTrans);
	  objTrans.addChild(behavior2);
	  behavior2.setSchedulingBounds(bounds);
		
	  // Create the translate behavior node
	  MouseTranslate behavior3 = new MouseTranslate();
	  behavior3.setTransformGroup(objTrans);
	  objTrans.addChild(behavior3);
	  behavior3.setSchedulingBounds(bounds);
		
		root.addChild(objTrans);
		
		
		
		
//		root.addChild(rotator);
//		root.addChild(arena);
//		root.addChild(createSky());
		
		
		//    root.addChild(arena);
		
		//    TextureLoader loader = new TextureLoader(Resources.getImageUrl("SkyDome.jpg"), this);
//		Background background = new Background();
//		background.setColor(new Color3f(0.1f, 0.1f, 1.0f));
//		background.setApplicationBounds(bounds);
//		background.setCapability(Background.ALLOW_COLOR_WRITE);
//		background.setApplicationBounds(bounds);
//		root.addChild(background);
//
//
//
		//    LinearFog fog = new LinearFog();
		//    fog.setColor(new Color3f(0.0f, 0.0f, 0.0f));
		//    fog.setFrontDistance(16.7);
		//    fog.setBackDistance(23.0);
		//    fog.setCapability(LinearFog.ALLOW_COLOR_WRITE);
		//    fog.setCapability(LinearFog.ALLOW_DISTANCE_WRITE);
		//    fog.setInfluencingBounds(bounds);
		//    root.addChild(fog);
		
		root.compile();
		
		return root;
  }
  
	private Arena createArene()
	{
		Arena arena = new Arena();
		return(arena);
	}
	
	
  private BranchGroup createSky()
  {
		TextureLoader loader = new TextureLoader(Resources.getImageUrl("SkyDome.jpg"), this);
		Appearance appear = new Appearance();
		appear.setTexture(loader.getTexture());
		TextureAttributes texAttr = new TextureAttributes();
		texAttr.setTextureMode(TextureAttributes.MODULATE);
		appear.setTextureAttributes(texAttr);
		Sphere sphere = new Sphere(50.0f,Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS_INWARD,appear);
		BranchGroup bg = new BranchGroup();
		TransformGroup tg = new TransformGroup();
		Transform3D transform = new Transform3D();
		transform.rotZ(Math.PI);
		tg.setTransform(transform);
		tg.addChild(sphere);
		bg.addChild(tg);
		bg.compile();
		return(bg);
  }
	
  
  
  private BranchGroup loadModel(String modelname)
  {
		try
		{
			TransformGroup tg = new TransformGroup();
			Transform3D transform = new Transform3D();
			transform.setScale(0.2f);
			tg.setTransform(transform);
			MD2Loader loader     = new MD2Loader();
			InputStream model_is = Resources.getModelUrl(modelname+".md2").openStream();
			InputStream skin_is  = Resources.getModelUrl(modelname+".pcx").openStream();
			
			MD2Model model = loader.loadWithPCX(model_is, skin_is);
			MD2ModelInstance mod_instance = model.getInstance();
			// "run"; "attack"
			mod_instance.setAnimation("stand");
			synchronized(models)
			{
				models.add(mod_instance);
			}
			BranchGroup temp = new BranchGroup();
			temp.addChild(tg);
			tg.addChild(mod_instance);
			if(animator==null)
			{
				animator = new ModelAnimator();
				animator.start();
			}
			return temp;
		}
		catch (ModelLoadingException e)
		{
			System.err.println(e);
			System.exit(1);
		}
		catch (IOException e)
		{
			System.err.println(e);
			System.exit(1);
		}
		return null;
  }
	
	
	/**
	 * Invoked when a key has been typed.
	 * See the class description for {@link KeyEvent} for a definition of
	 * a key typed event.
	 */
	public void keyTyped(KeyEvent e)
	{
	}
	
	/**
	 * Invoked when a key has been pressed.
	 * See the class description for {@link KeyEvent} for a definition of
	 * a key pressed event.
	 */
	public void keyPressed(KeyEvent e)
	{
		if(KeyEvent.VK_ESCAPE == e.getKeyCode())
		{
			System.exit(-1);
		}
		System.out.println(e);
	}
	
	/**
	 * Invoked when a key has been released.
	 * See the class description for {@link KeyEvent} for a definition of
	 * a key released event.
	 */
	public void keyReleased(KeyEvent e)
	{
	}
	
	public void modelUpdated(GameDataModel gdm)
	{
		RPObject[] spectators = gdm.getSpectators();
		RPObject[] fighters = gdm.getFighters();
		arena.setSpectators(spectators);
		arena.setFighters(fighters);
	}
  
  private class ModelAnimator
	extends Thread
  {
		public ModelAnimator()
		{
			setName("Model animator...");
		}
		/**
		 * A quick running thread
		 */
		public void run()
		{
			int counter = 0;
			while (true)
			{
				try
				{
					Thread.sleep(250);
				}
				catch (Exception e)
				{
				}
				synchronized(models)
				{
//					if(counter==50)
//					{
//						currAnim++;
//						counter = 0;
//						if(currAnim>=animations.length)
//						{
//							currAnim = 0;
//						}
//						System.out.println("Current animation " + animations[currAnim]);
//						for (int i = 0; i < models.size(); i++)
//						{
//							if (models.get(i)!=null)
//							{
//								((MD2ModelInstance)models.get(i)).setAnimation(animations[currAnim]);
//							}
//						}
//					}
//					else
//					{
					for (int i = 0; i < models.size(); i++)
					{
						if (models.get(i)!=null)
						{
							((MD2ModelInstance)models.get(i)).nextFrame();
						}
					}
//					}
				}
				counter++;
			}
		}
  }
	
	
	private final class Arena
	extends BranchGroup
	{
		private final static double radius = 2.0;
		private final static int columnCount = 12;
		// object_id --> Node
		private Map mSpectators;
		// object_id --> Node
		private Map mFighters;
		
		
		public Arena()
		{
			mSpectators = new HashMap(5);
			mFighters   = new HashMap(2);
			
			QuadArray plane = new QuadArray(4, GeometryArray.COORDINATES
																			| GeometryArray.TEXTURE_COORDINATE_2);
			Point3f p = new Point3f();
			p.set(-100.0f, 0.0f,  100.0f);
			plane.setCoordinate(3, p);
			p.set(-100.0f, 0.0f, -100.0f);
			plane.setCoordinate(2, p);
			p.set( 100.0f, 0.0f, -100.0f);
			plane.setCoordinate(1, p);
			p.set( 100.0f, 0.1f,  100.0f);
			plane.setCoordinate(0, p);
			
			TexCoord2f tc = new TexCoord2f(0.0f, 100.0f);
			plane.setTextureCoordinate(0,0,tc);
			tc = new TexCoord2f(0.0f, 0.0f);
			plane.setTextureCoordinate(0,1,tc);
			tc = new TexCoord2f(100.0f, 0.0f);
			plane.setTextureCoordinate(0,2,tc);
			tc = new TexCoord2f(100.0f, 100.0f);
			plane.setTextureCoordinate(0,3,tc);
			
			TextureLoader loader = new TextureLoader(Resources.getImageUrl("terrain_texture.png"), The1001Game3D.this);
			Appearance appearance = new Appearance();
			Texture texture = loader.getTexture();
			appearance.setTexture(texture);
			TextureAttributes texture_attr = new TextureAttributes();
			texture_attr.setTextureMode(TextureAttributes.MODULATE);
			appearance.setTextureAttributes(texture_attr);
			
			Shape3D shape = new Shape3D(plane,appearance);
			addChild(shape);
			
			
			loader = new TextureLoader(Resources.getImageUrl("Column_texture.png"), The1001Game3D.this);
			appearance = new Appearance();
			texture = loader.getTexture();
			appearance.setTexture(texture);
			texture_attr = new TextureAttributes();
			texture_attr.setTextureMode(TextureAttributes.MODULATE);
			appearance.setTextureAttributes(texture_attr);
			
			double angle_inc = 2*Math.PI/columnCount;
			for (int i = 0; i < columnCount; i++)
			{
				Cylinder cylinder = new Cylinder(0.06f,0.88f,Cylinder.GENERATE_NORMALS|Cylinder.GENERATE_TEXTURE_COORDS,appearance);
				Transform3D transform = new Transform3D();
				double sin = Math.sin(angle_inc*i);
				double cos = Math.cos(angle_inc*i);
				Vector3f v3f = new Vector3f((float)(sin*radius),0.50f,(float)(cos*radius));
				transform.set(v3f);
				TransformGroup tg2 = new TransformGroup();
				tg2.setTransform(transform);
				tg2.addChild(cylinder);
				addChild(tg2);
			}
			setCapability(Arena.ALLOW_CHILDREN_WRITE);
			setCapability(Arena.ALLOW_CHILDREN_EXTEND);
		}
		
		public void setSpectators(RPObject[] spectators)
		{
			for (int i = 0; i < spectators.length; i++)
			{
				try
				{
					String id = spectators[i].get("object_id");
					
					if(mSpectators.get(id)==null)
					{
						String name = spectators[i].get(RPCode.var_name);
						Node node = loadModel("billgates");
						
						Font3D f3d = new Font3D(new Font("default", Font.PLAIN, 2),
																		new FontExtrusion());
						Text3D txt = new Text3D(f3d, name);
						Shape3D sh = new Shape3D();
						Appearance app = new Appearance();
						Material mm = new Material();
						mm.setLightingEnable(true);
						mm.setAmbientColor(new Color3f(1.0f,0.0f,0.0f));
						mm.setDiffuseColor(new Color3f(0.0f,1.0f,0.0f));
						mm.setDiffuseColor(new Color3f(0.0f,0.0f,1.0f));
						mm.setEmissiveColor(new Color3f(0.0f,1.0f,1.0f));
						app.setMaterial(mm);
						sh.setGeometry(txt);
						sh.setAppearance(app);
						double angle = Math.random()*2*Math.PI;
						double sin = Math.sin(angle);
						double cos = Math.cos(angle);
						TransformGroup tg = new TransformGroup();
						Transform3D t = new Transform3D();
						Vector3f v3f = new Vector3f((float)(sin*(radius+radius*0.1)),0.26f,(float)(cos*(radius+radius*0.1)));
						t.set(v3f);
						TransformGroup tgt = new TransformGroup();
						Transform3D tx = new Transform3D();
						v3f = new Vector3f(0,0.20f,0);
						tx.set(v3f);
						Transform3D tt = new Transform3D();
						tt.setScale(0.03);
						tx.mul(tt);
						tgt.setTransform(tx);
						tgt.addChild(sh);
						
//						Point3d p3d_eye = new Point3d(v3f);
//						Point3d p3d_0 = new Point3d(0.0f,0.0f,0.0f);
//						Transform3D t2 = new Transform3D();
//						t2.lookAt(p3d_eye,p3d_0,new Vector3d(0.0f,1.0f,0.0f));
//						t2.mul(t);
						tg.setTransform(t);
						
						tg.addChild(node);
						tg.addChild(tgt);
						BranchGroup bg = new BranchGroup();
						bg.addChild(tg);
//						bg.addChild(tgt);
						bg.compile();
						addChild(bg);
						marauroad.trace("Arena::setSpectators","D","new setSpectators added: " + name);
						mSpectators.put(id,node);
					}
				}
				catch (Attributes.AttributeNotFoundException e)
				{
					marauroad.trace("Arena::setSpectators","X",e.getMessage());
				}
			}
		}
		
		public void setFighters(RPObject[] fighters)
		{
			for (int i = 0; i < fighters.length; i++)
			{
				try
				{
					String id = fighters[i].get("object_id");
					
					if(mFighters.get(id)==null)
					{
						String look = fighters[i].get(RPCode.var_look);
						String name = fighters[i].get(RPCode.var_name);
						Node node = loadModel(look);
						Font3D f3d = new Font3D(new Font("default", Font.PLAIN, 2),
																		new FontExtrusion());
						Text3D txt = new Text3D(f3d, name);
						Shape3D sh = new Shape3D();
						Appearance app = new Appearance();
						Material mm = new Material();
						mm.setLightingEnable(true);
						mm.setAmbientColor(new Color3f(1.0f,0.0f,0.0f));
						mm.setDiffuseColor(new Color3f(0.0f,1.0f,0.0f));
						mm.setDiffuseColor(new Color3f(0.0f,0.0f,1.0f));
						mm.setEmissiveColor(new Color3f(0.0f,1.0f,1.0f));
						app.setMaterial(mm);
						sh.setGeometry(txt);
						sh.setAppearance(app);
						double angle = Math.random()*2*Math.PI;
						double sin   = Math.sin(angle);
						double cos   = Math.cos(angle);
						TransformGroup tg = new TransformGroup();
						Transform3D t = new Transform3D();
						Vector3f v3f = new Vector3f((float)(sin*(radius*0.4)),0.26f,(float)(cos*(radius*0.4)));
						t.set(v3f);
						tg.setTransform(t);
						tg.addChild(node);
						
						TransformGroup tgt = new TransformGroup();
						Transform3D tx = new Transform3D();
						v3f = new Vector3f(0,0.20f,0);
						tx.set(v3f);
						Transform3D tt = new Transform3D();
						tt.setScale(0.03);
						tx.mul(tt);
						tgt.setTransform(tx);
						tgt.addChild(sh);
						
						tg.addChild(tgt);
						BranchGroup bg = new BranchGroup();
						bg.addChild(tg);
//						bg.addChild(tgt);
						bg.compile();
						addChild(bg);
						marauroad.trace("Arena::setFighters","D","new fighter added: " + name + ", look = " +look);
						mFighters.put(id,node);
					}
				}
				catch (Attributes.AttributeNotFoundException e)
				{
					marauroad.trace("Arena::setFighters","X",e.getMessage());
				}
			}
		}
	}
	
	/**
	 *
	 */
  public static void main(String[] args)
  {
		JFrame frame = new JFrame("Arena");
		The1001Game3D gamedisplay = new The1001Game3D(null);
		frame.getContentPane().add(gamedisplay);
//		frame.setSize(800,600);
		frame.setUndecorated(true);
		RPObject [] spectators = new RPObject[2];
		RPObject [] fighters   = new RPObject[2];
		for (int i = 0; i < spectators.length; i++)
		{
			RPObject rp = new RPObject();
			rp.put("object_id","spec_"+i);
			rp.put("name","Spectator_"+i);
			spectators[i] = rp;
		}
		for (int i = 0; i < fighters.length; i++)
		{
			try
			{
				RPObject rp = new Gladiator(new RPObject.ID(i));
				rp.put("object_id","glad_"+i);
				fighters[i] = rp;
			}
			catch (RPObject.SlotAlreadyAddedException e) {}
		}
		
		gamedisplay.arena.setSpectators(spectators);
		gamedisplay.arena.setFighters(fighters);
		
		GraphicsDevice vDevice = GraphicsEnvironment.
		getLocalGraphicsEnvironment().getDefaultScreenDevice();
//		if(vDevice.isDisplayChangeSupported())
//		{
//			System.out.println("display change supported");
//		}
//
//		DisplayMode modes[] = vDevice.getDisplayModes();
//		vDevice.setDisplayMode(modes[67]);
//		vDevice.setFullScreenWindow(frame);
//		DisplayMode mode =vDevice.getDisplayMode();
//		System.out.println(mode.getWidth()+"x"+mode.getHeight()+", "+mode.getBitDepth() + " @"+mode.getRefreshRate());
//
////		DisplayMode modes[] = vDevice.getDisplayModes();
//		for (int i = 0; i < modes.length; i++)
//		{
//			System.out.println("["+i+"]="+modes[i].getWidth()+"x"+modes[i].getHeight()+", "+modes[i].getBitDepth() + " @"+modes[i].getRefreshRate());
//		}
//
//
//		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		vDevice.setFullScreenWindow(SwingUtilities.getWindowAncestor(frame));
		frame.setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
		frame.show();
		
  }
	
	private BranchGroup createButton(String id, String image)
	{
		BranchGroup bg = new BranchGroup();
		QuadArray plane = new QuadArray(4, GeometryArray.COORDINATES
																		| GeometryArray.TEXTURE_COORDINATE_2);
		Point3f p = new Point3f();
		p.set(-1.0f, 1.0f, 0.0f);
		plane.setCoordinate(3, p);
		p.set(1.0f, 1.0f, 0.0f);
		plane.setCoordinate(2, p);
		p.set( 1.0f, -1.0f, 0.0f);
		plane.setCoordinate(1, p);
		p.set( -1.0f, -1.0f,  0.0f);
		plane.setCoordinate(0, p);
		
		TexCoord2f tc = new TexCoord2f(0.0f, 1.0f);
		plane.setTextureCoordinate(0,3,tc);
		tc = new TexCoord2f(1.0f, 1.0f);
		plane.setTextureCoordinate(0,2,tc);
		tc = new TexCoord2f(1.0f, 0.0f);
		plane.setTextureCoordinate(0,1,tc);
		tc = new TexCoord2f(0.0f, 0.0f);
		plane.setTextureCoordinate(0,0,tc);
		
		TextureLoader loader = new TextureLoader(Resources.getImageUrl(image), The1001Game3D.this);
		Appearance appearance = new Appearance();
		Texture texture = loader.getTexture();
		appearance.setTexture(texture);
		TextureAttributes texture_attr = new TextureAttributes();
		texture_attr.setTextureMode(TextureAttributes.MODULATE);
		appearance.setTextureAttributes(texture_attr);
		
		Shape3D shape = new Shape3D(plane,appearance);
		shape.setCapability(Shape3D.ENABLE_PICK_REPORTING);
		bg.addChild(shape);
		bg.compile();
		return(bg);
	}
	
	
	public class MouseHandler extends Behavior
	{
		private WakeupCondition wCond;
		private PickCanvas pickCanvas;
		
		// Constructor
		public MouseHandler(BranchGroup bGroup, Canvas3D canvas3D)
		{
			//event that runs your processStimulus method
			wCond = new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED);
			
			//create and initialize picking object
			pickCanvas = new PickCanvas(canvas3D, bGroup);
			pickCanvas.setTolerance(0.1f); //accuracy
			pickCanvas.setMode(PickCanvas.GEOMETRY_INTERSECT_INFO);
		}
		
		// Initialization
		public void initialize()
		{
			//start waiting for event
			wakeupOn(wCond);
		}
		
		// Event handling
		public void processStimulus(Enumeration criteria)
		{
			//get event that occured
			MouseEvent event = (MouseEvent) ((WakeupOnAWTEvent)
																			 criteria.nextElement()).getAWTEvent()[0];
			
			pickCanvas.setShapeLocation(event);
//			Point3d eyePos = pickCanvas.getStartPosition();
			
			//get result of event
			PickResult pickResult = pickCanvas.pickClosest();
			
			//... and picked object
			Node node = pickResult.getObject();
			marauroad.trace("MouseHandler::processStimulus","D","Node selected: " + node);
			//start waiting for next event
			wakeupOn(wCond);
		}
	}
	
}

