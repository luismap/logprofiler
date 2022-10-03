package features.parser.domain.usecases

import features.parser.data.controllers.ImpalaMetricParser
import features.parser.data.models.ImpalaMetricProfile
import features.parser.data.sources.ImpalaMetricProfileStringSource

object GetImpalaProfile {
  def getFromString(line: String): ImpalaMetricProfile = {
    val impalaMetricProfileStringSource = new ImpalaMetricProfileStringSource
    new ImpalaMetricParser(impalaMetricProfileStringSource).getProfileFromString(line)
  }
}
