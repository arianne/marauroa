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
        for player in _online_players:
            pos=move(player,self.map)
        
    def onInit(self, object):
        """ Do what you need to initialize this player """
        pos=self.map.getRandomRespawn()
        object.put("x",pos[0])
        object.put("y",pos[1])
        
        zone.add(object)
        _online_players.append(object)
        return 1

    def onExit(self, objectid):
        """ Do what you need to remove this player """
        for x in _online_players:
            if x.get("id")==playerid.getObjectID():
                _online_players.remove(x)
                break
            
        zone.remove(objectid)
        return 1

    def onTimeout(self, objectid):
        return onExit(self,objectid)


class mapacmanRPMap:
    grid=[]
    respawnPoints=[]
    last_respawnPoints=0
    
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
        	for char in line
        		print "Character: "+char
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
_online_players=[]
_player_tracked=[]

def randomDirection():
    return directions[int((rand()/32768)*4)]

#
#  RPObject python creation methods.
#

def createPlayer(name):
    """ This function create a player """
    object=RPObject()
    object.put("id",zone.getValidId())
    object.put("name",name)
    object.put("x",0)
    object.put("y",0)
    object.put("dir",randomDirection())
    object.put("score",0)
    return object;

def createGhost(name):
    """ This function create a ghost """
    object=RPObject()
    object.put("id",zone.getValidId())
    object.put("name",name)
    object.put("x",0)
    object.put("y",0)
    object.put("dir",randomDirection())
    return object;

def createBall(x,y):
    """ This function create a Ball object that when eats by player increments
    its score. """
    object=RPObject()
    object.put("id",zone.getValidId());
    object.put("x",x)
    object.put("y",y)
    object.put("!score",1)
    object.put("!respawn",60)
    return object;

def createSuperBall(x,y):
    """ This function create a SuperBall object that when eats by player
    make it to be able to eat and destroy the ghosts """
    object=createBall(x,y)
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
    
