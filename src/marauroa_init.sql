create table if not exists player 
  (
  id integer auto_increment not null,
  username VARCHAR(30) not null,
  password VARCHAR(30) not null,
  
  email VARCHAR(50) not null,
  timedate TIMESTAMP,
  
  primary key(id)
  );

create table if not exists characters 
  (
  player_id integer not null,
  charname VARCHAR(30) not null,
  object_id integer not null, 
  
  PRIMARY KEY(charname,player_id)
  );

create table if not exists loginEvent 
  ( 
  player_id integer not null,
  address VARCHAR(40), 
  timedate TIMESTAMP,
  result TINYINT
  );

create table if not exists statistics 
  ( 
  timedate timestamp,
  
  bytes_send integer,
  bytes_recv integer,
  
  players_login integer,
  players_logout integer,
  players_timeout integer,
  players_online integer,
  
  PRIMARY KEY(timedate)
  );

create table if not exists rpobject
  (
  id integer not null primary key, 
  slot_id integer
  ) 
  TYPE=INNODB;
  
create table if not exists rpattribute
  (
  object_id integer not null,
  name varchar(64) not null,
  value varchar(255),
  primary key(object_id,name)
  )
  TYPE=INNODB;

create table if not exists rpslot
  (
  object_id integer not null, 
  name varchar(64) not null, 
  slot_id integer auto_increment not null,
  
  primary key(slot_id)
  ) 
  TYPE=INNODB;



