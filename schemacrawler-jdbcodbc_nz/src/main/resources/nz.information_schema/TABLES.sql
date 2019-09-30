select   database AS TABLE_CAT,
  SCHEMA AS TABLE_SCHEM,
  TABLENAME AS TABLE_NAME,
  objtype AS TABLE_TYPE,
  description AS REMARKS from _v_table
  where schema like '${schemas}';
  