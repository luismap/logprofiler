CREATE SCHEMA profiles;

CREATE TABLE IF NOT EXISTS profiles.impala_profile (
  session_id STRING,
  session_type STRING,
  user_id STRING,
  sql_statement STRING,
  start_time STRING,
  end_time STRING,
  query_type STRING,
  start_time_parsed TIMESTAMP,
  end_time_parsed  TIMESTAMP,
  ingestion_ts TIMESTAMP,
  file_name STRING
)
PARTITIONED BY (ingestion_date INT, ingestion_hour INT)
STORED AS PARQUET;

CREATE EXTERNAL TABLE IF NOT EXISTS profiles.impala_profile_txt (
  session_id STRING,
  user_id STRING,
  sql_statement STRING,
  start_time STRING,
  ingestion_ts TIMESTAMP
)
PARTITIONED BY (ingestion_date INT, ingestion_hour INT)
row format delimited fields terminated by '|'
STORED AS TEXTFILE