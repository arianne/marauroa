#!/bin/bash
./gnuplots.sh AC_24_7.png "select cast(UNIX_TIMESTAMP(timedate)/(24*3600) as signed) as time,count(*) from player where (unix_timestamp(now())-unix_timestamp(timedate))<15*24*3600 group by time order by time desc;" "Daily created accounts " 0 40
./gnuplots_time.sh BS_3600.png "select date_format(timedate,\"%m/%d/%H\") as time,sum(bytes_send)/1024 as bytes_send from statistics where (unix_timestamp(now())-unix_timestamp(timedate))<24*3600 group by time;" "KBytes send per hour" 0
./gnuplots_time.sh BR_3600.png "select date_format(timedate,\"%m/%d/%H\") as time,sum(bytes_recv)/1024 as bytes_recv from statistics where (unix_timestamp(now())-unix_timestamp(timedate))<24*3600 group by time;" "KBytes recv per hour" 0
./gnuplots_time.sh PO_60.png "select date_format(timedate,\"%m/%d/%H\") as time,max(players_online) from statistics where (unix_timestamp(now())-unix_timestamp(timedate))<24*3600 group by time;" "Average Players load per hour" 0 24
