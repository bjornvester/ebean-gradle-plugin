create table my_entity_java (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  constraint pk_my_entity_java primary key (id)
);

create table my_test_entity_java (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  constraint pk_my_test_entity_java primary key (id)
);

