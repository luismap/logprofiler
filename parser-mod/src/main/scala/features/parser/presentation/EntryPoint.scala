package features.parser.presentation



import org.apache.log4j.Logger

import scala.io.Source


object EntryPoint extends App {

  val file = Source.fromFile("src/main/resources/profile.txt")
  val logger = Logger.getLogger(this.getClass.getName)
  val sessionId = raw"^\s*Session ID:(.*)".r
  for (line <-  file.getLines()) {
    line match {
      case sessionId(id) => logger.info(id)
      case _ =>
    }
  }
}
