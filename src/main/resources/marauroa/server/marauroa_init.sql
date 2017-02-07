create table if not exists account
  (
  id integer auto_increment not null,
  username varchar(255) not null,
  password varchar(255),
  timedate timestamp default CURRENT_TIMESTAMP,
  status char(8) not null default 'active',

  primary key(id)
  );

create unique index if not exists i_account_username on account(username);


create table if not exists characters
  (
  id integer auto_increment not null,
  player_id integer not null,
  charname varchar(32) not null,
  object_id integer not null,
  timedate timestamp default CURRENT_TIMESTAMP,
  status char(8) not null default 'active',

  PRIMARY KEY(id)
  );

create unique index if not exists i_characters_charname ON characters(charname);
create index if not exists i_characters_player_id ON characters(player_id);

create table if not exists rpobject
  (
  object_id integer auto_increment not null,
  data blob,  
  protocol_version integer,
  primary key(object_id)
  );

create table if not exists rpzone 
  (
  zone_id varchar(32) not null,
  data blob,
  protocol_version integer,
  primary key(zone_id)
  )
 ;

create table if not exists loginEvent
  (
  id integer auto_increment not null,
  player_id integer not null,
  account_link_id integer,
  address varchar(64),
  service char(20),
  seed varchar(120),

  timedate timestamp default CURRENT_TIMESTAMP,

  result tinyint,
  primary key(id)
  );

create index if not exists i_loginEvent_player_id_timedate ON loginEvent(player_id, timedate);
create index if not exists i_loginEvent_address_timedate ON loginEvent(address, timedate);
create index if not exists i_loginEvent_timedate ON loginEvent(timedate);


create table if not exists passwordChange
  (
  id integer auto_increment not null,
  player_id integer not null,
  address varchar(64),
  service char(20),
  timedate timestamp default CURRENT_TIMESTAMP,
  oldpassword varchar(255),
  result tinyint,
  primary key(id)
  );

create index if not exists i_passwordChange_player_id ON passwordChange(player_id);
create index if not exists i_passwordChange_address ON passwordChange(address);

create table if not exists statistics
  (
  id integer auto_increment not null,
  timedate timestamp default CURRENT_TIMESTAMP,
  
  bytes_send integer,
  bytes_recv integer,
  
  players_login integer,
  players_logout integer,
  players_timeout integer,
  players_online integer,

  ips_online integer,

  PRIMARY KEY(id)
  );

create index if not exists i_statistics_timedate ON statistics(timedate);

create table if not exists gameEvents
  (
  id integer auto_increment not null,
  timedate timestamp default CURRENT_TIMESTAMP,
  source varchar(64),
  event  varchar(64),
  param1 varchar(128),
  param2 varchar(255),
  PRIMARY KEY(id)
  );
  
create index if not exists i_gameEvents_timedate ON gameEvents(timedate);
create index if not exists i_gameEvents_param1 ON gameEvents(param1);
create index if not exists i_gameEvents_param2 ON gameEvents(param2);
create index if not exists i_gameEvents_source_event ON gameEvents(source, event);
create index if not exists i_gameEvents_event_param1 ON gameEvents(event, param1);
create index if not exists i_gameEvents_source_timedate ON gameEvents(source, timedate);
create index if not exists i_gameEvents_event_timedate ON gameEvents(event, timedate);


create table if not exists loginseed
  (
  id integer auto_increment not null,
  player_id integer,
  seed varchar(120),
  address varchar(64),
  complete integer,
  used integer,
  timedate timestamp default CURRENT_TIMESTAMP,
  primary key(id)
  );

create unique index if not exists i_loginseed_seed on loginseed(seed);
create index if not exists i_loginseed_player_id on loginseed(player_id);

create table if not exists banlist
  (
  id integer auto_increment not null,
  address varchar(64),
  mask    varchar(15),
  reason  varchar(255),
  PRIMARY KEY(id)
  );

create table if not exists accountban
  (
  id integer auto_increment not null,
  player_id integer,
  reason  varchar(255),
  timedate timestamp default CURRENT_TIMESTAMP,
  expire timestamp null default NULL,
  PRIMARY KEY(id)
  );

create index if not exists i_accountban_player_id ON accountban(player_id);



create table if not exists accountLink
  (
  id          integer auto_increment not null,
  player_id   integer,
  type        char(10),
  username    varchar(255),
  nickname    varchar(255),
  email       varchar(255),
  secret      varchar(255),
  PRIMARY KEY(id)
  );

create index if not exists i_accountLink_player_id ON accountLink(player_id);
create index if not exists i_accountLink_username ON accountLink(username);


create table if not exists email
  (
  id integer auto_increment not null,
  player_id  integer,
  email      varchar(64),
  token      varchar(64),
  address    varchar(64),
  timedate   timestamp default CURRENT_TIMESTAMP,
  confirmed  timestamp,
  primary key(id)
  );

create index if not exists i_email_email_timedate ON email(email, timedate);
create index if not exists i_email_player_id ON email(player_id);
