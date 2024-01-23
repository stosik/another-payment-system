-- liquibase formatted sql

CREATE TABLE IF NOT EXISTS customer (
    id INT NOT NULL AUTO_INCREMENT,
    currency VARCHAR(3) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS invoice (
    id INT NOT NULL AUTO_INCREMENT,
    currency VARCHAR(255) NOT NULL,
    value DATE NOT NULL,
    customer_id INT NOT NULL,
    status DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS job_lock (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    locked TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);