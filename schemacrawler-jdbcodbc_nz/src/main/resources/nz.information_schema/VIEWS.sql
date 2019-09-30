
select   database AS TABLE_CAT,
  SCHEMA AS TABLE_SCHEM,
  viewname AS TABLE_NAME,
  definition AS VIEW_DEFINITION,
  'UNKNOWN' AS CHECK_OPTION,
  'N' as IS_UPDATABLE
  from _v_view
  where schema like '${schemas}';