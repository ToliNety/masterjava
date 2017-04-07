DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS cities;
DROP TABLE IF EXISTS groups;
DROP TABLE IF EXISTS users;
DROP SEQUENCE IF EXISTS user_seq;
DROP TYPE IF EXISTS user_flag;

CREATE TYPE user_flag AS ENUM ('active', 'deleted', 'superuser');
CREATE TYPE group_type AS ENUM ('FINISHED', 'CURRENT', 'REGISTERING');

CREATE SEQUENCE user_seq START 100000;

CREATE TABLE users (
  id        INTEGER PRIMARY KEY DEFAULT nextval('user_seq'),
  full_name TEXT NOT NULL,
  email     TEXT NOT NULL,
  flag      user_flag NOT NULL
);

CREATE UNIQUE INDEX email_idx ON users (email);

CREATE TABLE cities (
  id        TEXT PRIMARY KEY,
  name      TEXT NOT NULL
);

CREATE TABLE projects (
  id          INTEGER PRIMARY KEY DEFAULT nextval('user_seq'),
  name        TEXT NOT NULL,
  description TEXT NOT NULL
);

CREATE UNIQUE INDEX project_name_idx ON projects (name);

CREATE TABLE groups (
  id          INTEGER PRIMARY KEY DEFAULT nextval('user_seq'),
  name        TEXT NOT NULL,
  type        group_type NOT NULL,
  project_id  INTEGER NOT NULL,
  FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX group_name_idx ON groups (name);