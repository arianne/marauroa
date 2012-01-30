/**
 * The package stores the connection validator.
 * <p>
 * Currently works only with version 4 of the TPC/IP protocol.<br>
 * It support both UDP and TCP sockets.<br>
 * 
 * Connection valiator stores at database some permanent bans.
 * <pre>
 create table if not exists banlist
 (
 id integer auto_increment not null,
 address varchar(15),
 mask    varchar(15),
 
 PRIMARY KEY(id)
 );
 * </pre>
 */
package marauroa.server.net.validator;

