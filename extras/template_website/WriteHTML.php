<?php
include_once('xml.php');

$depth=array();
$content=array();

function parseXMLFile($file, $startElementFunc, $endElementFunc)
  {
  global $depth, $content;
  
  $depth = array();
  $content= array();

  $xml_parser = xml_parser_create();

  xml_set_element_handler($xml_parser, $startElementFunc, $endElementFunc);

  if(!($fp = fopen($file, "r"))) 
    {
    die("could not open XML input");
    }

  while ($data = fread($fp, 4096)) 
    {
    if (!xml_parse($xml_parser, $data, feof($fp))) 
      {
      die(sprintf("XML error: %s at line %d",
                   xml_error_string(xml_get_error_code($xml_parser)),
                   xml_get_current_line_number($xml_parser)));
      }
    }

  xml_parser_free($xml_parser);
  }

function stats_startElement($parser, $name, $attrs) 
  {
  global $depth, $content;
  
  array_push($depth, $name);
  $content[$name]=$attrs;
  }

function stats_endElement($parser, $name) 
  {
  global $depth, $content;

  array_pop($depth);
  }


function GetServerStatus($xml)
  {
  global $content;
  
  parseXMLFile($xml['serversite'][0]['serverdata'][0]['path'][0],"stats_startElement","stats_endElement");

  if(time()>$content["STATISTICS"]["TIME"]+60)
    {
    return false;
    }
  else
    {
    return true;
    }
  }
  
  
function WriteRenderScreenshots($xml)
	{
	  /*
  foreach($website['website'][0]['menu'][0]['entry'] as $key=>$item)
    {
    if(is_array($item))
      {
      $len=strlen($item['name']);
      echo '<li><a href="'.$item['url'].'">'.$item['name'].'</a></li>';
      }
    }*/
	echo '<div id="screenshots">';
	echo '<h1>Screenshots</h1>';
	echo '<ul>';
  	foreach($xml['serversite'][0]['screenshots'][0]['image'] as $key=>$item)
    {
		echo '<li><img src="'.$item.'" alt="Screenshot"></li>';
	}
	echo '</ul>';
	
	echo '</div>';	
	}

function WriteHeader($xml)
  {  
	  
	echo '<title>'.$xml['serversite']['0 attr']['name'].'</title>';
    echo '<link REL="SHORTCUT ICON" HREF="'.$xml['serversite'][0]['favicon'][0].'">';
    echo '<link rel="stylesheet" type="text/css" href="'.$xml['serversite'][0]['CSSfile'][0].'">';

  }

function WriteTitleSection($xml)
  {
	global $serverStatusString; 
	
	echo '<img class="left" src="'.$xml['serversite'][0]['gamelogo'][0].'" alt="game logo">'; 
	echo '<span class="title">'.$xml['serversite'][0]['title'][0].'</span>';
	//echo '<a href="http://arianne.sourceforge.net"><img class="right" src="http://arianne.sourceforge.net/images/ariannePowered.png" alt="Arianne Powered!"></a>';
	if( $xml['serversite'][0]['serverdata'][0]['serverstate']['0 attr']['enable'] == "1")
	{
		echo '<p>';
		echo (GetServerStatus($xml)? $xml['serversite'][0]['serverdata'][0]['serverstate'][0]['up'][0]: $xml['serversite'][0]['serverdata'][0]['serverstate'][0]['down'][0]); 
		echo '</p>';
	}
  }
  
function WriteNewsBar($xml)
  {
	$news = array();
	$maxNewsItems = $xml['serversite'][0]['newsbar'][0]['maxitems'][0];
	
	$content=implode("",file($xml['serversite'][0]['newsbar'][0]['path'][0]));
	$news = XML_unserialize($content);
	
	echo '<div id="news_bar">';
	   
  echo '<ul class="newslist">';
  
  foreach($news['news'][0]['item'] as $key=>$item)
    {
    if($maxNewsItems==0) break;
    $maxNewsItems--;


    echo '<li><div class="newsitem"><h3>'.$item['title'][0].'</h3><p class="itemdate">'.$item['date'][0].'</p>';

    if(isset($item['images']))
      {
      foreach($item['images'] as $key=>$image) 
        {
        if(is_array($image))
          {
          echo '<img src="'.$image['image']['0 attr']['url'].'" alt="Game screenshot">';
          }
        }
        echo '<div class="newscontent_image">'.$item['content'][0].'</div><div class="clearright">&nbsp;</div></div>';
      }
      else
      {
        echo '<div class="newscontent_noimage">'.$item['content'][0].'</div><div class="clearright">&nbsp;</div></div>';

      }
      echo '</li>';
    }

  echo '</ul>';  
	echo '</div>';
  }
  
function WritePageContent($xml)
 {
	echo '<div class="game_image"><img src="'.$xml['serversite'][0]['description'][0]['image'][0].'" alt="Game Image"></div>'; 
	echo '<div class="description"><h1>What is '.$xml['serversite'][0]['title'][0].'?</h1>'.$xml['serversite'][0]['description'][0]['text'][0].'</div>';  	 
 }

function WriteFooter($xml)
  {
	  if( $xml['serversite'][0]['footer']['0 attr']['enable'] == "1" )
	  	echo $xml['serversite'][0]['footer'][0]['text'][0];
  }
  
function WriteAccountForm($xml)
  {
	echo '<h1>Create an Account for free</h1>';  
    echo '<p>You need an account in order to play '.$xml['serversite'][0]['title'][0];
    echo '. You can create a free account here.</p>';

echo '<form name="accountForm" method="POST" action="">'.
	 '<input type="hidden" name="cmd" value="newaccount">'.
	 '<table>'.
	 '<tr><td><b>Real name</b>: </td><td><input type="text" name="realname" size="60" maxlength="60">';
	 if ($xml['serversite'][0]['help']['0 attr']['enable']  == "1")
	 {
	 	echo '( <a class="help" href="" onClick="return popitup(\''.$xml['serversite'][0]['help'][0].'?id=1\')">?</a> )';
 	 }
echo '</td></tr>';
echo '<tr><td><b>Email</b> <i>(*)</i>: </td><td><input type="text" name="email" size="60" maxlength="60">';
	 if ($xml['serversite'][0]['help']['0 attr']['enable']  == "1")
	 {
	 	echo '( <a class="help" href="" onClick="return popitup(\''.$xml['serversite'][0]['help'][0].'?id=2\')">?</a> )';
 	 }
echo '</td></tr>';
echo '</table>'.
	 '<p>Username is not modifiable once choosen, so choose wisely.</p>'.
	 '<table>'.
	 '<tr><td>Username: </td><td><input type="text" name="username" size="20" maxlength="20">';
	 if ($xml['serversite'][0]['help']['0 attr']['enable']  == "1")
	 {
	 	echo '( <a class="help" href="" onClick="return popitup(\''.$xml['serversite'][0]['help'][0].'?id=3\')">?</a> )';
 	 }
echo '</td></tr>';
echo '<tr><td>Password: </td><td><input type="password" size="20" name="password">';
	 if ($xml['serversite'][0]['help']['0 attr']['enable']  == "1")
	 {
	 	echo '( <a class="help" href="" onClick="return popitup(\''.$xml['serversite'][0]['help'][0].'?id=4\')">?</a> )';
 	 }
echo '</td></tr>';
echo '</table>'.
	 '<p><input type="submit" value="Create account">';
	 if ($xml['serversite'][0]['help']['0 attr']['enable'] == "1")
	 {
	 	echo '( <a class="help" href="" onClick="return popitup(\''.$xml['serversite'][0]['help'][0].'?id=5\')">?</a> )';
	 }
echo '</p></form>';
	 
echo '<p><i>(*)</i>You need to enter a valid email address in case we need to contant you. You can\'t leave the field empty.'.
	 ' <b>Please, limit yourself to one account per person</b>.</p>';
}

function WriteDownloads($xml)
  {
  // section specifies if this is being called from download page or from games page
 echo '<div id="downloads">';
 echo '<h1>Downloads</h1>';
 echo '<p>You can download files related to our project (including clients, servers and documentation) at: <br><a class="download_link" href="';
 echo $xml['serversite'][0]['downloads'][0]['filelocation'][0].'">'.$xml['serversite'][0]['downloads'][0]['filelocation'][0].'</a></p></div>';

  }
  
function getVariable($xmlStats, $type)
  {
  foreach($xmlStats['statistics'][0]['attrib'] as $i=>$j)
    {
    if(is_array($j))
      {
      if($j['name']==$type)
        {
        return $j['value'];
        }
      }
    }
  
  return 0;
  }
  
function WriteTextServerStats($xml)
  {
  $content=implode("",file($xml['serversite'][0]['serverdata'][0]['path'][0]));
  $xmlStats = XML_unserialize($content);

  echo '<div id="textstat"><h1>Detailed statistics</h1>';
  echo '<p class="uptime">This server uptime is '.$xmlStats['statistics'][0]['uptime']['0 attr']['value'].' seconds</p>';

  echo '<table>';
  echo '<tr class="maintitle"><td align="center" colspan="4"><b>Bytes managed</b></td></tr>';
  echo '<tr class="subtitle"><td colspan="2">Recieved</td><td colspan="2">Send</td></tr>';
  echo '<tr class="data"><td colspan="2">'.getVariable($xmlStats,"Bytes recv").'</td><td colspan="2">'.getVariable($xmlStats,"Bytes send").'</td></tr>';

  echo '<tr><td>&nbsp;</td></tr>';
  echo '<tr class="maintitle"><td align="center" colspan="4"><b>Messages managed</b></td></tr>';
  echo '<tr class="subtitle"><td>Recieved</td><td>Send</td><td>Invalid version</td><td>Incorrect</td></tr>';
  echo '<tr class="data"><td>'.getVariable($xmlStats,"Message recv").'</td><td>'.getVariable($xmlStats,"Message send").'</td><td>'.getVariable($xmlStats,"Message invalid version").'</td><td>'.getVariable($xmlStats,"Message incorrect").'</td></tr>';

  echo '<tr><td>&nbsp;</td></tr>';
  echo '<tr class="maintitle"><td align="center" colspan="4"><b>Players managed</b></td></tr>';
  echo '<tr class="subtitle"><td>Logins</td><td>Logins failed</td><td>Logouts</td><td>Timeouts</td></tr>';
  echo '<tr class="data"><td>'.getVariable($xmlStats,"Players login").'</td><td>'.getVariable($xmlStats,"Players invalid login").'</td><td>'.getVariable($xmlStats,"Players logout").'</td><td>'.getVariable($xmlStats,"Players timeout").'</td></tr>';

  echo '<tr><td>&nbsp;</td></tr>';
  echo '<tr class="maintitle"><td align="center" colspan="4"><b>Actions managed</b></td></tr>';
  echo '<tr class="subtitle"><td>Managed</td><td>Chat</td><td>Move</td><td>Attack</td></tr>';
  echo '<tr class="data"><td>'.getVariable($xmlStats,"Actions added").'</td><td>'.getVariable($xmlStats,"Actions chat").'</td><td>'.getVariable($xmlStats,"Actions move").'</td><td>'.getVariable($xmlStats,"Actions attack").'</td></tr>';

  echo '</table></div>';

  echo '<div id="textstat"><h1>Killed statistics</h1>';
  echo '<table>';
  echo '<tr class="maintitle"><td align="center" colspan="4"><b>Creatures killed</b></td></tr>';
  echo '<tr class="subtitle"><td colspan="2">Creature</td><td colspan="2">Amount</td></tr>';
  $creatures=array("sheep","rat","caverat","orc");
  foreach($creatures as $creature)
    {    
    $amount=getVariable($xmlStats,"Killed ".$creature);
    echo '<tr class="data"><td colspan="2">'.$creature.'</td><td colspan="2">'.$amount.'</td></tr>';
    }
  echo '</table></div>';
  }
  
function WriteGraphicServerStats($xml)
  {
  echo '<div id="graphicstat"><h1>Last 24 hours graphical representation</h1>';
  echo '<table>';
  echo '  <tr>';
  echo '    <td>KBytes send per hour during the last 24 hours<br><img src="query/BS_3600.png" alt="Statistic"></td>';
  echo '    <td>KBytes recieved hour during the last 24 hours<br><img src="query/BR_3600.png" alt="Statistic"></td>';
  echo '  </tr>';
  echo '  <tr>';
  echo '    <td>Player Online per hour during the last 24 hours<br><img src="query/PO_60.png" alt="Statistic"></td>';
  echo '    <td>Daily accounts created during last 15 days<br><img src="query/AC_24_7.png" alt="Statistic"></td>';
  echo '  </tr>';
  echo '</table></div>';
	  
  }
 
?>