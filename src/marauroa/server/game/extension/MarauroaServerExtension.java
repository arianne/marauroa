package marauroa.server.game.extension;

import java.util.HashMap;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.game.Perception;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;

public abstract class MarauroaServerExtension {
    /** the logger instance. */
    private static final Logger logger = Log4J.getLogger(MarauroaServerExtension.class);
    /** Lists the instances of the loaded extensions. */
    private static Map<String, MarauroaServerExtension> loadedInstances = new HashMap<String, MarauroaServerExtension>();

    /**
     * @return the loadedInstances
     */
    public static Map<String, MarauroaServerExtension> getLoadedInstances() {
        return loadedInstances;
    }

    public abstract void init();

    public synchronized boolean perform(String name) {
        return (false);
    }

    public String getMessage(String name) {
        return (null);
    }

    public static MarauroaServerExtension getInstance(String name) {
        try {
            Class<?> extensionClass = Class.forName(name);

            if (!MarauroaServerExtension.class.isAssignableFrom(extensionClass)) {
                logger.debug("Class is no instance of SimpleServerExtension.");
                return null;
            }

            logger.debug("Loading ServerExtension: " + name);
            java.lang.reflect.Constructor<?> constr = extensionClass.getConstructor();

            // simply create a new instance. The constructor creates all
            // additionally objects
            MarauroaServerExtension instance = (MarauroaServerExtension) constr.newInstance();
            // store it in the hashmap for later reference
            getLoadedInstances().put(name, instance);
            logger.debug("Done!");
            return instance;
        } catch (Exception e) {
            logger.warn("SimpleServerExtension " + name + " loading failed.", e);
            return null;
        }
    }

    /**
     * Action to perform
     * 
     * @param player Player requesting the action
     * @param action Action details
     */
    public abstract void onAction(RPObject player, RPAction action);
    
    /**
     * Query the extension to plug in any action when an object is added to a zone.
     * 
     * @param object Added object
     * @return potentially modified object
     */
    public RPObject onRPObjectAddToZone(RPObject object){
        return object;
    }
    
    /**
     * Query the extension to plug in any action when an object is removed from a zone.
     * 
     * @param object Removed object
     * @return potentially modified object
     */
    public RPObject onRPObjectRemoveFromZone(RPObject object){
        return object;
    }
    
    /**
     * Query the extension to plug in any changes to the perception of an object.
     * 
     * @param object Object to potentially modify the perception
     * @param type 
     */
    public void getPerception(RPObject object, byte type, Perception p){
        //Do nothing by default
    }
    
    /**
     * Plug into the definition of the client class
     * @param client 
     */
    public void modifyClientObjectDefinition(RPClass client){
        
    }
    
    /**
     * Action to perform after the world is initialized (all classes are defined)
     */
    public void afterWorldInit(){
        
    }
    
    /**
     * Update the database. Register/Update DAO's here as well
     */
    public void updateDatabase(){
        
    }
}