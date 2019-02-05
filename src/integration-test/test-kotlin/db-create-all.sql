create table my_entity_java (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  constraint pk_my_entity_java primary key (id)
);

create table my_entity_kotlin (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  constraint pk_my_entity_kotlin primary key (id)
);

create table my_entity_kotlin_extends_super_java (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  constraint pk_my_entity_kotlin_extends_super_java primary key (id)
);

create table my_kotlin_test_entity (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  constraint pk_my_kotlin_test_entity primary key (id)
);

