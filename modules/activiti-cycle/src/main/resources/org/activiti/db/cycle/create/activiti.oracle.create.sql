create table CYCLE_CONFIG (
	ID_ NVARCHAR2(64),
    VALUE_ NVARCHAR2(2000),
    REV_ INTEGER,
    primary key (ID_)
);

create table CYCLE_LINK (
	ID_ bigint,
	SOURCE_ARTIFACT_ID_ NVARCHAR2(64),
	SOURCE_ELEMENT_ID_ NVARCHAR2(2000) DEFAULT NULL,
	SOURCE_ELEMENT_NAME_ NVARCHAR2(2000),
	SOURCE_REVISION_ bigint DEFAULT NULL,
	TARGET_ARTIFACT_ID_ NVARCHAR2(2000),
	TARGET_ELEMENT_ID_ NVARCHAR2(2000) DEFAULT NULL,
	TARGET_ELEMENT_NAME_ NVARCHAR2(2000),
	TARGET_REVISION_ bigi nt DEFAULT NULL,
	LINK_TYPE_ NVARCHAR2(2000),
	DESCRIPTION_ NVARCHAR2(2000),
	LINKED_BOTH_WAYS_ boolean,
	primary key (ID_)
);

create table CYCLE_TAG (
	ID_ bigint,
	NAME_ NVARCHAR2(2000),
	ALIAS_ NVARCHAR2(2000),
	primary key(ID_)
);