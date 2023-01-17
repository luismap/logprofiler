ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.10"

val kafkaVersion = "3.2.1"
val circeVersion = "0.14.2"
val sparkVersion = "3.2.0"

ThisBuild / resolvers += "re" at "https://repo.orl.eng.hitachivantara.com/artifactory/pnt-mvn/"

val hadoopVersion = "3.2.2"


lazy val assemblySettingsParserMod = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case _ => MergeStrategy.last
  }
)

lazy val assemblySettingsSpark = Seq(

  assemblyMergeStrategy in assembly := {
    case PathList("org","aopalliance", xs @ _*) => MergeStrategy.last
    case PathList("javax", "inject", xs @ _*) => MergeStrategy.last
    case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
    case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
    case PathList("org", "apache", xs @ _*) => MergeStrategy.last
    case PathList("com", "google", xs @ _*) => MergeStrategy.last
    case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
    case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
    case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
    case "about.html" => MergeStrategy.rename
    case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
    case "META-INF/mailcap" => MergeStrategy.last
    case "META-INF/mimetypes.default" => MergeStrategy.last
    case "plugin.properties" => MergeStrategy.last
    case "log4j.properties" => MergeStrategy.first
    case x => MergeStrategy.first
  }
)

lazy val root = (project in file ("."))
  .settings(
    name := "entrypoint",
    assemblySettingsSpark,
    excludeDependencies ++= Seq(
      ExclusionRule("ch.qos.logback.classic.util.ContextSelectorStaticBinder", "logback-classic")
    ),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion ,
      "org.apache.spark" %% "spark-hive" % sparkVersion,
      "org.apache.spark" %% "spark-streaming" % sparkVersion,
      "org.apache.spark" %% "spark-hadoop-cloud" % sparkVersion,
      "org.apache.hive" % "hive-jdbc" % "3.1.1",
      "com.typesafe" % "config" % "1.4.2",

      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.4",
      "org.apache.hadoop" % "hadoop-common" % hadoopVersion,
      "org.apache.hadoop" % "hadoop-client" % hadoopVersion,
      "org.apache.hadoop" % "hadoop-aws" % hadoopVersion
    ),
  ) dependsOn (parserMod)

lazy val streamSpark = (project in file("stream-spark"))
  //.dependsOn(parserMod)
  .settings(
    assemblySettingsSpark,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion ,
      "org.apache.spark" %% "spark-hive" % sparkVersion,
      "org.apache.spark" %% "spark-streaming" % sparkVersion,
      "org.apache.spark" %% "spark-hadoop-cloud" % sparkVersion,
      "org.apache.hive" % "hive-jdbc" % "3.1.1",
      "com.typesafe" % "config" % "1.4.2",

      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.4",
     "org.apache.hadoop" % "hadoop-common" % hadoopVersion,
     "org.apache.hadoop" % "hadoop-client" % hadoopVersion,
      "org.apache.hadoop" % "hadoop-aws" % hadoopVersion
    )
  )

lazy val parserMod = (project in file("parser-mod"))
  .settings(
    assemblySettingsParserMod,
    libraryDependencies ++= Seq(
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.4",
      "org.apache.thrift" % "libthrift" % "0.16.0",
      "org.apache.impala" % "impala-frontend" % "4.0.0.7.1.8.0-801",
      "commons-codec" % "commons-codec" % "1.15",
      "commons-io" % "commons-io" % "2.8.0"
    )
  )