<?php 
        function ValidateMail($Email) { 
            global $HTTP_HOST; 
    $result = array(); 

if (!eregi("^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,3})$", $Email)) { 

  $result[0]=false; 
        $result[1]="$Email is not properly formatted"; 
        return $result; 
    } 

    $result[0]=true; 
    $result[1]="$Email appears to be valid."; 
    return $result; 
} // end of function 
?>