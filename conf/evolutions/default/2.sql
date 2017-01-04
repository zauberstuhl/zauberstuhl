# projects schema

# --- !Ups

CREATE TABLE projects (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title varchar(25) NOT NULL,
  body varchar(255) NOT NULL
);

# --- !Downs

DROP TABLE projects;
