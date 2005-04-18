<?PHP	
	
/************************************************************************************************
 ***
 *** CONFIGURATION: Modify values to meet your system
 ***
 *************************************************************************************************/

/* Location and name of the settings file for this site */
$xmlSiteSettings="settings.xml";

/************************************************************************************************
 *** CODE:
 *** You shouldn't need to change this.
 ***
 *************************************************************************************************/

$depth=array();
$content=array();

$xml=array();

include_once('WriteHTML.php');           
$content=implode("",file($xmlSiteSettings));
$xml = XML_unserialize($content);

?>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd"> 
<html>
<head>

<?php
global $xml;
// build header
WriteHeader($xml);      
?>


  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

  <script language="JavaScript" type="text/javascript">
	var newwindow = '';

	function popitup(url)
	  {
	  if (!newwindow.closed && newwindow.location)
	    {
	    newwindow.location.href = url;
	    }
	  else
	    {
		newwindow=window.open(url,'name','height=200,width=300');
		if (!newwindow.opener) newwindow.opener = self;
	    }
	  if (window.focus) {newwindow.focus()}
	  return false;
      }
  </script>
</head>

<body>

<!-- Page Title Start -->
<div id="title_section">
<?php 
global $xml;
WriteTitleSection($xml);      
?>
</div>
<!-- Page Title End -->

<div class="clearboth">&nbsp;</div>

<!-- News Bar Start -->
<?php 
global $xml;

if( $xml['serversite'][0]['newsbar']['0 attr']['enable'] == "1" )
{
	WriteNewsBar($xml);   
}
?>
<!-- News Bar End -->

<!-- Page Content Start -->
<div id="pagecontent">
<?PHP 

global $xml;
WritePageContent($xml);      
?>


<div class="clearleft">&nbsp;</div>

<?php
global $xml;

include('account.php');

if( $xml['serversite'][0]['top20']['0 attr']['enable'] == "1" )
{
	echo '<div id="top20">';
	include('top20.php');
	echo '</div>';
} 

if( $xml['serversite'][0]['downloads']['0 attr']['enable'] == "1" )
{
	WriteDownloads($xml);
}

if( $xml['serversite'][0]['serverdata'][0]['text']['0 attr']['enable'] == "1" )
{
	WriteTextServerStats($xml);
}

if( $xml['serversite'][0]['serverdata'][0]['graphic']['0 attr']['enable'] == "1" )
{
	WriteGraphicServerStats($xml);
}

if( $xml['serversite'][0]['screenshots']['0 attr']['enable'] == "1" )
{
	WriteRenderScreenshots($xml);
}
?>

</div>
<!-- Page Content End -->

<!-- Page Footer Start -->
<div id="pagefooter">
<?php 
global $xml;
WriteFooter($xml);      
?>
</div>
<!-- Page Footer End -->

</body>
</html>
