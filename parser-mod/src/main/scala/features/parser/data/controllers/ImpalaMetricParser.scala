package features.parser.data.controllers

import features.parser.data.models.ImpalaMetricProfile
import features.parser.data.sources.ImpalaMetricProfileStringSource
import features.parser.domain.controllers.MetricParser

class ImpalaMetricParser(
                        stringSource: ImpalaMetricProfileStringSource
                        ) extends MetricParser{
  override def getProfileFromString(line: String): ImpalaMetricProfile = {
    stringSource.decode(line)
  }



}
