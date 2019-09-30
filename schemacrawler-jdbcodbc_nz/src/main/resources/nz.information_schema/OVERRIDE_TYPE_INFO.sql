
SELECT
 max(case when  strpos(format_type, '(')>0 then substr(format_type,1, strpos(format_type, '(')-1) else format_type end) AS TYPE_NAME, atttypid AS DATA_TYPE, 38 AS "PRECISION",
 NULL AS LITERAL_PREFIX,
max(case when  strpos(format_type, 'CHAR')>0 then '''' else null end) AS LITERAL_SUFFIX, NULL AS CREATE_PARAMS,
 1 AS NULLABLE, 0 AS CASE_SENSITIVE, 3 AS SEARCHABLE,
 0 AS UNSIGNED_ATTRIBUTE, 1 AS FIXED_PREC_SCALE, 0 AS AUTO_INCREMENT,
  max(case when  strpos(format_type, '(')>0 then substr(format_type,1, strpos(format_type, '(')-1) else format_type end) AS LOCAL_TYPE_NAME, -84 AS MINIMUM_SCALE, 127 AS MAXIMUM_SCALE,
 NULL AS SQL_DATA_TYPE, NULL AS SQL_DATETIME_SUB, 10 AS NUM_PREC_RADIX
 from 
 _V_RELATION_COLUMN
 group by atttypid;