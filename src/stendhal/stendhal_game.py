#
# Stendhal - Arianne multiplayer adventures game -
#
# It is a game with a RPG style gameplay.
# The world as of version 0.01 is made of several zones:
# - Village
# - Field around the village
# - Forest
#
# Players can play on the world by:
# - walking around
# - talking with NPC for selling, buying and quests
# - handle a backpack
# - attack some attackable NPC
# - grab some grabable objects.

from marauroa.game.python import *
from marauroa.game import *
from marauroa.net import *
from marauroa import *

import java.util.LinkedList

class StendhalRP(PythonRP):
    def __init__(self, world):
        self.world=world

    def execute(self, id, action):
        return 0

    def nextTurn(self):
        pass

    def onInit(self, object):
        object.put("zoneid","village");
        print self.world;
        self.world.add(object)
        return 1

    def onExit(self, objectid):
        return 0

    def onTimeout(self, objectid):
        return 0


class StendhalWorld(PythonWorld):
    def __init__(self, world):
        self.world=world
        self.createRPClasses()

    def createRPClasses(self):
        STRING=RPClass.STRING
        SHORT_STRING=RPClass.STRING
        INT=RPClass.INT
        SHORT=RPClass.SHORT
        BYTE=RPClass.BYTE
        FLAG=RPClass.FLAG
        
        HIDDEN=RPClass.HIDDEN

        objclass=RPClass("position")
        objclass.add("x",BYTE)
        objclass.add("y",BYTE)
        
        objclass=RPClass("character")
        objclass.isA("position")
        objclass.add("name",SHORT_STRING)
        objclass.add("xp",INT)
        objclass.add("hp",BYTE)
        objclass.add("atk",BYTE)
        objclass.add("def",BYTE)
        objclass.add("zoneid",SHORT_STRING,HIDDEN)
      
    def createRPWorld(self):
        village=marauroa.game.MarauroaRPZone("village")
        self.world.addRPZone(village)
        
        
    
    def onInit(self):
        pass
        
    def onFinish(self):
        pass
