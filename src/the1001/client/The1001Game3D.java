/* $Id: The1001Game3D.java,v 1.17 2004/07/13 20:31:54 arianne_rpg Exp $ */
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

import java.util.*;
import javax.media.j3d.*;
import com.mnstarfire.loaders3d.Inspector3DS;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.swing.JFrame;
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
  private List modelInstances;
  private ModelAnimator animator;
  // private String animations[] = {"pain","stand","jump","flip","salute","taunt",
  // "wave","point","crstand","crwalk","crattack",
  // "crpain","crdeath","death","run","attack"};
  // private int currAnim;
  private GameDataModel gdm;
  private Arena arena;
  private BranchGroup bgCenterText;
  private Text3D centerText3D;
  private Map models;
  private MD2Loader md2Loader;
  public The1001Game3D(GameDataModel gdm)
  {
    super(SimpleUniverse.getPreferredConfiguration());
    this.gdm = gdm;
    modelInstances = new ArrayList();
    models         = new HashMap();
    md2Loader      = new MD2Loader();
    animator       = null;
    
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
      new BoundingSphere(new Point3d(0.0,0.0,0.0), 20.0);
    // Create the root of the branch graph
    BranchGroup root = new BranchGroup();
    BranchGroup button_stone = createButton(GameDataModel.CMD_STONE,"stone_Button.png");
    TransformGroup tgs = new TransformGroup();
    Transform3D trs1 = new Transform3D();
    
    trs1.setScale(0.05);
    
    Transform3D trs2 = new Transform3D();
    Vector3f v3f = new Vector3f(-0.34f,0.22f,1.5f);
    
    trs2.set(v3f);
    trs2.mul(trs1);
    tgs.setTransform(trs2);
    tgs.addChild(button_stone);
    root.addChild(tgs);
    
    BranchGroup button_paper = createButton(GameDataModel.CMD_PAPER,"paper_Button.png");
    
    tgs = new TransformGroup();
    trs1 = new Transform3D();
    trs1.setScale(0.05);
    trs2 = new Transform3D();
    v3f = new Vector3f(-0.34f,0.12f,1.5f);
    trs2.set(v3f);
    trs2.mul(trs1);
    tgs.setTransform(trs2);
    tgs.addChild(button_paper);
    root.addChild(tgs);
    
    BranchGroup button_scissor = createButton(GameDataModel.CMD_SCISSOR,"scissor_Button.png");
    
    tgs = new TransformGroup();
    trs1 = new Transform3D();
    trs1.setScale(0.05);
    trs2 = new Transform3D();
    v3f = new Vector3f(-0.34f,0.02f,1.5f);
    trs2.set(v3f);
    trs2.mul(trs1);
    tgs.setTransform(trs2);
    tgs.addChild(button_scissor);
    root.addChild(tgs);
    
    BranchGroup button_fight = createButton(GameDataModel.CMD_FIGHT,"RequestFight_Button.gif");
    
    tgs = new TransformGroup();
    trs1 = new Transform3D();
    trs1.setScale(0.05);
    trs2 = new Transform3D();
    v3f = new Vector3f(+0.34f,-0.18f,1.5f);
    trs2.set(v3f);
    trs2.mul(trs1);
    tgs.setTransform(trs2);
    tgs.addChild(button_fight);
    root.addChild(tgs);
    
    BranchGroup button_vote_up = createButton(GameDataModel.CMD_VOTE_DOWN,"thumbDown_Button.gif");
    
    tgs = new TransformGroup();
    trs1 = new Transform3D();
    trs1.setScale(0.05);
    trs2 = new Transform3D();
    v3f = new Vector3f(+0.34f,0.22f,1.5f);
    trs2.set(v3f);
    trs2.mul(trs1);
    tgs.setTransform(trs2);
    tgs.addChild(button_vote_up);
    root.addChild(tgs);
    
    BranchGroup button_vote_down = createButton(GameDataModel.CMD_VOTE_UP,"thumbUp_Button.gif");
    
    tgs = new TransformGroup();
    trs1 = new Transform3D();
    trs1.setScale(0.05);
    trs2 = new Transform3D();
    v3f = new Vector3f(+0.34f,0.12f,1.5f);
    trs2.set(v3f);
    trs2.mul(trs1);
    tgs.setTransform(trs2);
    tgs.addChild(button_vote_down);
    root.addChild(tgs);
    bgCenterText = new BranchGroup();
    
    Font3D f3d = new Font3D(new Font("Times New Roman", Font.PLAIN, 1),
                            new FontExtrusion());
    
    centerText3D = new Text3D(f3d);
    centerText3D.setCapability(Text3D.ALLOW_STRING_WRITE);
    centerText3D.setCapability(Text3D.ALLOW_STRING_READ);
    
    Shape3D sh_txt = new Shape3D();
    Appearance app_txt = new Appearance();
    Material mm_txt = new Material();
    
    // mm_txt.setLightingEnable(true);
    // mm_txt.setSpecularColor(new Color3f(1.0f,0.0f,0.0f));
    // mm_txt.setAmbientColor(new Color3f(1.0f,0.0f,0.0f));
    // mm_txt.setDiffuseColor(new Color3f(0.0f,1.0f,0.0f));
    mm_txt.setEmissiveColor(new Color3f(1.0f,1.0f,1.0f));
    app_txt.setMaterial(mm_txt);
    sh_txt.setGeometry(centerText3D);
    sh_txt.setAppearance(app_txt);
    sh_txt.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    PickTool.setCapabilities(sh_txt, PickTool.INTERSECT_FULL);
    bgCenterText.addChild(sh_txt);
    bgCenterText.setPickable(false);
    tgs = new TransformGroup();
    trs1 = new Transform3D();
    trs1.setScale(0.03);
    trs2 = new Transform3D();
    v3f = new Vector3f(-0.28f,0.1f,1.5f);
    trs2.set(v3f);
    trs2.mul(trs1);
    tgs.setTransform(trs2);
    tgs.addChild(bgCenterText);
    root.addChild(tgs);
    centerText3D.setString("Request fame: 6");
    arena = createArene();
    
    // Create the TransformGroup node and initialize it to the
    // identity. Enable the TRANSFORM_WRITE capability so that
    // our behavior code can modify it at run time. Add it to
    // the root of the subgraph.
    TransformGroup trans = new TransformGroup();
    
    trans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    // objRoot.addChild(objTrans);
    
    // Create a simple Shape3D node; add it to the scene graph.
    trans.addChild(arena);
    trans.addChild(createSky());
    
    TransformGroup tg = new TransformGroup();
    Transform3D tr1 = new Transform3D();
    Vector3f v = new Vector3f(0.0f,-0.6f,-1.2f);
    
    tr1.set(v);
    
    Transform3D tr2 = new Transform3D();
    
    tr2.rotX(Math.PI/10);
    tr2.mul(tr1);
    tg.setTransform(tr2);
    tg.addChild(trans);
    
    // tg.addChild(rotator);
    
    
    
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
    
    SelectBehavior pick = new SelectBehavior (this, root,0.005f);
    
    pick.setSchedulingBounds (bounds);
    root.addChild (pick);
    if(gdm!=null)
    {
      pick.setActionListener(GameDataModel.CMD_PAPER,gdm.getActionHandler());
      pick.setActionListener(GameDataModel.CMD_SCISSOR,gdm.getActionHandler());
      pick.setActionListener(GameDataModel.CMD_STONE,gdm.getActionHandler());
      pick.setActionListener(GameDataModel.CMD_FIGHT,gdm.getActionHandler());
      pick.setActionListener(GameDataModel.CMD_VOTE_UP,gdm.getActionHandler());
      pick.setActionListener(GameDataModel.CMD_VOTE_DOWN,gdm.getActionHandler());
    }
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
    
    sphere.setCapability(Node.ALLOW_PICKABLE_READ);
    sphere.setPickable(false);
    
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
  
  private MD2ModelInstance loadModel(String modelname)
  {
    try
    {
      MD2Model model = (MD2Model)models.get(modelname);
      
      if(model==null)
      {
        URL url = Resources.getModelUrl(modelname+".md2");
        
        if(url==null)
        {
          url = Resources.getModelUrl("billgates.md2");
        }
        
        InputStream model_is = url.openStream();
        
        url = Resources.getModelUrl(modelname+".pcx");
        if(url==null)
        {
          url = Resources.getModelUrl("billgates.pcx");
        }
        
        InputStream skin_is  = url.openStream();
        
        model = md2Loader.loadWithPCX(model_is, skin_is);
        models.put(modelname,model);
      }
      
      MD2ModelInstance mod_instance = model.getInstance();
      
      mod_instance.setAnimation("stand");
      mod_instance.setCapability(Node.ALLOW_PICKABLE_READ);
      mod_instance.setPickable(false);
      synchronized(modelInstances)
      {
        modelInstances.add(mod_instance);
      }
      if(animator==null)
      {
        animator = new ModelAnimator();
        animator.start();
      }
      return mod_instance;
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
    List spectators = gdm.getSpectators();
    List fighters = gdm.getFighters();
    
    arena.setSpectators(spectators);
    arena.setFighters(fighters);
    arena.setArena(gdm.getArena());
    arena.setArenaMode(gdm.getStatus());
    arena.setOwnGladiator(gdm.getFirstOwnGladiator());
    arena.setOwnCharacter(gdm.getOwnCharacter());
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
          Thread.sleep(100);
        }
        catch (Exception e)
        {
        }
        synchronized(modelInstances)
        {
          for (int i = 0; i < modelInstances.size(); i++)
          {
            if (modelInstances.get(i)!=null)
            {
              ((MD2ModelInstance)modelInstances.get(i)).nextFrame();
            }
          }
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
    private String mode;
    private String ownGladiatorID;
    private RPObject ownCharacter;
    private RPObject rpArena;
    public Arena()
    {
      mode = GameDataModel.ARENA_MODE_WAITING;
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
      
      shape.setPickable(false);
      shape.setCollidable(true);
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
        Cylinder cylinder = new Cylinder(0.06f,0.88f,Cylinder.GENERATE_NORMALS|Cylinder.GENERATE_TEXTURE_COORDS|Cylinder.ENABLE_GEOMETRY_PICKING,appearance);
        Transform3D transform = new Transform3D();
        double sin = Math.sin(angle_inc*i);
        double cos = Math.cos(angle_inc*i);
        Vector3f v3f = new Vector3f((float)(sin*radius),0.50f,(float)(cos*radius));
        
        transform.set(v3f);
        
        TransformGroup tg2 = new TransformGroup();
        
        tg2.setTransform(transform);
        tg2.addChild(cylinder);
        cylinder.setPickable(false);
        cylinder.setCollidable(true);
        addChild(tg2);
      }
      
      URL url_model = Resources.getModelUrl("Orc_Arena.3DS");
      Inspector3DS inloader = new Inspector3DS(url_model);
      
      marauroad.trace("Arena","D",""+url_model.toExternalForm());
      marauroad.trace("Arena","D",Resources.getModelBaseUrl("Orc_Arena.3DS").toExternalForm());
      inloader.setURLBase(Resources.getModelBaseUrl("Orc_Arena.3DS").toExternalForm());
      inloader.parseIt(); // process the file
      
      TransformGroup the_model = inloader.getModel();
      Transform3D t = new Transform3D();
      
      t.setScale(0.0015f);
      the_model.setTransform(t);
      
      BranchGroup bg = new BranchGroup();
      
      bg.addChild(the_model);
      bg.setPickable(false);
      addChild(bg);
      setCapability(BranchGroup.ALLOW_CHILDREN_READ);
      setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
      setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
      setCapability(ALLOW_DETACH);
    }
    
    /**
     * Method setArena
     *
     * @param    arena               a  RPObject
     *
     */
    public void setArena(RPObject arena)
    {
      this.rpArena = arena;
    }
    
    /**
     * Method setOwnCharacter
     *
     * @param    ownCharacter        a  RPObject
     *
     */
    public void setOwnCharacter(RPObject ownCharacter)
    {
      this.ownCharacter = ownCharacter;
    }
    
    /**
     * Method setOwnGladiator
     *
     * @param    gladiator           a  RPObject
     *
     */
    public void setOwnGladiator(RPObject gladiator)
    {
      if(gladiator!=null)
      {
        try
        {
          this.ownGladiatorID=gladiator.get(RPCode.var_object_id);
        }
        catch (AttributeNotFoundException e)
        {
          marauroad.trace("Arena::setOwnGladiator","X","gladiator without id! "+gladiator);
        }
      }
      else
      {
        this.ownGladiatorID = "";
      }
    }
    
    public void setArenaMode(String mode)
    {
      this.mode = mode;
      if(RPCode.var_waiting.equals(mode))
      {
        centerText3D.setString("Arena waiting...");
      }
      else if(RPCode.var_request_fame.equals(mode))
      {
        String timeout = "";
        String thumbs_up = "";
        String thumbs_down = "";
        String waiting = "";
        String fame = "";
        
        if(rpArena!=null)
        {
          try
          {
            timeout      = rpArena.get(RPCode.var_timeout);
          }
          catch (AttributeNotFoundException e)
          {
            timeout ="?";
          }
          try
          {
            thumbs_up    = rpArena.get(RPCode.var_thumbs_up);
          }
          catch (AttributeNotFoundException e)
          {
            thumbs_up ="?";
          }
          try
          {
            thumbs_down  = rpArena.get(RPCode.var_thumbs_down);
          }
          catch (AttributeNotFoundException e)
          {
            thumbs_down ="?";
          }
          try
          {
            waiting      = rpArena.get(RPCode.var_waiting);
          }
          catch (AttributeNotFoundException e)
          {
            waiting ="?";
          }
          try
          {
            fame         = rpArena.get(RPCode.var_karma);
          }
          catch (AttributeNotFoundException e)
          {
            fame ="?";
          }
        }
        centerText3D.setString("Request fame("+fame+"): "+timeout + " Up: "+thumbs_up+" Down: "+thumbs_down+" Wait: "+waiting);
      }
      else if(RPCode.var_fighting.equals(mode))
      {
        centerText3D.setString("");
      }
      else if(RPCode.var_setting_next_combat.equals(mode))
      {
        centerText3D.setString("Wait for next combat...");
      }
    }
    
    public void setSpectators(List spectators)
    {
      HashSet hs = new HashSet();
      
      try
      {
        for (int i = 0; i < spectators.size(); i++)
        {
          hs.add(((RPObject)spectators.get(i)).get(RPCode.var_object_id));
        }
      }
      catch (AttributeNotFoundException e) {}
      
      List al_removed = new ArrayList();
      
      for (Iterator iter = mSpectators.keySet().iterator(); iter.hasNext();)
      {
        Object id = iter.next();
        
        if(!hs.contains(id))
        {
          // remove....
          ModelAndBranch mab = (ModelAndBranch)mSpectators.get(id);
          
          if(mab != null && mab.bgroup!=null)
          {
            removeChild(mab.bgroup);
            al_removed.add(id);
          }
        }
      }
      for (int i = 0; i < al_removed.size(); i++)
      {
        mSpectators.remove(al_removed.get(i));
      }
      for (int i = 0; i < spectators.size(); i++)
      {
        try
        {
          String id = ((RPObject)spectators.get(i)).get(RPCode.var_object_id);
          ModelAndBranch mab = (ModelAndBranch)mSpectators.get(id);
          
          if(mab==null)
          {
            String name = ((RPObject)spectators.get(i)).get(RPCode.var_name);
            MD2ModelInstance model = loadModel("billgates");
            
            mab = new ModelAndBranch();
            mab.model = model;
            placeModel(name,mab,radius+radius*0.1);
            marauroad.trace("Arena::setSpectators","D","new setSpectators added: " + name);
            mSpectators.put(id,mab);
          }
          if(ownCharacter!=null)
          {
            String ownCharacterID = ownCharacter.get(RPCode.var_object_id);
            
            if(ownCharacterID!=null)
            {
              String id_w = ((RPObject)spectators.get(i)).get(RPCode.var_object_id);
              
              if(ownCharacterID.equals(id_w))
              {
                marauroad.trace("Arena::setSpectators()","D","own character, setting color.");
                
                Material mm_txt = new Material();
                
                mm_txt.setLightingEnable(true);
                mm_txt.setAmbientColor(new Color3f(0.0f,1.0f,0.0f));
                mm_txt.setDiffuseColor(new Color3f(0.0f,1.0f,0.0f));
                mm_txt.setDiffuseColor(new Color3f(0.0f,1.0f,0.0f));
                mm_txt.setEmissiveColor(new Color3f(0.0f,1.0f,0.0f));
                
                Appearance appearance = new Appearance();
                
                appearance.setMaterial(mm_txt);
                mab.nameLabel.setAppearance(appearance);
              }
            }
          }
        }
        catch (AttributeNotFoundException e)
        {
          marauroad.trace("Arena::setSpectators","X",e.getMessage());
        }
      }
    }
    
    public void setFighters(List fighters)
    {
      HashSet hs = new HashSet();
      
      try
      {
        for (int i = 0; i < fighters.size(); i++)
        {
          hs.add(((RPObject)fighters.get(i)).get(RPCode.var_object_id));
        }
      }
      catch (AttributeNotFoundException e) {}
      
      List al_removed = new ArrayList();
      
      for (Iterator iter = mFighters.keySet().iterator(); iter.hasNext();)
      {
        Object id = iter.next();
        
        if(!hs.contains(id))
        {
          // remove....
          ModelAndBranch mab = (ModelAndBranch)mFighters.get(id);
          
          if(mab != null && mab.bgroup!=null)
          {
            removeChild(mab.bgroup);
            al_removed.add(id);
          }
        }
      }
      for (int i = 0; i < al_removed.size(); i++)
      {
        mFighters.remove(al_removed.get(i));
      }
      for (int i = 0; i < fighters.size(); i++)
      {
        try
        {
          String id = ((RPObject)fighters.get(i)).get(RPCode.var_object_id);
          String name = ((RPObject)fighters.get(i)).get(RPCode.var_name);
          
          marauroad.trace("Arena::setFighters","D","Name = " + name);
          
          ModelAndBranch mab = (ModelAndBranch)mFighters.get(id);
          MD2ModelInstance model;
          
          if(mab==null)
          {
            String look = ((RPObject)fighters.get(i)).get(RPCode.var_look);
            
            model = loadModel(look);
            mab = new ModelAndBranch();
            mab.model = model;
            placeModel(name,mab,radius*0.1f);
            marauroad.trace("Arena::setFighters","D","new fighter added: " + name + ", look = " +look);
            mFighters.put(id,mab);
          }
          model = mab.model;
          if(GameDataModel.ARENA_MODE_FIGHTING.equals(mode))
          {
            marauroad.trace("Arena::setFighters","D","Arena is in fight mode");
            
            int damage = -1;
            int hp = -1;
            
            try
            {
              damage = ((RPObject)fighters.get(i)).getInt(RPCode.var_damage);
              hp = ((RPObject)fighters.get(i)).getInt(RPCode.var_hp);
              marauroad.trace("Arena::setFighters","D","Damage:"+damage);
              marauroad.trace("Arena::setFighters","D","HP    :"+hp);
              if(damage>0)
              {
                model.setAnimation("pain");
                ((Text3D)mab.nameLabel.getUserData()).setString(name+ " " +damage);
              }
              else
              {
                model.setAnimation("attack");
                ((Text3D)mab.nameLabel.getUserData()).setString(name);
              }
              ((Text3D)mab.healthLabel.getUserData()).setString(""+hp);
            }
            catch (AttributeNotFoundException e)
            {
            }
            if(ownGladiatorID!=null)
            {
              String id_w = ((RPObject)fighters.get(i)).get(RPCode.var_object_id);
              
              if(ownGladiatorID.equals(id_w))
              {
                marauroad.trace("Arena::setFighters()","D","own gladiator, setting color.");
                
                Material mm_txt = new Material();
                
                mm_txt.setLightingEnable(true);
                mm_txt.setAmbientColor(new Color3f(0.0f,1.0f,0.0f));
                mm_txt.setDiffuseColor(new Color3f(0.0f,1.0f,0.0f));
                mm_txt.setDiffuseColor(new Color3f(0.0f,1.0f,0.0f));
                mm_txt.setEmissiveColor(new Color3f(0.0f,1.0f,0.0f));
                
                Appearance appearance = new Appearance();
                
                appearance.setMaterial(mm_txt);
                mab.nameLabel.setAppearance(appearance);
              }
            }
          }
          else if(GameDataModel.ARENA_MODE_REQ_FAME.equals(mode))
          {
            marauroad.trace("Arena::setFighters","D","Arena is in req fame mode");
            
            int hp =-1;
            
            try
            {
              hp = ((RPObject)fighters.get(i)).getInt(RPCode.var_hp);
              if(hp<=0)
              {
                model.setAnimation("death");
              }
              else
              {
                model.setAnimation("salute");
              }
            }
            catch (AttributeNotFoundException e) {}
            marauroad.trace("Arena::setFighters","D","Heal points:"+hp);
          }
          else if(GameDataModel.ARENA_MODE_WAITING.equals(mode))
          {
            marauroad.trace("Arena::setFighters","D","Arena is in waiting mode");
            model.setAnimation("stand");
          }
          else
          {
            marauroad.trace("Arena::setFighters","D","Arena is in unknown mode ["+mode+"]");
          }
        }
        catch (AttributeNotFoundException e)
        {
          marauroad.trace("Arena::setFighters","X",e.getMessage());
        }
      }
    }
    
    private void placeModel(String name, ModelAndBranch model_br, double radius)
    {
      double angle = Math.random()*2*Math.PI;
      double sin = Math.sin(angle);
      double cos = Math.cos(angle);
      BranchGroup bg_main = new BranchGroup();
      
      model_br.bgroup = new BranchGroup();
      
      TransformGroup tg_scale_all = new TransformGroup();
      Transform3D trans_scale_and_translate_all = new Transform3D();
      Vector3f v3f = new Vector3f((float)(sin*radius),0.26f,(float)(cos*radius));
      
      trans_scale_and_translate_all.set(v3f);
      
      Transform3D trot = new Transform3D();
      float angle_rot = v3f.angle(new Vector3f(-1.0f,0.0f,0.0f));
      
      if(angle>=Math.PI/2 && angle<=3*Math.PI/2)
      {
        angle_rot = -angle_rot;
      }
      //
      trot.rotY(angle_rot);
      
      Transform3D t = new Transform3D();
      
      t.setScale(0.2f);
      trot.mul(t);
      trans_scale_and_translate_all.mul(trot);
      tg_scale_all.setTransform(trans_scale_and_translate_all);
      
      Transform3D trans_scale_text = new Transform3D();
      
      trans_scale_text.setScale(0.2);
      
      // name label
      Font3D f3d = new Font3D(new Font("default", Font.PLAIN, 2),
                              new FontExtrusion());
      Text3D txt = new Text3D(f3d, name);
      
      txt.setCapability(Text3D.ALLOW_STRING_WRITE);
      txt.setCapability(Text3D.ALLOW_STRING_READ);
      
      // txt.setString(""+(angle*180/Math.PI));
      Shape3D sh_txt = new Shape3D();
      Appearance appearance = new Appearance();
      Material mm_txt = new Material();
      
      mm_txt.setLightingEnable(true);
      mm_txt.setAmbientColor(new Color3f(1.0f,0.0f,0.0f));
      mm_txt.setDiffuseColor(new Color3f(0.0f,1.0f,0.0f));
      mm_txt.setEmissiveColor(new Color3f(0.0f,1.0f,1.0f));
      appearance.setMaterial(mm_txt);
      sh_txt.setGeometry(txt);
      sh_txt.setAppearance(appearance);
      sh_txt.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      sh_txt.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
      sh_txt.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
      PickTool.setCapabilities(sh_txt, PickTool.INTERSECT_FULL);
      model_br.nameLabel = sh_txt;
      sh_txt.setUserData(txt);
      
      TransformGroup tg_label_name = new TransformGroup();
      
      tg_label_name.addChild(sh_txt);
      
      Transform3D tr_txt = new Transform3D();
      Transform3D tr2_txt = new Transform3D();
      
      tr2_txt.setTranslation(new Vector3f(0.0f,1.0f,0.0f));
      tr_txt.rotY(Math.PI/2);
      tr_txt.mul(tr2_txt);
      tr_txt.mul(trans_scale_text);
      tg_label_name.setTransform(tr_txt);
      tg_scale_all.addChild(tg_label_name);
      // health label
      
      f3d = new Font3D(new Font("default", Font.PLAIN, 2),
                       new FontExtrusion());
      txt = new Text3D(f3d, name);
      txt.setCapability(Text3D.ALLOW_STRING_WRITE);
      txt.setCapability(Text3D.ALLOW_STRING_READ);
      sh_txt = new Shape3D();
      appearance = new Appearance();
      mm_txt = new Material();
      mm_txt.setLightingEnable(true);
      mm_txt.setAmbientColor(new Color3f(1.0f,0.0f,0.0f));
      mm_txt.setDiffuseColor(new Color3f(0.0f,1.0f,0.0f));
      mm_txt.setDiffuseColor(new Color3f(0.0f,0.0f,1.0f));
      mm_txt.setEmissiveColor(new Color3f(0.0f,1.0f,1.0f));
      appearance.setMaterial(mm_txt);
      sh_txt.setGeometry(txt);
      sh_txt.setAppearance(appearance);
      sh_txt.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      sh_txt.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
      sh_txt.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
      PickTool.setCapabilities(sh_txt, PickTool.INTERSECT_FULL);
      txt.setString("100");
      model_br.healthLabel = sh_txt;
      sh_txt.setUserData(txt);
      
      Transform3D trans = new Transform3D();
      
      trans.rotX(-Math.PI/2);
      
      Transform3D trans2 = new Transform3D();
      
      trans2.rotZ(-Math.PI/2);
      trans.mul(trans2);
      trans2 = new Transform3D();
      trans2.setTranslation(new Vector3f(0.0f,0.0f,-0.5f));
      trans.mul(trans2);
      
      // trans.mul(trans_scale_text);
      
      TransformGroup tr_gr = new TransformGroup();
      
      tr_gr.setTransform(trans);
      tr_gr.addChild(sh_txt);
      tg_scale_all.addChild(tr_gr);
      model_br.bgroup.addChild(model_br.model);
      tg_scale_all.addChild(model_br.bgroup);
      bg_main.addChild(tg_scale_all);
      model_br.bgroup = bg_main;
      bg_main.setCapability(BranchGroup.ALLOW_DETACH);
      addChild(bg_main);
    }
    
    private void _placeModel(String name, ModelAndBranch model_br, double radius)
    {
      BranchGroup bg_model = new BranchGroup();
      TransformGroup tg_model_scale = new TransformGroup();
      Transform3D trans1 = new Transform3D();
      
      trans1.setScale(0.2f);
      
      // name label
      Font3D f3d = new Font3D(new Font("default", Font.PLAIN, 2),
                              new FontExtrusion());
      Text3D txt = new Text3D(f3d, name);
      
      txt.setCapability(Text3D.ALLOW_STRING_WRITE);
      txt.setCapability(Text3D.ALLOW_STRING_READ);
      
      Shape3D sh_txt = new Shape3D();
      Appearance appearance = new Appearance();
      Material mm_txt = new Material();
      
      mm_txt.setLightingEnable(true);
      mm_txt.setAmbientColor(new Color3f(1.0f,0.0f,0.0f));
      mm_txt.setDiffuseColor(new Color3f(0.0f,1.0f,0.0f));
      mm_txt.setDiffuseColor(new Color3f(0.0f,0.0f,1.0f));
      mm_txt.setEmissiveColor(new Color3f(0.0f,1.0f,1.0f));
      appearance.setMaterial(mm_txt);
      sh_txt.setGeometry(txt);
      sh_txt.setAppearance(appearance);
      sh_txt.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      sh_txt.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
      sh_txt.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
      PickTool.setCapabilities(sh_txt, PickTool.INTERSECT_FULL);
      model_br.nameLabel = sh_txt;
      sh_txt.setUserData(txt);
      
      double angle = Math.random()*2*Math.PI;
      double sin = Math.sin(angle);
      double cos = Math.cos(angle);
      TransformGroup tg = new TransformGroup();
      Transform3D t = new Transform3D();
      Vector3f v3f = new Vector3f((float)(sin*radius),0.26f,(float)(cos*radius));
      
      t.set(v3f);
      
      TransformGroup tgt = new TransformGroup();
      Transform3D tx = new Transform3D();
      Vector3f v3f1 = new Vector3f(0,0.20f,0);
      
      tx.set(v3f1);
      
      Transform3D tt = new Transform3D();
      
      tt.setScale(0.03);
      tx.mul(tt);
      
      Transform3D trot = new Transform3D();
      
      trot.rotY(v3f.angle(new Vector3f(-1.0f,0.0f,0.0f)));
      tx.mul(trot);
      tgt.setTransform(tx);
      tgt.addChild(sh_txt);
      // health label
      
      f3d = new Font3D(new Font("default", Font.PLAIN, 2),
                       new FontExtrusion());
      txt = new Text3D(f3d, name);
      txt.setCapability(Text3D.ALLOW_STRING_WRITE);
      txt.setCapability(Text3D.ALLOW_STRING_READ);
      sh_txt = new Shape3D();
      appearance = new Appearance();
      mm_txt = new Material();
      mm_txt.setLightingEnable(true);
      mm_txt.setAmbientColor(new Color3f(1.0f,0.0f,0.0f));
      mm_txt.setDiffuseColor(new Color3f(0.0f,1.0f,0.0f));
      mm_txt.setDiffuseColor(new Color3f(0.0f,0.0f,1.0f));
      mm_txt.setEmissiveColor(new Color3f(0.0f,1.0f,1.0f));
      appearance.setMaterial(mm_txt);
      sh_txt.setGeometry(txt);
      sh_txt.setAppearance(appearance);
      sh_txt.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      sh_txt.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
      sh_txt.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
      PickTool.setCapabilities(sh_txt, PickTool.INTERSECT_FULL);
      txt.setString("100");
      model_br.healthLabel = sh_txt;
      sh_txt.setUserData(txt);
      
      Transform3D trans = new Transform3D();
      
      trans.rotX(Math.PI/2);
      
      Transform3D trans2 = new Transform3D();
      
      trans2.rotY(Math.PI);
      trans.mul(trans2);
      
      TransformGroup tr_gr = new TransformGroup();
      
      tr_gr.addChild(sh_txt);
      tr_gr.setTransform(trans);
      tgt.addChild(tr_gr);
      tg.setTransform(t);
      trans1.mul(trot);
      tg_model_scale.setTransform(trans1);
      tg_model_scale.addChild(model_br.model);
      bg_model.addChild(tg_model_scale);
      tg.addChild(bg_model);
      tg.addChild(tgt);
      
      BranchGroup bg = new BranchGroup();
      
      bg.addChild(tg);
      model_br.bgroup=bg;
      bg.setCapability(BranchGroup.ALLOW_DETACH);
      bg.compile();
      addChild(bg);
    }
  } // Arena
  private BranchGroup createButton(String id, String image)
  {
    BranchGroup bg = new BranchGroup();
    QuadArray plane = new QuadArray(4, GeometryArray.COORDINATES
                                      | GeometryArray.TEXTURE_COORDINATE_2);
    Point3f p = new Point3f(-1.0f, 1.0f, 0.0f);
    
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
    
    shape.setUserData(id);
    
    TransparencyAttributes ta = new TransparencyAttributes();
    
    ta.setTransparencyMode (TransparencyAttributes.FASTEST);
    // ta.setSrcBlendFunction(TransparencyAttributes.BLEND_SRC_ALPHA);
    // ta.setDstBlendFunction(TransparencyAttributes.BLEND_SRC_ALPHA);
    ta.setTransparency (0.0f);
    appearance.setTransparencyAttributes(ta);
    PickTool.setCapabilities(shape, PickTool.INTERSECT_FULL);
    
    float width  = texture.getWidth();
    float height = texture.getHeight();
    float prop_x  = width/height;
    float prop_y  = 1.0f;
    float prop_z  = 1.0f;
    
    if(prop_x<1 && prop_x!=0)
    {
      prop_y = 1/prop_x;
      prop_x = 1;
    }
    marauroad.trace("The1001Game3D::createButton","D","Button = "+image+", width="+width+", height="+height + ", prop_x="+prop_x);
    
    TransformGroup tg = new TransformGroup();
    Transform3D transform = new Transform3D(new float[]{prop_x,0,0,0,0,prop_y,0,0,0,0,prop_z,0,0,0,0,1});
    
    tg.setTransform(transform);
    tg.addChild(shape);
    bg.addChild(tg);
    bg.compile();
    return(bg);
  }
  // because MD2ModelInstance's set/get/UserData dont work :-(
  private final class ModelAndBranch
  {
    public MD2ModelInstance model;
    public BranchGroup bgroup;
    public Shape3D nameLabel;
    public Shape3D healthLabel;
  }
  /**
   *
   */
  public static void main(String[] args)
  {
    JFrame frame = new JFrame("Arena");
    The1001Game3D gamedisplay = new The1001Game3D(null);
    
    frame.getContentPane().add(gamedisplay);
    frame.setSize(640,480);
    frame.setUndecorated(true);
    
    List spectators = new ArrayList();
    List fighters   = new ArrayList();
    
    // for (int i = 0; i < spectators.length; i++)
    // {
    // RPObject rp = new RPObject();
    // rp.put("id","spec_"+i);
    // rp.put("name","Spectator_"+i);
    // spectators[i] = rp;
    // }
    // for (int i = 0; i < fighters.length; i++)
    // {
    // try
    // {
    // RPObject rp = new Gladiator(new RPObject.ID(i));
    // rp.put("id","glad_"+i);
    // fighters[i] = rp;
    // }
    // catch (RPObject.SlotAlreadyAddedException e) {}
    // }
    gamedisplay.arena.setSpectators(spectators);
    gamedisplay.arena.setFighters(fighters);
    frame.show();
    //
    try
    {
      Thread.sleep(4000);
    }
    catch (InterruptedException e)
    {
    }
    // gamedisplay.centerText3D.setString("Kein Wurst");
    // RPObject [] lllfighters   = new RPObject[0];
    // System.out.println("removing fighters.....");
    // System.out.flush();
    // gamedisplay.arena.setFighters(lllfighters);
  }
}
