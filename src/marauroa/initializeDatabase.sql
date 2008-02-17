#
# Example SQL code to create database for Marauroa and granting access rights to an
# user named marauroa_dbuser with password marauroa_dbpwd.
#
create database marauroa;
grant all on marauroa.* to marauroa_dbuser@localhost identified by 'marauroa_dbpwd';