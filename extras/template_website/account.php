<?PHP

/************************************************************************************************
 *** CODE:
 *** You shouldn't need to change this, unless you want to add some parameters on creation time.
 *** My recomendation is that you do it at runtime. 
 ***
 *************************************************************************************************/
global $xml;

echo '<div id="account_form">';

$created_char=false;
if(isset($HTTP_POST_VARS['cmd']))
  {
  if( 
    ((!isset($HTTP_POST_VARS['realname'])) OR ereg("\"",$HTTP_POST_VARS['realname'])) OR
    ((!isset($HTTP_POST_VARS['email'])) OR ereg("\"",$HTTP_POST_VARS['email'])) OR
    ((!isset($HTTP_POST_VARS['username'])) OR ereg("\"",$HTTP_POST_VARS['username'])) OR
    ((!isset($HTTP_POST_VARS['password'])) OR ereg("\"",$HTTP_POST_VARS['password'])))
    {
    echo '<p>Please don\'t mess about here. This is a free game</p>';    
    }
  else
    {
    include('mailValidator.php');
    $result=ValidateMail($HTTP_POST_VARS['email']);
    if($result[0]==false)
      {
      echo '<p>You need to provide a valid email address. We want to limit the server to one account per person.</p>';    
      }

    /* Create the account. */
    $cmdline='"'.$xml['serversite'][0]['accountcreationinfo'][0]['routetojavaVM'][0].' -cp "'.$xml['serversite'][0]['accountcreationinfo'][0]['classpath'][0].'" '.$xml['serversite'][0]['accountcreationinfo'][0]['createAccountclass'][0];
    $cmdline=$cmdline.' -u "'.$HTTP_POST_VARS['username'].'"';
    $cmdline=$cmdline.' -p "'.$HTTP_POST_VARS['password'].'"';
    $cmdline=$cmdline.' -c "'.$HTTP_POST_VARS['username'].'"';
    $cmdline=$cmdline.' -e "'.$HTTP_POST_VARS['email'].'"';

    $output=array();
    exec($cmdline,$output,$return);

    if($return==1)
      {
      echo '<p class="warning">You forgot to set a value. Check again form</p>';
      }
    else if($return==2)
      {
      echo '<p class="warning">Ahh!, you trying to mess about? No account created. Check the characters used.</p>';
      }
    else if($return==3)
      {
      echo '<p class="warning">Ahh!, No account created. Check the size of your strings used.</p>';
      }
    else if($return==4)
      {
      echo '<p class="warning">Bad luck! Player already exists. Try another username.</p>';
      }
    else if($return==0)
      {
      echo '<p><b>Account created.</b></p>'.
           '<p>Write this information down, so you don\'t forget it:<br>'.
           '<b>Server IP</b>: marauroa.ath.cx<br>'.      
           '<b>Username</b>: '.htmlspecialchars($HTTP_POST_VARS['username'], ENT_QUOTES).'<br>'.
           '<b>Password</b>: '.htmlspecialchars($HTTP_POST_VARS['password'], ENT_QUOTES).'<br>'.
           '<b>Character name</b>: '.htmlspecialchars($HTTP_POST_VARS['username'], ENT_QUOTES).'</p>'.
           '';
           
	  $created_char=true;
      }
    else
      {
      echo '<p class="warning">Something bad happened.</p>';
      }
    } 
  }

if($created_char==false)
  {
		WriteAccountForm($xml);
  }
echo '</div>';
?>
