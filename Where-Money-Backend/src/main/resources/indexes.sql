-- Range queries use a compound index, not DATE_FORMAT on bill_time.
set @has_idx := (select count(*) from information_schema.statistics where table_schema=database() and table_name='pay_bill' and index_name='idx_book_time');
set @idx_sql := if(@has_idx=0,'create index idx_book_time on pay_bill(book_id,bill_time)','select 1');
prepare idx_stmt from @idx_sql;
execute idx_stmt;
deallocate prepare idx_stmt;

-- Range queries use a compound index, not DATE_FORMAT on bill_time.
set @has_idx := (select count(*) from information_schema.statistics where table_schema=database() and table_name='income_bill' and index_name='idx_book_time');
set @idx_sql := if(@has_idx=0,'create index idx_book_time on income_bill(book_id,bill_time)','select 1');
prepare idx_stmt from @idx_sql;
execute idx_stmt;
deallocate prepare idx_stmt;

-- Range queries use a compound index, not DATE_FORMAT on bill_time.
set @has_idx := (select count(*) from information_schema.statistics where table_schema=database() and table_name='refund_bill' and index_name='idx_book_time');
set @idx_sql := if(@has_idx=0,'create index idx_book_time on refund_bill(book_id,bill_time)','select 1');
prepare idx_stmt from @idx_sql;
execute idx_stmt;
deallocate prepare idx_stmt;

-- Range queries use a compound index, not DATE_FORMAT on bill_time.
set @has_idx := (select count(*) from information_schema.statistics where table_schema=database() and table_name='transfer_bill' and index_name='idx_book_time');
set @idx_sql := if(@has_idx=0,'create index idx_book_time on transfer_bill(book_id,bill_time)','select 1');
prepare idx_stmt from @idx_sql;
execute idx_stmt;
deallocate prepare idx_stmt;
