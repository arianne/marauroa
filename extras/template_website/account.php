<?PHP
/************************************************************************************************
 ***
 *** CONFIGURATION: Modify values to meet your system
 ***
 *************************************************************************************************/

/** Location of the java virtual machine */
$route_to_javavm='C:/j2sdk1.4.2_05/bin/javaw';

/** Classpath location */
$classpath='mysql-connector-java-3.0.9-stable-bin.jar;marauroa-0.41.jar';

/** Name of the createaccount class */
$createAccount_class='mapacman.mapacmancreateaccount';

/************************************************************************************************
 *** CODE:
 *** You shouldn't need to change this, unless you want to add some parameters on creation time.
 *** My recomendation is that you do it at runtime. 
 ***
 *************************************************************************************************/

$created_char=false;
if(isset($HTTP_POST_VARS['cmd']))
  {
  if( 
    ((!isset($HTTP_POST_VARS['realname'])) OR ereg("\"",$HTTP_POST_VARS['realname'])) OR
    ((!isset($HTTP_POST_VARS['email'])) OR ereg("\"",$HTTP_POST_VARS['email'])) OR
    ((!isset($HTTP_POST_VARS['username'])) OR ereg("\"",$HTTP_POST_VARS['username'])) OR
    ((!isset($HTTP_POST_VARS['password'])) OR ereg("\"",$HTTP_POST_VARS['password'])))
    {
    echo 'Don\'t be a <i>bad guy</i>. This is a free game<p>';    
    echo '<p>';
    }
  else
    {
    include('mailValidator.php');
    $result=ValidateMail($HTTP_POST_VARS['email']);
    if($result[0]==false)
      {
      echo 'You need to provide a valid email address. We want to limit the server to one account per person.<p>';    
      echo '<p>';
      }

    /* Create the account. */
    $cmdline='"'.$route_to_javavm.' -cp "'.$classpath.'" '.$createAccount_class;
    $cmdline=$cmdline.' -u "'.$HTTP_POST_VARS['username'].'"';
    $cmdline=$cmdline.' -p "'.$HTTP_POST_VARS['password'].'"';
    $cmdline=$cmdline.' -c "'.$HTTP_POST_VARS['username'].'"';
    $cmdline=$cmdline.' -e "'.$HTTP_POST_VARS['email'].'"';

    $output=array();
    exec($cmdline,$output,$return);

    if($return==1)
      {
      echo '<font color="#FF0000">You forgot to set a value. Check again form</font>';
	  echo '<p>';
      }
    else if($return==2)
      {
      echo '<font color="#FF0000">Ahh!, you a bad guy? No account created. Check the characters used.</font>';
	  echo '<p>';
      }
    else if($return==3)
      {
      echo '<font color="#FF0000">Ahh!, No account created. Check the size of your strings used.</font>';
	  echo '<p>';
      }
    else if($return==4)
      {
      echo '<font color="#FF0000">Bad luck! Player does exists. Try another username.</font>';
	  echo '<p>';
      }
    else if($return==0)
      {
      echo '<b><font color="#0000FF">Account created.</font></b><p>'.
           'Write this down, so you don\'t forget it<br>'.
           '<b>Server IP</b>: marauroa.ath.cx<br>'.      
           '<b>Username</b>: '.htmlspecialchars($HTTP_POST_VARS['username'], ENT_QUOTES).'<br>'.
           '<b>Password</b>: '.htmlspecialchars($HTTP_POST_VARS['password'], ENT_QUOTES).'<br>'.
           '<b>Character name</b>: '.htmlspecialchars($HTTP_POST_VARS['username'], ENT_QUOTES).'<p>'.
           '';
           
	  $created_char=true;
      }
    else
      {
      echo '<font color="#FF0000">Something bad happened.</font>';
	  echo '<p>';
      }
    } 
  }

if($created_char==false)
  {
echo '<h1>Create account for free</h1>';  
echo 'You need an account in order to play mapacman. You can create a free account here.';
echo '<table><tr><td>';
echo '<form name="accountForm" method="POST" action="">'.
	 '<input type="hidden" name="cmd" value="newaccount"/>'.
	 '<table width="90%">'.
	 '<tr><td width="30%">Real name: </td><td><input type="text" name="realname" size="60" maxlength="60"/>'.
         '<font size="2">'.
         '( <a href="" onClick="return popitup(\'help.php?id=1\')">?</a> )</td></tr>'.
         '</font>'.
	 '<tr><td>Email <i>(*)</i>: </td><td><input type="text" name="email" size="60" maxlength="60"/>'.
         '<font size="2">'.
         '( <a href="" onClick="return popitup(\'help.php?id=2\')">?</a> )</td></tr>'.
         '</font>'.
	 '</table>'.
	 '<p>Username is not modifiable once choosen, so choose wisely.<br>'.
	 '<table width="90%">'.
	 '<tr><td width="30%">Username: </td><td><input type="text" name="username" size="20" maxlength="20"/>'.
         '<font size="2">'.
         '( <a href="" onClick="return popitup(\'help.php?id=3\')">?</a> )</td></tr>'.
         '</font>'.
	 '<tr><td>Password: </td><td><input type="password" size="20" name="password"/>'.
         '<font size="2">'.         
         '( <a href="" onClick="return popitup(\'help.php?id=4\')">?</a> )</td></tr>'.
         '</font>'.
	 '</table>'.
	 '<p><input type="submit" value="Create account"/>'.
         '<font size="2">'.   
         '( <a href="" onClick="return popitup(\'help.php?id=8\')">?</a> )'.
         '</font>'.
	 '</form>';
	 
echo '<i>(*)</i>You need to write an email where we can contact you if it is requiered. You can\'t leave the field empty.'.
	 ' <b>Please also limit yourself to one account per person</b>.';

echo '</td></tr></table>';

  }
?>
