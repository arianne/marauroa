from marauroa.game.python import *
from marauroa.game import *
from marauroa.net import *

from java.util import LinkedList

import random

pacman_mapfile='map_definition.txt'

#
# A few constants to make things more beautiful
#
success=1
failed=0


_directions=['N','W','S','E']

def randomDirection():
    return _directions[random.randint(0,3)]

#
# PythonRP Interface for Java classes
#

class RealPythonZone(PythonZone):
    def __init__(self, zone):
        self._zone=zone

    def onInit(self):
        return 1

    def onFinish(self):
        return 1


def getPythonAI():
    return variable_PythonAI

def setPythonAI(pythonAI):
    global variable_PythonAI
    variable_PythonAI=None
    
    if variable_PythonAI is None:
        variable_PythonAI=pythonAI    

class RealPythonAI(PythonAI):
    def __init__(self, zone, sched):
        self._zone=zone
        self._sched=sched
        self.pythonRP=None
        self.ghosts=[]
        
        setPythonAI(self)
        
    def setPythonRP(self, pythonRP):
        self.pythonRP=pythonRP
     
    def createEnviroment(self):
        ghost=self.pythonRP.createGhost('Sticky')
        self.pythonRP.onInitAIGhost(ghost)
        self.ghosts.append(ghost)
        
    def compute(self,timelimit):
        # TODO: Code here the ghost AI
        # Personally I would use the PythonRP directly and move the ghost at will
        # But if you are really in need, add actions to the scheduler so they are
        # done as if you are a simple player.
        if len(self.pythonRP._online_players)==0:
            return 1
            
        for ghost in self.ghosts:
            ghost.add("!decision",-1)
            
            target=None
            if not ghost.has("!target"):
                players=self.pythonRP._online_players
                target=players[random.randint(0,len(players)-1)]
                ghost.put("!target",target.get("id"))
            else:
                try:
                    target=self.pythonRP._zone.get(RPObject.ID(ghost.getInt("!target")))
                except:
                    ghost.remove("!target")
            
            if target is not None and ghost.getInt("!decision")<=0:
                ghost.put("!decision",5)
                difx=target.getInt("x")-ghost.getInt("x")
                dify=target.getInt("y")-ghost.getInt("y")
                
                if difx>0:
                    ghost.put("!hdir","E")
                elif difx<0:
                    ghost.put("!hdir","W")
                    
                if dify>0:
                    ghost.put("!vdir","S")
                elif dify<0:
                    ghost.put("!vdir","N")
                
            if self.pythonRP.canMove(ghost)==0:
                dir=randomDirection()
                ghost.put("dir",dir)
            else:            
                self.pythonRP.move(ghost)
                self.pythonRP._zone.modify(ghost)

        for player in self.pythonRP._online_players:
            self.pythonRP._ghostCollisions(player)
                
        return 1
    
class RealPythonRP(PythonRP):
    def __init__(self,zone):
        self._removed_elements=[]
        self._super_players=[]
        self._online_players=[]
        self._online_ghosts=[]
        self._killedFlagGhosts=[]

        self._zone=zone
        self._map=mapacmanRPMap(self,pacman_mapfile)
        self._serializedMap=None

        instance=getPythonAI()
        instance.setPythonRP(self)
        instance.createEnviroment()
        
    def getZone(self):
        return self._zone

    def execute(self, id, action):
        """ called to execute actions from player identified by id that wants to do action action """      
        result=0
        player=self._zone.get(id)
        action_code=action.get("type")

        if action_code=="turn":
            result=self.turn(player,action.get("dir"))
        elif action_code=="chat":
            result=self.chat(player,action.get("content"))
        else:
            print "action not registered"
        
        return result
        
    def move(self, player):
        """ This methods try to move the player and return the new position """
        x=player.getInt("x")
        y=player.getInt("y")
        dir=player.get("dir")
    
        if dir=='N' and (y-1)>=0 and self._map.get(x,y-1)<>'*':
            y=y-1
            player.put("y",y)
        elif dir=='W' and (x-1)>=0 and self._map.get(x-1,y)<>'*':
            x=x-1
            player.put("x",x)
        elif dir=='S' and (y+1)<self._map.sizey() and self._map.get(x,y+1)<>'*':
            y=y+1
            player.put("y",y)
        elif dir=='E' and (x+1)<self._map.sizex() and self._map.get(x+1,y)<>'*':
            x=x+1
            player.put("x",x)
            
        return (x,y)

    def canMove(self, player):
        dir=player.get("dir")
        if (dir=='E' or dir=='W') and player.has("!vdir"):
            vdir=player.get("!vdir")
            if self._canMove(player,vdir):
                player.put("dir",vdir)
                player.remove("!vdir")

        if (dir=='N' or dir=='S') and player.has("!hdir"):
            hdir=player.get("!hdir")
            if self._canMove(player,hdir):
                player.put("dir",hdir)
                player.remove("!hdir")
        
        return self._canMove(player,player.get("dir"))
            
    def _canMove(self, player, dir):
        """ This methods try to move the player and return the new position """
        x=player.getInt("x")
        y=player.getInt("y")
  
        if dir=='N' and (y-1)>=0 and self._map.get(x,y-1)<>'*':
            return 1
        elif dir=='W' and (x-1)>=0 and self._map.get(x-1,y)<>'*':
            return 1
        elif dir=='S' and (y+1)<self._map.sizey() and self._map.get(x,y+1)<>'*':
            return 1
        elif dir=='E' and (x+1)<self._map.sizex() and self._map.get(x+1,y)<>'*':
            return 1
        else:
            return 0
    
    def turn(self, player, direction):
        result=failed
        directionsH=['E','W']
        directionsV=['N','S']
        
        if _directions.count(direction)==1 and not self._canMove(player,direction):
            if directionsH.count(direction)==1:
                player.put("!hdir",direction)
        
            if directionsV.count(direction)==1:
                player.put("!vdir",direction)

        if _directions.count(direction)==1 and self._canMove(player,direction):
            player.put("dir",direction)
            self._zone.modify(player)
            result=success
        return result

    def chat(self, player, content):
        player.put("?text",content)
        self._zone.modify(player)
        return success

    def _removeBall(self, ball, pos):
        self._zone.remove(ball.getID())
        self._map.removeZoneRPObject(pos)
        element={'timeout':ball.getInt("!respawn"),'object':ball}
        self._removed_elements.append(element)
    
    def _addBall(self, element):
        self._map.addZoneRPObject(element['object'])
        self._removed_elements.remove(element)
    
    def _movePlayer(self,player):
        pos=self.move(player)
        self._zone.modify(player)
        
        if self._map.hasZoneRPObject(pos):
            object_in_pos=self._map.getZoneRPObject(pos)
            if object_in_pos.get("type")=="ball":
                self._removeBall(object_in_pos,pos)
                    
                # Increment the score of the player
                player.add("score",1)
            elif object_in_pos.get("type")=="superball":
                self._removeBall(object_in_pos,pos)
                    
                # Notify to remove the attribute on timeout
                timeout=object_in_pos.getInt("!timeout")
                player.put("super",timeout)
                element={'timeout':timeout,'object':player}
                self._super_players.append(element)
    
    def removeKilledFlag(self):
        for object in self._killedFlagGhosts:
            if object.has("?kill"):
                object.remove("?kill")
                self._zone.modify(object)
        

    def _ghostCollisions(self, player):
        pos=(player.getInt("x"),player.getInt("y"))
        for ghost in self.getGhosts(pos):
            if player.has("super"):
                # TODO: Eat the ghost
                pass
            else:
                # TODO: kill the player
                print "Ghost killed player ",player.get("id") 
                ghost.add("score",1)
                ghost.put("?kill","")
                if ghost.has("!target"): ghost.remove("!target")
                self._killedFlagGhosts.append(ghost)
                self._zone.modify(ghost)
                
                pos=self._map.getRandomRespawn()
                player.put("x",pos[0])
                player.put("y",pos[1])
                player.put("score",player.getInt("score")/2)
                self._zone.modify(player)
    
    def _foreachPlayer(self):
        for player in self._online_players:
            if(self.canMove(player)):
                self._movePlayer(player)
                self._ghostCollisions(player)
            else:
                pass
    
    def nextTurn(self):
        """ execute actions needed to place this code on the next turn """
        self.removeKilledFlag()
        
        for object in self._removed_elements:
            if object['timeout']==0:
                self._addBall(object)
            else:
                object['timeout']=object['timeout']-1

        for object in self._super_players:
            if object['timeout']==0:
                object['object'].remove("super")
                self._super_players.remove(object)
            else:
                object['timeout']=object['timeout']-1
                object['object'].put("super",object['timeout'])

            self._zone.modify(object['object'])

        self._foreachPlayer()
        

    def getGhosts(self,pos):
        list=[]
        for ghost in self._online_ghosts:
            if pos[0]==ghost.getInt("x") and pos[1]==ghost.getInt("y"):
                list.append(ghost)
        return list
        
    def onInit(self, object):
        """ Do what you need to initialize this player """
        pos=self._map.getRandomRespawn()
        object.put("x",pos[0])
        object.put("y",pos[1])
        
        self._zone.add(object)
        self._online_players.append(object)
        return 1

    def onInitAIGhost(self, object):
        """ Do what you need to initialize this AI """
        pos=self._map.getRandomRespawn()
        object.put("x",pos[0])
        object.put("y",pos[1])
        
        self._zone.add(object)
        self._online_ghosts.append(object)
        return 1

    def onExit(self, objectid):
        """ Do what you need to remove this player """
        for x in self._online_players:
            if x.getInt("id")==objectid.getObjectID():
                self._online_players.remove(x)
                break
            
        self._zone.remove(objectid)
        return 1

    def onTimeout(self, objectid):
        return self.onExit(objectid)
    
    def buildMapObjectsList(self):
        if self._serializedMap is None:
            self._serializedMap=self._map.serializeMap()

        return self._serializedMap


    def createPlayer(self, name):
        """ This function create a player """
        object=self._zone.create()
        object.put("type","player");
        object.put("name",name)
        object.put("x",0)
        object.put("y",0)
        object.put("dir",randomDirection())
        object.put("score",0)
        return object;

    lastNonStorableId=1;
    
    def createGhost(self, name):
        """ This function create a ghost """
        object=RPObject()
        object.put("id",RealPythonRP.lastNonStorableId)
        RealPythonRP.lastNonStorableId=RealPythonRP.lastNonStorableId+1        
        object.put("type","ghost");
        object.put("name",name)
        object.put("x",0)
        object.put("y",0)
        object.put("dir",randomDirection())
        object.put("score",0)
        object.put("!decision",10)
        return object;

    def createBall(self, x,y):
        """ This function create a Ball object that when eats by player increments
        its score. """
        object=RPObject()
        object.put("id",RealPythonRP.lastNonStorableId)
        RealPythonRP.lastNonStorableId=RealPythonRP.lastNonStorableId+1        
        object.put("type","ball");
        object.put("x",x)
        object.put("y",y)
        object.put("!score",1)
        object.put("!respawn",60)
        return object;

    def createSuperBall(self, x,y):
        """ This function create a SuperBall object that when eats by player
        make it to be able to eat and destroy the ghosts """
        object=self.createBall(x,y)
        object.put("type","superball");
        object.put("!timeout",15)
        return object;

    
class mapacmanRPMap:
    def __init__(self, pythonRP, sizex, sizey):
        self._respawnPoints=[]
        self._last_respawnPoints=0
        self._objects_grid={}
        
        self._grid=[]
        self._zone=pythonRP.getZone()
        for i in range(sizex):
            grid_line=''
            for j in range(sizey):
                grid_line=grid_line+' '
            self._grid.append(grid_line)
    
    def __init__(self, pythonRP, filename):
        f=open(filename,'r')
        line=f.readline()
        
        self._respawnPoints=[]
        self._last_respawnPoints=0
        self._objects_grid={}
        
        self._grid=[]
        self._zone=pythonRP.getZone()
        
        i=0
        while line<>'':
            j=0
            for char in line[:-1]:
                if char=='.':
                    self.addZoneRPObject(pythonRP.createBall(j,i))
                elif char=='0':
                    self.addZoneRPObject(pythonRP.createSuperBall(j,i))
                j=j+1
                
            self._grid.append(line[:-1])
            self.computeRespawnPoints(i,line[:-1])
            line=f.readline()
            i=i+1
    
    def computeRespawnPoints(self,y,line):
        i=line.find('+')
        while i<>-1:
            pos=(i,y)
            self._respawnPoints.append(pos)
            i=line.find('+',i+1)
    
    def get(self,x,y):
        return (self._grid[y])[x]
    
    def hasZoneRPObject(self, pos):
        return self._objects_grid.has_key(pos)
    
    def getZoneRPObject(self,pos):
        return self._objects_grid[pos]
    
    def addZoneRPObject(self,object):
        x=object.getInt("x")
        y=object.getInt("y")
        
        self._objects_grid[(x,y)]=object
        self._zone.add(object)
    
    def removeZoneRPObject(self,pos):
        del self._objects_grid[pos]

    def sizey(self):
        return len(self._grid)
    
    def sizex(self):
        return len(self._grid[0])
    
    def getRandomRespawn(self):
        self._last_respawnPoints=(self._last_respawnPoints+1)%(len(self._respawnPoints))
        return self._respawnPoints[self._last_respawnPoints]
    
    def serializeMap(self):
        def createBlock(pos):
            object=RPObject()
            object.put("x",pos[0])
            object.put("y",pos[1])
            object.put("type","block")
            return object
            
        listObjects=LinkedList()
        y=0
        for line in self._grid:
            x=0
            for char in line:
                if char=='*':
                    listObjects.add(createBlock((x,y)))
                x=x+1
            y=y+1

        return listObjects
