<?php 
if(isset($HTTP_GET_VARS['id']))
{
        $id = $HTTP_GET_VARS['id'];
	switch($id)
	{
	case 1:
?>

<a name="name"></a><h3>Real Name</h3><p>You must provide your real name for registration purposes. Please not however that 
this information will be kept private and will not at any time be passed onto third parties.</p>
<p align="center"><a href="javascript:window.close()">Close</a></p>

<?php 
	break;
	case 2:
?>

<a name="email"></a><h3>Real E-mail Address</h3><p>You must provide a real email address for registration purposes. Please not however that 
this information will be kept private and will not at any time be passed onto third parties.</p>
<p align="center"><a href="javascript:window.close()">Close</a></p>

<?php 
	break;
	case 3:
?>

<a name="username"></a><h3>Username</h3><p>Your username is your unique identification. It is the name under which you will login to 
the game server.</p>
<p align="center"><a href="javascript:window.close()">Close</a></p>

<?php 
	break;
	case 4:
?>

<a name="password"></a><h3>Password</h3><p>Your password is kept private and is case sensitive so make sure you enter it correctly. 
It is advised that you create a new unique password for the game and not some important one 
you use for your bank for example!</p>
<p align="center"><a href="javascript:window.close()">Close</a></p>

<?php 
	break;
	case 5:
?>

<a name="create"></a><h3>Create!</h3><p>Click this button to create your account. Please press once only!</p>
<p align="center"><a href="javascript:window.close()">Close</a></p>

<?php 
	}
}
?>