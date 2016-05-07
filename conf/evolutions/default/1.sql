# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table device_info (
  id                        integer auto_increment not null,
  device_token              varchar(200) not null,
  device_type               varchar(10) not null,
  username                  varchar(100),
  phone_number              varchar(255) not null,
  role                      varchar(20) not null,
  createdon                 datetime(6),
  constraint pk_device_info primary key (id))
;




# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table device_info;

SET FOREIGN_KEY_CHECKS=1;

