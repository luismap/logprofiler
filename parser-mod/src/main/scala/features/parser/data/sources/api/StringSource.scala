package features.parser.data.sources.api

import features.parser.data.models.ImpalaMetricProfile

trait StringSource {
  /**
   * decode a string of zlib compressed TRuntimeProfileTree object
   * base64, thrift
   * @param line
   * @return
   */
  def decode(line: String): ImpalaMetricProfile
}
