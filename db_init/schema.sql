CREATE TABLE tb_user
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'unique identifier of the user',
    email VARCHAR(200) NOT NULL COMMENT 'email for user',
    password VARCHAR(129) NOT NULL COMMENT 'password',
    name VARCHAR(120) NULL,
    PRIMARY KEY (id)
) COMMENT 'All users';

ALTER TABLE tb_user
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

CREATE TABLE tb_user_external_project
(
    id VARCHAR(200) NOT NULL COMMENT 'identifier of external project',
    user_id BIGINT NOT NULL COMMENT 'unique identifier of the user',
    name VARCHAR(120) NOT NULL COMMENT 'Name of external project',
    PRIMARY KEY (id, user_id)
) COMMENT 'External Project identifier for users';

ALTER TABLE tb_user_external_project
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;