name := "als-spark"

version := "0.1"

scalaVersion := "2.11.12"

lazy val versions = new {
  val spark = "2.3.0"
  val breeze = "0.11.2"
  val akka = "2.5.12"
  val akkaHttp = "10.1.3"
  val shc = "1.1.1-2.1-s_2.11"
  val config = "1.2.1"
  val slf4j = "1.0.2"
}

val providedScope = Seq(
  "org.apache.spark" %% "spark-sql" % versions.spark,
  "org.apache.spark" %% "spark-hive" % versions.spark,
  "org.apache.spark" %% "spark-mllib" % versions.spark
)

libraryDependencies ++= providedScope //.map(_ % "provided")
libraryDependencies ++= Seq(

  // breeze for fast calculation
  "org.scalanlp" %% "breeze" % versions.breeze,
  "org.scalanlp" %% "breeze-natives" % versions.breeze,

  // spark hbase connector
  "com.hortonworks" % "shc-core" % versions.shc,

  // common
  "com.typesafe" % "config" % versions.config,
  "org.clapper" %% "grizzled-slf4j" % versions.slf4j,

  // web layer
  "com.typesafe.akka" %% "akka-http" % versions.akkaHttp,
  "com.typesafe.akka" %% "akka-http-spray-json" % versions.akkaHttp,
  "com.typesafe.akka" %% "akka-stream" % versions.akka
)


assemblyJarName in assembly := s"${name.value}.jar"
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case PathList("google", "protobuf", xs@_*) => MergeStrategy.first
  case "application.conf" => MergeStrategy.concat
  case "log4j.properties" => MergeStrategy.concat
  case x =>
    val baseStrategy = (assemblyMergeStrategy in assembly).value
    baseStrategy(x)
}
test in assembly := {}


//mergeStrategy in assembly := {
//  case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
//  case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
//  case "reference.conf" => MergeStrategy.concat
//  case _ => MergeStrategy.first
//}