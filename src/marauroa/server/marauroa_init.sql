create table if not exists account
  {
  id integer auto_increment not null,
  username varchar(32) not null,
  password varchar(255) not null,
  
  email varchar(64) not null,
  timedate timestamp,
  status ENUM('active','banned') not null default 'active',
  
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
  data blob,  
  
  primary key(object_id)
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
  players_online integer
  );

create table if not exists gameEvents
  (
  timedate timestamp,
  
  source varchar(64),
  event  varchar(64),
  param1 varchar(128),
  param2 varchar(1024)
  );
  
create table if not exists banlist
  (
  id integer auto_increment not null,
  address varchar(15),
  mask    varchar(15),

  PRIMARY KEY(id)
  );
