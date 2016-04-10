# donations schema

# --- !Ups

CREATE TABLE donations (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  received double NOT NULL,
  currency varchar(255) NOT NULL,
  provider varchar(255) NOT NULL,
  time int(11) NOT NULL
);

# --- !Downs

DROP TABLE donations;
