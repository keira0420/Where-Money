create table if not exists cache_eviction_task
(
    id bigint auto_increment primary key,
    cache_key varchar(255) not null,
    attempts int not null default 0,
    next_retry timestamp not null default CURRENT_TIMESTAMP,
    create_time timestamp not null default CURRENT_TIMESTAMP,
    index idx_cache_retry (next_retry,id)
);

create table if not exists ai_bill_confirmation
(
    user_id int not null,
    request_id varchar(36) not null,
    payload_hash varchar(32) not null,
    saved tinyint not null default 1,
    create_time timestamp default CURRENT_TIMESTAMP not null,
    primary key (user_id, request_id)
);

set @has_saved := (select count(*) from information_schema.columns where table_schema=database() and table_name='ai_bill_confirmation' and column_name='saved');
set @migration := if(@has_saved=0,'alter table ai_bill_confirmation add column saved tinyint not null default 1','select 1');
prepare migration_stmt from @migration;
execute migration_stmt;
deallocate prepare migration_stmt;

create table if not exists ai_report_log
(
    id bigint auto_increment primary key,
    user_id int not null,
    book_id int not null,
    report_month varchar(7) not null,
    source varchar(16) not null,
    latency_ms bigint not null,
    create_time timestamp default CURRENT_TIMESTAMP not null,
    index idx_report_user_time (user_id, create_time)
);

create table if not exists budget
(
    id               int auto_increment
        primary key,
    book_id          int                         not null,
    bill_category_id int                         not null,
    used             decimal(10, 2) default 0.00 null,
    times            int            default 0    null,
    limit_amount     decimal(10, 2)              null
);

create table if not exists user
(
    id              int auto_increment
        primary key,
    user_name       varchar(255)                        not null,
    hashed_password varchar(255)                        not null,
    is_active       tinyint(1)                          not null,
    create_time     timestamp default CURRENT_TIMESTAMP not null,
    constraint user_id_uindex
        unique (id),
    constraint user_user_name_uindex
        unique (user_name)
);

create table if not exists asset
(
    id         int auto_increment
        primary key,
    user_id    int                                         not null,
    type       enum ('资金', '信用卡', '充值', '投资理财') not null,
    balance    decimal(10, 2)                              not null,
    asset_name varchar(255)                                not null,
    bill_date  int                                         null,
    repay_date int                                         null,
    quota      decimal(10, 2)                              null,
    in_total   tinyint(1)                                  not null,
    svg        varchar(255)                                null,
    constraint asset_id_uindex
        unique (id),
    constraint asset_ibfk_1
        foreign key (user_id) references user (id)
);

set @idx_exists := (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'asset'
      and index_name = 'user_id'
);
set @stmt := if(@idx_exists = 0, 'create index user_id on asset (user_id)', 'select 1');
prepare stmt from @stmt;
execute stmt;
deallocate prepare stmt;

create table if not exists book
(
    id           int auto_increment
        primary key,
    user_id      int                                      not null,
    title        varchar(255)                             not null,
    create_time  timestamp      default CURRENT_TIMESTAMP not null,
    begin_date   int            default 1                 not null,
    total_budget decimal(10, 2)                           null,
    used_budget  decimal(10, 2) default 0.00              null,
    constraint book_id_uindex
        unique (id),
    constraint book_ibfk_1
        foreign key (user_id) references user (id)
);

create table if not exists bill_category
(
    id                 int auto_increment
        primary key,
    book_id            int                           null,
    bill_category_name varchar(255)                  not null,
    svg                text                          not null,
    type               enum ('收入', '支出', '转账') not null,
    constraint bill_category_id_uindex
        unique (id),
    constraint bill_category_ibfk_1
        foreign key (book_id) references book (id)
);

create table if not exists income_bill
(
    id                 int auto_increment
        primary key,
    book_id            int            not null,
    income_asset_id    int            not null,
    bill_category_id   int            not null,
    amount             decimal(10, 2) not null,
    bill_time          timestamp      null,
    remark             varchar(255)   null,
    image              mediumblob     null,
    image_content_type varchar(63)    null,
    constraint income_bill_id_uindex
        unique (id),
    constraint income_bill_ibfk_1
        foreign key (book_id) references book (id),
    constraint income_bill_ibfk_2
        foreign key (income_asset_id) references asset (id),
    constraint income_bill_ibfk_3
        foreign key (bill_category_id) references bill_category (id)
);

set @idx_exists := (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'income_bill'
      and index_name = 'bill_category_id'
);
set @stmt := if(@idx_exists = 0, 'create index bill_category_id on income_bill (bill_category_id)', 'select 1');
prepare stmt from @stmt;
execute stmt;
deallocate prepare stmt;

set @idx_exists := (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'income_bill'
      and index_name = 'book_id'
);
set @stmt := if(@idx_exists = 0, 'create index book_id on income_bill (book_id)', 'select 1');
prepare stmt from @stmt;
execute stmt;
deallocate prepare stmt;

set @idx_exists := (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'income_bill'
      and index_name = 'income_asset_id'
);
set @stmt := if(@idx_exists = 0, 'create index income_asset_id on income_bill (income_asset_id)', 'select 1');
prepare stmt from @stmt;
execute stmt;
deallocate prepare stmt;

create table if not exists pay_bill
(
    id                 int auto_increment
        primary key,
    book_id            int                  not null,
    pay_asset_id       int                  not null,
    bill_category_id   int                  not null,
    amount             decimal(10, 2)       not null,
    bill_time          timestamp            null,
    remark             varchar(255)         null,
    refunded           tinyint(1) default 0 not null,
    image              mediumblob           null,
    image_content_type varchar(63)          null,
    constraint pay_bill_id_uindex
        unique (id),
    constraint pay_bill_ibfk_1
        foreign key (book_id) references book (id),
    constraint pay_bill_ibfk_2
        foreign key (pay_asset_id) references asset (id),
    constraint pay_bill_ibfk_3
        foreign key (bill_category_id) references bill_category (id)
)
    comment '支出账单';

set @idx_exists := (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'pay_bill'
      and index_name = 'bill_category_id'
);
set @stmt := if(@idx_exists = 0, 'create index bill_category_id on pay_bill (bill_category_id)', 'select 1');
prepare stmt from @stmt;
execute stmt;
deallocate prepare stmt;

set @idx_exists := (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'pay_bill'
      and index_name = 'book_id'
);
set @stmt := if(@idx_exists = 0, 'create index book_id on pay_bill (book_id)', 'select 1');
prepare stmt from @stmt;
execute stmt;
deallocate prepare stmt;

set @idx_exists := (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'pay_bill'
      and index_name = 'pay_asset_id'
);
set @stmt := if(@idx_exists = 0, 'create index pay_asset_id on pay_bill (pay_asset_id)', 'select 1');
prepare stmt from @stmt;
execute stmt;
deallocate prepare stmt;

create table if not exists refund_bill
(
    id                 int auto_increment
        primary key,
    book_id            int            not null,
    pay_bill_id        int            not null,
    refund_asset_id    int            not null,
    amount             decimal(10, 2) not null,
    bill_time          timestamp      null,
    remark             varchar(255)   null,
    image              mediumblob     null,
    image_content_type varchar(63)    null,
    constraint refund_bill_id_uindex
        unique (id),
    constraint refund_bill_ibfk_1
        foreign key (book_id) references book (id),
    constraint refund_bill_ibfk_2
        foreign key (pay_bill_id) references pay_bill (id),
    constraint refund_bill_ibfk_3
        foreign key (refund_asset_id) references asset (id)
);

set @idx_exists := (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'refund_bill'
      and index_name = 'book_id'
);
set @stmt := if(@idx_exists = 0, 'create index book_id on refund_bill (book_id)', 'select 1');
prepare stmt from @stmt;
execute stmt;
deallocate prepare stmt;

set @idx_exists := (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'refund_bill'
      and index_name = 'pay_bill_id'
);
set @stmt := if(@idx_exists = 0, 'create index pay_bill_id on refund_bill (pay_bill_id)', 'select 1');
prepare stmt from @stmt;
execute stmt;
deallocate prepare stmt;

set @idx_exists := (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'refund_bill'
      and index_name = 'refund_asset_id'
);
set @stmt := if(@idx_exists = 0, 'create index refund_asset_id on refund_bill (refund_asset_id)', 'select 1');
prepare stmt from @stmt;
execute stmt;
deallocate prepare stmt;

create table if not exists transfer_bill
(
    id                 int auto_increment
        primary key,
    book_id            int            not null,
    in_asset_id        int            null,
    out_asset_id       int            null,
    amount             decimal(10, 2) not null,
    transfer_fee       decimal(10, 2) null,
    bill_time          timestamp      null,
    remark             varchar(255)   null,
    image              mediumblob     null,
    image_content_type varchar(63)    null,
    constraint transfer_bill_id_uindex
        unique (id),
    constraint transfer_bill_ibfk_1
        foreign key (book_id) references book (id),
    constraint transfer_bill_ibfk_2
        foreign key (in_asset_id) references asset (id),
    constraint transfer_bill_ibfk_3
        foreign key (out_asset_id) references asset (id)
);

set @idx_exists := (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'transfer_bill'
      and index_name = 'book_id'
);
set @stmt := if(@idx_exists = 0, 'create index book_id on transfer_bill (book_id)', 'select 1');
prepare stmt from @stmt;
execute stmt;
deallocate prepare stmt;

set @idx_exists := (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'transfer_bill'
      and index_name = 'in_asset_id'
);
set @stmt := if(@idx_exists = 0, 'create index in_asset_id on transfer_bill (in_asset_id)', 'select 1');
prepare stmt from @stmt;
execute stmt;
deallocate prepare stmt;

set @idx_exists := (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'transfer_bill'
      and index_name = 'out_asset_id'
);
set @stmt := if(@idx_exists = 0, 'create index out_asset_id on transfer_bill (out_asset_id)', 'select 1');
prepare stmt from @stmt;
execute stmt;
deallocate prepare stmt;

create table if not exists ai_bill_parse_log
(
    id            int auto_increment
        primary key,
    user_id       int                                      not null,
    book_id       int                                      not null,
    type          enum ('收入', '支出', '转账', '退款') not null,
    input_text    text                                     not null,
    model_name    varchar(127)                             null,
    status        varchar(63)                              not null,
    error_code    varchar(63)                              null,
    error_message varchar(255)                             null,
    llm_raw_json  text                                     null,
    latency_ms    bigint                                   not null,
    create_time   timestamp      default CURRENT_TIMESTAMP not null
);

set @idx_exists := (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'ai_bill_parse_log'
      and index_name = 'user_id'
);
set @stmt := if(@idx_exists = 0, 'create index user_id on ai_bill_parse_log (user_id)', 'select 1');
prepare stmt from @stmt;
execute stmt;
deallocate prepare stmt;

set @idx_exists := (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'ai_bill_parse_log'
      and index_name = 'book_id'
);
set @stmt := if(@idx_exists = 0, 'create index book_id on ai_bill_parse_log (book_id)', 'select 1');
prepare stmt from @stmt;
execute stmt;
deallocate prepare stmt;

set @idx_exists := (
    select count(1)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'ai_bill_parse_log'
      and index_name = 'create_time'
);
set @stmt := if(@idx_exists = 0, 'create index create_time on ai_bill_parse_log (create_time)', 'select 1');
prepare stmt from @stmt;
execute stmt;
deallocate prepare stmt;
