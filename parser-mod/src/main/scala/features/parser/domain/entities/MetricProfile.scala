package features.parser.domain.entities

abstract class MetricProfile {
  var session_id: String
  var session_type: String
  var user_id: String
  var sql_statement: String
  var start_time: String
  var end_time: String
  var query_status: String
  var query_type: String
}
