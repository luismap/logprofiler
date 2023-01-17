package features.parser.data.models

import features.parser.domain.entities.MetricProfile

/**
 * for spark, easier to create same schema
 * using sql conversion for column names
 *
 * @param session_id
 * @param user_id
 */
case class ImpalaMetricProfile(
                                var session_id: String,
                                var session_type:String,
                                var user_id: String,
                                var sql_statement: String,
                                var start_time: String,
                                var end_time: String,
                                var query_status: String,
                                var query_type: String) extends MetricProfile {
  def clean: ImpalaMetricProfile = {
    this.sql_statement = sql_statement.replaceAll("\\n", " ").trim
    this.user_id = user_id.trim
    ImpalaMetricProfile(session_id,
      session_type,
      this.user_id,
      this.sql_statement,
      start_time,
      end_time,
      query_status,
      query_type)
  }
}