CREATE TABLE IF NOT EXISTS conversation_content (
                                                    ID INT(11) PRIMARY KEY AUTO_INCREMENT COMMENT 'id',
                                                    CID INT(11) COMMENT '会话id',
                                                    ROLE VARCHAR(255)   COMMENT '会话角色',
                                                    CONTENT VARCHAR(255)   COMMENT '会话内容',
                                                    TIME VARCHAR(255)  COMMENT '创建时间'
);

CREATE TABLE IF NOT EXISTS conversation (
                        ID INT(11) PRIMARY KEY AUTO_INCREMENT COMMENT 'id',
                        TITLE VARCHAR(255)   COMMENT '会话标题',
                        TIME VARCHAR(255)  COMMENT '创建时间'
);
