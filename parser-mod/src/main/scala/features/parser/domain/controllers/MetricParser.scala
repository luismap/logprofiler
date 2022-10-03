package features.parser.domain.controllers

import features.parser.domain.entities.MetricProfile


trait MetricParser {
  def getProfileFromString(line: String): MetricProfile
}
