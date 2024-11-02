CREATE TABLE IF NOT EXISTS pessoa
(
    apelido    TEXT UNIQUE NOT NULL PRIMARY KEY,
    nome       TEXT        NOT NULL,
    nascimento DATE        NOT NULL
);