<?
$hostName = "localhost";
$userName = "mapacman_user";
$password = "mapacman_passwd";
$dbName = "mapacman";


function renderQuery($title, $query)
  {
  // Select all the fields in all the records of the Computers table
  $result = mysql_query($query);

  // Determine the number of computers
  $number = mysql_numrows($result);
  
  echo '<h2>'.$title.'</h2>';
  echo '<table width="300"><tr><td>';
  echo '<table style="width: 250px; text-align: center;" border="0" cellspacing="0" cellpadding="0">';
  for ($i=0; $i<$number; $i++) 
    {
    $row= mysql_fetch_row($result);
    $columns=mysql_num_fields($result);
    if($i%2==0)
      {
      echo '<tr style="background-color: #91D4FF; vertical-align: top;">';
      }  
    else
      {
      echo '<tr style="background-color: #5F72E3; vertical-align: top;">';
      }
    for($j=0; $j<$columns; $j++)
      {
      $value=htmlspecialchars($row[$j], ENT_QUOTES);
      if($j==0) 
        {
        echo '<td>';
        if($i<2) echo '<font size="+'.(2-$i).'">';
        echo '<i>'.$value.'</i>';
        if($i<2) echo '</font>';
        echo '</td>';
        }
      else
        {
        echo '<td>'.$value.'</td>';
        }
      }
    echo '</tr>';
    }
  
  echo '</table>';
  echo '</td></tr></table>';
  }


// make connection to database
mysql_connect($hostName, $userName, $password) or die("Unable to connect to host $hostName");

mysql_select_db($dbName) or die( "Unable to select database $dbName");

$topFame = 'select s.value,r.value from rpattribute r, rpattribute s where r.name'.
	'="score" and s.name="name" and s.object_id=r.object_id order by cast(r.value as'.
	' signed) desc limit 15;';

renderQuery("Top 15 Pacmans",$topFame);

// Close the database connection
mysql_close();
?>
