create table if not exists player
  (
  id integer auto_increment not null,
  username varchar(32) not null,
  password varchar(32) not null,
  
  email varchar(64) not null,
  timedate timestamp,
  status ENUM('active','inactive','banned') not null default 'active',
  
  primary key(id)
  )
  TYPE=INNODB;

create table if not exists characters
  (
  player_id integer not null,
  charname varchar(32) not null,
  object_id integer not null,
  
  PRIMARY KEY(charname,player_id)
  )
  TYPE=INNODB;

create table if not exists rpobject
  (
  object_id integer auto_increment not null,
  slot_id integer,
  
  PRIMARY KEY(object_id)
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


create table if not exists loginEvent
  (
  player_id integer not null,
  address varchar(64),
  timedate timestamp,
  result tinyint
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
  
create table if not exists banlist
  (
    id integer auto_increment not null,
    address char(15),
    mask    char(15),
    PRIMARY KEY(id)
  );
