## TODO: There are extra helper methods that will surely make your life easier.
## You either wait me to place them here or you take a look to mapacman

from marauroa.game.python import *

class TemplateGamePythonRP(PythonRP):
    def __init__(self, zone):
        self._zone=zone

    def execute(self, id, action):
        pass

    def nextTurn(self):
        pass

    def onInit(self, object):
        pass

    def onExit(self, objectid):
        pass
    
    def onTimeout(self, objectid):
        pass

class TemplateZonePythonRP(PythonZoneRP):
    def __init__(self, zone):
        self._zone=zone

    def onInit():
        pass

    def onFinish():
        pass

    def serializeMap(self, objectid):
        pass

class TemplateAIPythonRP(PythonAIRP):
    def __init__(self, rpManager):
        self._rpManager=rpManager

    def onCompute(timelimit):
        pass
    
    
    
        
