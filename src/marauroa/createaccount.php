
<html>
<body>
<?PHP

if(isset($HTTP_POST_VARS['cmd']))
  {
  if( 
    ((!isset($HTTP_POST_VARS['username'])) OR ereg("\"",$HTTP_POST_VARS['username'])) OR
    ((!isset($HTTP_POST_VARS['password'])) OR ereg("\"",$HTTP_POST_VARS['password'])) OR
    ((!isset($HTTP_POST_VARS['character_model'])) OR ereg("\"",$HTTP_POST_VARS['character_model'])) OR
    ((!isset($HTTP_POST_VARS['gladiator'])) OR ereg("\"",$HTTP_POST_VARS['gladiator'])) OR
    ((!isset($HTTP_POST_VARS['gladiator_model'])) OR ereg("\"",$HTTP_POST_VARS['gladiator_model'])))
    {
    echo 'Don\'t be a <i>bad guy</i>. This is a free game<p>';    
    exit();
    }
  else
    {
    /* Create the account. */
    $cmdline='java -cp "mysql-connector-java-3.0.9-stable-bin.jar;marauroa-0.21.jar" marauroa.createaccount';
    $cmdline=$cmdline.' -u "'.$HTTP_POST_VARS['username'].'"';
    $cmdline=$cmdline.' -p "'.$HTTP_POST_VARS['password'].'"';
    $cmdline=$cmdline.' -c "'.$HTTP_POST_VARS['username'].'"';
    $cmdline=$cmdline.' -cm "'.$HTTP_POST_VARS['character_model'].'"';
    $cmdline=$cmdline.' -g "'.$HTTP_POST_VARS['gladiator'].'"';
    $cmdline=$cmdline.' -gm "'.$HTTP_POST_VARS['gladiator_model'].'"';

    $output=array();
    exec($cmdline,$output,$return);
    
    if($return==1)
      {
      echo 'You forgot to set a value. Check again form';
      exit();
      }
    else if($return==2)
      {
      echo 'Ahh!, you a bad guy? No account created. Check the characters used.';
      exit();
      }
    else if($return==3)
      {
      echo 'Ahh!, you a bad guy? No account created. Check the size of your strings used.';
      exit();
      }
    else if($return==4)
      {
      echo 'Bad luck! Player does exists. Try another username';
      exit();
      }
    else if($return==5)
      {
      echo 'Something bad happened.';
      exit();
      }
    else
      {
      echo '<b><font color="#0000FF">Account created.</font><p>'.
           'Write this down, so you don\'t forget it<br>'.
           '<b>Server IP</b>: marauroa.ath.cx<br>'.      
           '<b>Username</b>: '.$HTTP_POST_VARS['username'].'<br>'.
           '<b>Password</b>: '.$HTTP_POST_VARS['password'].'<br>'.
           '<b>Character name</b>: '.$HTTP_POST_VARS['username'].'<p>'.
           '';
           
      exit();
      }
    } 
  }

$character_models= array("billgates");
$gladiator_models= array("pknight","orc","yohko");

echo '<b><font color="#0000FF">Marauroa\'s online server</font></b><br>'.
   	 'Fill all the fields and create an account for yourself on this Marauroa server<br>'.
   	 'Enjoy!<p>';

echo '<form method="POST" action="index.php">'.
	 '<input type="hidden" name="cmd" value="newaccount"/>'.
	 '<table>'.
	 '<tr><td><b>Username</b>: </td><td><input type="text" name="username" maxlength="20"/></td></tr>'.
	 '<tr><td><b>Password</b>: </td><td><input type="password" name="password"/></td></tr>'.
	 '<tr><td><b>Character model</b>: </td><td><select name="character_model">';
	 for($i=0;$i<count($character_models);$i++)
	   {
	   echo '<option value="'.$character_models[$i].'">'.$character_models[$i].'</option>';
	   }
echo '</select></td></tr>'.
	 '<tr><td><b>Gladiator name</b>: </td><td><input type="text" name="gladiator" maxlength="20"/></td></tr>'.
	 '<tr><td><b>Gladiator model</b>: </td><td><select name="gladiator_model">';
	 for($i=0;$i<count($gladiator_models);$i++)
	   {
	   echo '<option value="'.$gladiator_models[$i].'">'.$gladiator_models[$i].'</option>';
	   }
echo '</select></td></tr>'.
	 '<tr><td></td><td><input type="submit" value="Create account"/></td></tr>'.
	 '</table>'.
     '</form>';
echo '<p>Remember you can help us by submitting new MD2 ( like Quake 2 ) models and helping to debug the game.'.
	 '<br><a href="http://marauroa.sourceforge.net">Marauroa</a>';
?>
</body>
</html>