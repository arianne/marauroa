create table if not exists player
  (
  id integer auto_increment not null,
  username VARCHAR(32) not null,
  password VARCHAR(32) not null,
  
  email VARCHAR(64) not null,
  timedate TIMESTAMP,
  status ENUM('active','inactive','banned') not null default 'active',
  
  primary key(id)
  )
  TYPE=INNODB;


create table if not exists characters
  (
  player_id integer not null,
  charname VARCHAR(32) not null,
  zone_id VARCHAR(32) not null,
  object_id integer not null,
  
  PRIMARY KEY(charname,player_id)
  )
  TYPE=INNODB;
  

create table if not exists loginEvent
  (
  player_id integer not null,
  address VARCHAR(64),
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
  id integer not null,
  zone_id varchar(32) not null,
  slot_id integer
  
  PRIMARY KEY(id,zone_id)
  )
  TYPE=INNODB;
  
create table if not exists rpattribute
  (
  object_id integer not null,
  zone_id varchar(32) not null,
  name varchar(64) not null,
  value varchar(255),
  primary key(object_id,zone_id,name)
  )
  TYPE=INNODB;

create table if not exists rpslot
  (
  object_id integer not null,
  zone_id varchar(32) not null,
  name varchar(64) not null,
  slot_id integer auto_increment not null,
  
  primary key(slot_id)
  )
  TYPE=INNODB;

create table if not exists rpzone
  (
  object_id integer not null,
  zone_id varchar(32) not null,
  
  primary key(object_id,zone_id)
  )
  TYPE=INNODB;

create table if not exists rpworld
  (
  zone_id varchar(32) not null,  

  primary key(zone_id)
  )
  TYPE=INNODB;
