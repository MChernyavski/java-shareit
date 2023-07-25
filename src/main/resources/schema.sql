DROP TABLE IF EXISTS USERS CASCADE;
create table IF NOT EXISTS USERS
(
    id    BIGINT        NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    name  VARCHAR(255)  NOT NULL,
    email VARCHAR(512)  NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

DROP TABLE IF EXISTS REQUESTS CASCADE;
create table IF NOT EXISTS REQUESTS
(
    ID           BIGINT                                    NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    DESCRIPTION  VARCHAR                                   NOT NULL,
    REQUESTOR_ID BIGINT,
    CREATED      TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,
    CONSTRAINT PK_REQUEST PRIMARY KEY (ID),
    CONSTRAINT FK_REQUEST_USER FOREIGN KEY (REQUESTOR_ID) references USERS (ID) on delete cascade
);

DROP TABLE IF EXISTS ITEMS CASCADE;
CREATE TABLE IF NOT EXISTS ITEMS
(
    ID          BIGINT       NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    NAME        VARCHAR(255) NOT NULL,
    DESCRIPTION VARCHAR      NOT NULL,
    AVAILABLE   BOOLEAN,
    OWNER_ID    BIGINT       NOT NULL,
    REQUEST_ID  BIGINT,
    CONSTRAINT PK_ITEM PRIMARY KEY(ID),
    CONSTRAINT FK_ITEM_USERS FOREIGN KEY (OWNER_ID) references USERS (ID) on delete cascade,
    CONSTRAINT FK_ITEM_REQUESTS FOREIGN KEY (REQUEST_ID) references REQUESTS (ID) on delete cascade
);

DROP TABLE IF EXISTS BOOKINGS CASCADE;
CREATE TABLE IF NOT EXISTS BOOKINGS
(
    ID         BIGINT                      NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    START_DATE TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    END_DATE   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    ITEM_ID    BIGINT                      NOT NULL,
    BOOKER_ID  BIGINT                      NOT NULL,
    STATUS     VARCHAR                     NOT NULL,
    CONSTRAINT PK_BOOKING PRIMARY KEY(ID),
    CONSTRAINT FK_BOOKING_ITEM FOREIGN KEY (ITEM_ID) references ITEMS (ID) on delete cascade,
    CONSTRAINT FK_BOOKING_USER FOREIGN KEY (BOOKER_ID) references USERS (ID) on delete cascade
);

DROP TABLE IF EXISTS COMMENTS CASCADE;
CREATE TABLE IF NOT EXISTS COMMENTS
(
    ID        BIGINT                      NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    TEXT      VARCHAR                     NOT NULL,
    ITEM_ID   BIGINT                      NOT NULL,
    AUTHOR_ID BIGINT                      NOT NULL,
    CREATED   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT PK_COMMENT PRIMARY KEY(ID),
    CONSTRAINT FK_COMMENT_ITEM FOREIGN KEY (ITEM_ID) references ITEMS (ID) on delete cascade,
    CONSTRAINT FK_COMMENT_USER FOREIGN KEY (AUTHOR_ID) references USERS (ID) on delete cascade
);