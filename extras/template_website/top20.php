<?
/*
$hostName = "localhost";
$userName = "mapacman_user";
$password = "mapacman_passwd";
$dbName = "mapacman";
$topFame = 'select s.value,r.value from rpattribute r, rpattribute s where r.name'.
	'="score" and s.name="name" and s.object_id=r.object_id order by cast(r.value as'.
	' signed) desc limit 15;';*/

function renderQuery($title, $query)
  {
  // Select all the fields in all the records of the Computers table
  $result = mysql_query($query);

  // Determine the number of computers
  $number = mysql_numrows($result);
  
  echo '<h2>'.$title.'</h2>';
  echo '<table>';
  for ($i=0; $i<$number; $i++) 
    {
    $row= mysql_fetch_row($result);
    $columns=mysql_num_fields($result);
    if($i%2==0)
      {
      echo '<tr class="row1">';
      }  
    else
      {
      echo '<tr class="row2">';
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
  }


// make connection to database
mysql_connect($xml['serversite'][0]['dbhostname'][0], $xml['serversite'][0]['dbusername'][0], $xml['serversite'][0]['dbpassword'][0]) or die("Unable to connect to host $hostName");

mysql_select_db($xml['serversite'][0]['dbname'][0]) or die( "Unable to select database $dbName");



renderQuery($xml['serversite'][0]['top20'][0]['title'][0],$xml['serversite'][0]['top20'][0]['query'][0]);

// Close the database connection
mysql_close();
?>
