/**
 * This package stores all the important data structures of Marauroa.
 * <p>
 * Mainly we are talking about: <ul>
 * <li>Attributes
 * <li>RPObject
 * <li>RPSlot
 * <li>RPAction
 * <li>RPEvent
 * <li>RPClass
 * <li>Definitions
 * <li>IRPZone
 * </ul>
 *
 * The whole Marauroa system is managed by two main entities, RPAction and RPObject. 
 * There are also several helper classes like Attributes, RPSlot, RPEvent and RPClass
 * <p>
 * Atributtes<br>
 * Attributes is a collection of pairs of values in the form name-value.<br> 
 * We can store almost any basic type in a Attribute object:<ul>
 * <li>strings
 * <li>integer
 * <li>floats
 * <li>boolean
 * </ul>
 * 
 * We can't store structures in the attribute, but you can convert groups of data to a 
 * string and store it as a string. Marauroa provides helper methods for this.
 * <p>
 * Objects<br>
 * All information in the Marauroa server is contained in RPObjects. 
 * An object is composed of several attributes (an attribute is similar to a variable in 
 * that it has a name and contains a value) and Slots. A Slot is a container or array of 
 * containers belonging to an object, and are used to host (store) other objects inside them.
 * 
 * <b>Mandatory Object Attributes are id, type and zoneid</b>
 * id is an unique identification for the object inside a zone.<br> 
 * zoneid is the identification for the zone, unique inside world, where the object resides<br>
 * type is the type of the object aka class, so that you can share attributes definitions for 
 * all the instances of the class.
 * <p>
 * An id is only unique inside the zone which contains that object.
 * <p>
 * The engine provided two special types of attributes when you set no RPClass for a Attributes object:<ul>
 * <li>Attributes that begin with ! are completely hidden from all other users except 
 * the owner of the object. 
 * <li>Attributes that begin with # are completely hidden for all users.
 * </ul>
 * <p>
 * Classes of Objects Explained<br>
 * Classes of Objects are the basic way of structuring Marauroa data structures. 
 * The type of an attribute of a given object must be equal to a RPClass name of the type 
 * class you wish to use.<br> 
 * The class defines the type of the attribute, its visibility and assigns it an internal 
 * code that is used to speed up searchs and save bandwidth. You can base a class on another,
 * this feature is known as inheritance (a new class is create from a class that already 
 * exists and takes all the original classes methods and data and extends it).
 * <p>
 * The data types available are:<ul>
 * <li>Strings
 * <li>Short strings ( up to 255 bytes )
 * <li>Integers ( 4 bytes )
 * <li>Shorts ( 2 bytes )
 * <li>Byte ( 1 byte )
 * <li>Flag ( it is a binary attribute )
 * </ul>
 * Attributes can be private which means only the player client can see them when they change, or invisible if 
 * clients can't see them at all. Also an attribute can be volatile which means that it is not
 * stored to database.
 * <p>
 * Slots<br>
 * Objects can reside inside other objects much like you have the keys inside your pocket. 
 * The goal of Slots is to provide a richer game play while reducing the number of object in
 * the zone.<br>
 * To have objects inside, we need our hoster object to have slots to place them in.
 * <p>
 * For example an avatar can have:<ul>
 * <li>left hand
 * <li>right hand
 * <li>backpack
 * <li>left pocket
 * <li>right pocket
 * </ul>
 * And we can store objects in each of these slots.
 * <p>
 * Once an object has been stored inside an objects slot, the only way of accessing the 
 * stored object is through the object that contains our stored object.
 * 
 * As attributes, slots, that has not been defined in a RPClass, have two special types:<ul>
 * <li>Slots names that start with ! are only sent to owner player. (Hence only seen by the owner)
 * <li>Slots names that start with # are not sent to players. (Invisible to all)
 * </ul>
 *
 * Actions<br>
 * To express the willingness of a client to do something it must send the server a 
 * MessageC2SAction message.
 * An action is composed of several attributes. <i>(an attribute is similar to a variable in that it has a name and contains a value)</i>.
 * There are optional and mandatory attributes. If a mandatory attribute is not found, 
 * the message is skipped by the RPServerManager.
 * <p>
 * Mandatory Action Attributes are action_id and type.
 * The action_id is used to identify the action when a resulting response comes in a 
 * perception
 * 
 * @author miguel 
 */
package marauroa.common.game;

