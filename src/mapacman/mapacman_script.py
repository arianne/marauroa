from marauroa.game import *
from mapacman import *
#from mapacman_script import *

pacman_mapfile='map_definition.txt'

#
# PythonRP Interface for Java classes
# * We need it because calling functions is VERY slow
#

class RealPythonRP(PythonRP):
    map
    removed_elements=[]
    super_players=[]
    online_players=[]
    
    def __init__(self):
        self.map=mapacmanRPMap(pacman_mapfile)

    def execute(self, id, action):
        """ called to execute actions from player identified by id that wants
        to do action action """
        action_code=action.get("type")
        result=0
    
        if action_code=="turn":
            direction=action.get("dir")
            if _directions.count(direction)==1:
                player=zone.get(id)
                player.put("dir",direction)
                result=1
                zone.modify(player)
        elif action_code=="chat":
            content=action.get("content")
            player=zone.get(id)
            player.put("?text",content)
            zone.modify(player)
            result=1
        else:
            print "action not registered"
    
        return result
    
    def nextTurn(self):
        """ execute actions needed to place this code on the next turn """
        for player in self.online_players:
            pos=move(player,self.map)
            if self.map.hasZoneRPObject(pos):
                object_in_pos=self.map.getZoneRPObject(pos)
                if object_in_pos.get("type")=="ball":
                    zone.remove(RPObject.ID(object_in_pos))
                    self.map.removeZoneRPObject(pos)
                    element=RemovedElement(object_in_pos.getInt("!respawn"),object_in_pos)
                    self.removed_elements.append(element)
                    player.put("score",player.getInt("score")+1)
                    zone.modify(player)
                elif object_in_pos.get("type")=="superball":
                    zone.remove(RPObject.ID(object_in_pos))
                    self.map.removeZoneRPObject(pos)
                    element=RemovedElement(object_in_pos.getInt("!respawn"),object_in_pos)
                    self.removed_elements.append(element)
                    timeout=object_in_pos.getInt("!timeout")
                    player.put("super",timeout)
                    element=RemovedElement(timeout,player)
                    self.super_players.append(element)
                    zone.modify(player)

            for player_in_pos in self.getPlayers(pos):
                if player_in_pos.get("type")=="ghost":
                    if player.has("super"):
                        # Eat the ghost
                        pass
                    else:
                        # kill the player
                        pass
        
        for object in self.removed_elements:
            if object.timeout==0:
                self.map.addZoneRPObject(object.object)
            else:
                object.timeout=object.timeout-1

        for object in self.super_players:
            if object.timeout==0:
                object.object.remove("super")
            else:
                object.timeout=object.timeout-1
                object.put("super",object.timeout)
                zone.modify(object)

    def getPlayers(self,pos):
        list=[]
        for player in self.online_players:
            if pos[0]==player.getInt("x") and pos[1]==player.getInt("y"):
                list.append(player)
        return list
        
    def onInit(self, object):
        """ Do what you need to initialize this player """
        pos=self.map.getRandomRespawn()
        object.put("x",pos[0])
        object.put("y",pos[1])
        
        zone.add(object)
        self.online_players.append(object)
        return 1

    def onExit(self, objectid):
        """ Do what you need to remove this player """
        for x in _online_players:
            if x.get("id")==playerid.getObjectID():
                self.online_players.remove(x)
                break
            
        zone.remove(objectid)
        return 1

    def onTimeout(self, objectid):
        return onExit(self,objectid)

class RemovedElement:
    timeout=0
    object=0
    
    def __init__(self,timeout,object):
        self.timeout=timeout
        self.object=object
    
class mapacmanRPMap:
    grid=[]
    respawnPoints=[]
    last_respawnPoints=0
    objects_grid={}
    
    def __init__(self, sizex, sizey):
        self.grid=[]
        for i in range(sizex):
            grid_line=''
            for j in range(sizey):
                grid_line=grid_line+' '
            self.grid.append(grid_line)
    
    def __init__(self, filename):
        f=open(filename,'r')
        line=f.readline()
        i=0
        while line<>'':
            j=0
            for char in line[:-1]:
                if char=='.':
                    self.addZoneRPObject(createBall(j,i))
                elif char=='0':
                    self.addZoneRPObject(createSuperBall(j,i))
                j=j+1
                
            self.grid.append(line[:-1])
            self.computeRespawnPoints(i,line)
            line=f.readline()
            i=i+1
        print self.respawnPoints
    
    def computeRespawnPoints(self,y,line):
        i=line.find('+')
        while i<>-1:
            pos=(i,y)
            self.respawnPoints.append(pos)
            i=line.find('+',i+1)
    
    def get(self,x,y):
        return (self.grid[y])[x]
    
    def hasZoneRPObject(self, pos):
        return self.objects_grid.has_key(pos)
    
    def getZoneRPObject(self,pos):
        return self.objects_grid[pos]
    
    def addZoneRPObject(self,object):
        x=object.getInt("x")
        y=object.getInt("y")
        
        self.objects_grid[(x,y)]=object
        zone.add(object)
    
    def removeZoneRPObject(self,pos):
        del self.objects_grid[pos]

    def sizey(self):
        return len(self.grid)
    
    def sizex(self):
        return len(self.grid[0])
    
    def getRandomRespawn(self):
        self.last_respawnPoints=(self.last_respawnPoints+1)%(len(self.respawnPoints))
        return self.respawnPoints[self.last_respawnPoints]
        

#
#
#
SUCCESS=0
FAILED=1

_directions=['N','W','S','E']

def randomDirection():
    return directions[int((rand()/32768)*4)]

#
#  RPObject python creation methods.
#

def createPlayer(name):
    """ This function create a player """
    object=RPObject()
    object.put("id",zone.create().get("id"))
    object.put("type","player");
    object.put("name",name)
    object.put("x",0)
    object.put("y",0)
    object.put("dir",randomDirection())
    object.put("score",0)
    return object;

def createGhost(name):
    """ This function create a ghost """
    object=RPObject()
    object.put("id",zone.create().get("id"))
    object.put("type","ghost");
    object.put("name",name)
    object.put("x",0)
    object.put("y",0)
    object.put("dir",randomDirection())
    return object;

def createBall(x,y):
    """ This function create a Ball object that when eats by player increments
    its score. """
    object=RPObject()
    object.put("id",zone.create().get("id"));
    object.put("type","ball");
    object.put("x",x)
    object.put("y",y)
    object.put("!score",1)
    object.put("!respawn",60)
    return object;

def createSuperBall(x,y):
    """ This function create a SuperBall object that when eats by player
    make it to be able to eat and destroy the ghosts """
    object=createBall(x,y)
    object.put("type","superball");
    object.put("!timeout",15)
    return object;
    

#
# RPRuleProcessor method definitions
#

def move(player,map):
    """ This methods try to move the player and return the new position """
    x=player.getInt("x")
    y=player.getInt("y")
    dir=player.get("dir")
    
    if dir=='N' and (y-1)>=0 and map.get(x,y-1)<>'*':
        y=y-1
        player.put("y",y)
    elif dir=='W' and (x-1)>=0 and map.get(x-1,y)<>'*':
        x=x-1
        player.put("x",x)
    elif dir=='S' and (y+1)<map.sizex() and map.get(x,y+1)<>'*':
        y=y+1
        player.put("y",y)
    elif dir=='E' and (x+1)<map.sizey() and map.get(x+1,y)<>'*':
        x=x+1
        player.put("x",x)
        
    return (x,y)
    

#
# Test the whole thing
#
    
if __name__=='__main__':
    """ Test case here """
    pass
    
