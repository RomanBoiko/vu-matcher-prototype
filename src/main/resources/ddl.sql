create table failed_record (
	id IDENTITY,
	body CLOB,
	source_ref BIGINT,
	created TIMESTAMP default current_timestamp
);

create table record_active (
	id IDENTITY,
	body CLOB,
	source_ref BIGINT,
	created TIMESTAMP default current_timestamp
);

create table record_matched (
	id IDENTITY,
	body CLOB,
	source_ref BIGINT,
	match_ref BIGINT,
	created TIMESTAMP default current_timestamp
);

create table match (
	id IDENTITY,
	name VARCHAR(50),
	created TIMESTAMP default current_timestamp
);

create table exception_active (
	id IDENTITY,
	name VARCHAR(50),
	record_ref BIGINT,
	created TIMESTAMP default current_timestamp
);

create table exception_closed (
	id IDENTITY,
	name VARCHAR(50),
	record_ref BIGINT,
	created TIMESTAMP default current_timestamp
);

create table exception_comments_active (
	exception_ref BIGINT,
	user_ref VARCHAR(50),
	comment CLOB,
	created TIMESTAMP default current_timestamp
);

create table exception_comments_closed (
	exception_ref BIGINT,
	user_ref VARCHAR(50),
	comment CLOB,
	created TIMESTAMP default current_timestamp
);

create table journal (
	entry VARCHAR(2000),
	created TIMESTAMP default current_timestamp
);

create table allocation_group (
	id IDENTITY,
	name VARCHAR(100)
);

create table exception_group_active (
	exception_ref BIGINT,
	group_ref BIGINT,
	created TIMESTAMP default current_timestamp
);

create table exception_group_closed (
	exception_ref BIGINT,
	group_ref BIGINT,
	created TIMESTAMP default current_timestamp
);

create table source (
	id IDENTITY,
	uri VARCHAR(2000),
	created TIMESTAMP default current_timestamp
);