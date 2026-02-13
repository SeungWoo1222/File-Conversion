-- Debezium user
CREATE USER IF NOT EXISTS 'debezium'@'%' IDENTIFIED BY 'dbzpw';

-- Debezium이 binlog/스냅샷 위해 흔히 필요한 권한
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT
      ON *.* TO 'debezium'@'%';

FLUSH PRIVILEGES;
