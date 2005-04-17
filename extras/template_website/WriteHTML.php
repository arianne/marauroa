<?php
include_once('xml.php');

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
		echo '<li><img src="'.$item.'"></li>';
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
    if(!$all) $maxNewsItems--;


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
	echo '<div class="description"><h1>What is '.$xml['serversite'][0]['title'][0].'?</h1>'.$xml['serversite'][0]['description'][0]['text'][0]; 
	echo '<div class="game_image"><img src="'.$xml['serversite'][0]['description'][0]['image'][0].'"></div></div>'; 	 	 
 }

function WriteFooter($xml)
  {
	  if( $xml['serversite'][0]['footer']['0 attr']['enable'] == "1" )
	  	echo '<p>'.$xml['serversite'][0]['footer'][0]['text'][0].'</p>';
  }
  
function WriteAccountForm($xml)
  {
	echo '<h1>Create an Account for free</h1>';  
    echo 'You need an account in order to play '.$xml['serversite'][0]['title'][0];
    echo '. You can create a free account here.';

echo '<form name="accountForm" method="POST" action="">'.
	 '<input type="hidden" name="cmd" value="newaccount"/>'.
	 '<table>'.
	 '<tr><td>Real name: </td><td><input type="text" name="realname" size="60" maxlength="60"/>';
	 if ($xml['serversite'][0]['help']['0 attr']['enable']  == "1")
	 {
	 	echo '( <a class="help" href="" onClick="return popitup(\''.$xml['serversite'][0]['help'][0].'?id=1\')">?</a> )</td></tr>';
 	 }
echo '<tr><td>Email <i>(*)</i>: </td><td><input type="text" name="email" size="60" maxlength="60"/>';
	 if ($xml['serversite'][0]['help']['0 attr']['enable']  == "1")
	 {
	 	echo '( <a class="help" href="" onClick="return popitup(\''.$xml['serversite'][0]['help'][0].'?id=2\')">?</a> )</td></tr>';
 	 }
echo '</table>'.
	 '<p>Username is not modifiable once choosen, so choose wisely.<br>'.
	 '<table>'.
	 '<tr><td>Username: </td><td><input type="text" name="username" size="20" maxlength="20"/>';
	 if ($xml['serversite'][0]['help']['0 attr']['enable']  == "1")
	 {
	 	echo '( <a class="help" href="" onClick="return popitup(\''.$xml['serversite'][0]['help'][0].'?id=3\')">?</a> )</td></tr>';
 	 }
echo '<tr><td>Password: </td><td><input type="password" size="20" name="password"/>';
	 if ($xml['serversite'][0]['help']['0 attr']['enable']  == "1")
	 {
	 	echo '( <a class="help" href="" onClick="return popitup(\''.$xml['serversite'][0]['help'][0].'?id=4\')">?</a> )</td></tr>';
 	 }
echo '</table>'.
	 '<p><input type="submit" value="Create account"/>';
	 if ($xml['serversite'][0]['help']['0 attr']['enable'] == "1")
	 {
	 	echo '( <a class="help" href="" onClick="return popitup(\''.$xml['serversite'][0]['help'][0].'?id=5\')">?</a> )</td></tr>';
 	 }
echo '</form>';
	 
echo '<i>(*)</i>You need to enter a valid email address in case we need to contant you. You can\'t leave the field empty.'.
	 ' <b>Please, limit yourself to one account per person</b>.';
}

function WriteDownloads($xml)
  {
  // section specifies if this is being called from download page or from games page
 echo '<div id="downloads">';
 echo '<h1>Downloads</h1>';
 echo '<p>You can download files related to our project (including clients, servers and documentation) at: <br><a class="download_link" href="';
 echo $xml['serversite'][0]['downloads'][0]['filelocation'][0].'">'.$xml['serversite'][0]['downloads'][0]['filelocation'][0].'</a></p></div>';

  }
  
function WriteTextServerStats($xml)
  {
  global $content;
  
  parseXMLFile($xml['serversite'][0]['serverdata'][0]['path'][0],"stats_startElement","stats_endElement");
  
  echo '<div id="textstat"><h1>Detailed statistics</h1>';
  echo '<p>This server uptime is '.$content["UPTIME"]["VALUE"].'</p>';

  echo '<table>';
  echo '<tr class="maintitle"><td align="center" colspan="3"><b>Bytes managed</b></td></tr>';
  echo '<tr class="subtitle"><td>Recieved</td><td>Send</td><td>GZIP saved</td><tr>';
  echo '<tr class="data"><td>'.$content["BYTE"]["RECV"].'</td><td>'.$content["BYTE"]["SEND"].'</td><td>'.$content["BYTE"]["SAVED"].'</td></tr>';

  echo '<tr><td>&nbsp;<td/><td/></tr>';
  echo '<tr class="maintitle"><td align="center" colspan="3"><b>Messages managed</b></td></tr>';
  echo '<tr class="subtitle"><td>Recieved</td><td>Send</td><td>Incorrect</td><tr>';
  echo '<tr class="data"><td>'.$content["MESSAGE"]["RECV"].'</td><td>'.$content["MESSAGE"]["SEND"].'</td><td>'.$content["MESSAGE"]["INCORRECT"].'</td></tr>';

  echo '<tr><td>&nbsp;<td/><td/></tr>';
  echo '<tr class="maintitle"><td align="center" colspan="3"><b>Players managed</b></td></tr>';
  echo '<tr class="subtitle"><td>Logins</td><td>Logouts</td><td>Timeouts</td><tr>';
  echo '<tr class="data"><td>'.$content["PLAYER"]["LOGIN"].'</td><td>'.$content["PLAYER"]["LOGOUT"].'</td><td>'.$content["PLAYER"]["TIMEOUT"].'</td></tr>';

  echo '<tr><td>&nbsp;<td/><td/></tr>';
  echo '<tr class="maintitle"><td align="center" colspan="3"><b>Actions managed</b></td></tr>';
  echo '<tr class="subtitle"><td>Handled</td><td>Rejected</td><td/><tr>';
  echo '<tr class="data"><td>'.$content["ACTION"]["ADDED"].'</td><td>'.$content["ACTION"]["INVALID"].'</td><td/>';

  echo '<tr><td>&nbsp;<td/><td/></tr>';
  echo '<tr class="maintitle"><td align="center" colspan="3"><b>Online load</b></td><td/></tr>';
  echo '<tr class="subtitle"><td>Players</td><td>Objects</td><td/><tr>';
  echo '<tr class="data"><td>'.$content["ONLINE"]["PLAYERS"].'</td><td>'.$content["ONLINE"]["OBJECTS"].'</td><td/>';

  echo '</table></div>';
	  
  }
function WriteGraphicServerStats($xml)
  {
  echo '<div id="graphicstat"><h1>Last 24 hours graphical representation</h1>';
  echo '<table>';
  echo '  <tr>';
  echo '    <td>KBytes send per hour during the last 24 hours<br><img src="query/BS_3600.png"></td>';
  echo '    <td>KBytes recieved hour during the last 24 hours<br><img src="query/BR_3600.png"></td>';
  echo '  </tr>';
  echo '  <tr>';
  echo '    <td>Player Online per hour during the last 24 hours<br><img src="query/PO_60.png"></td>';
  echo '    <td>Daily accounts created during last 15 days<br><img src="query/AC_24_7.png"></td>';
  echo '  </tr>';
  echo '</table></div>';
	  
  }
 
?>