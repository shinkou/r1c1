organization := "com.shinkou"
name := "r1c1"
maintainer:= "chun-kwong.wong@gmail.com"
version := "0.1.0"
scalaVersion := "2.12.15"
resolvers ++= Seq(
	// redshift-jdbc42
	"Mulesoft" at "https://repository.mulesoft.org/nexus/content/repositories/public/"
)
libraryDependencies ++= Seq(
	"com.opencsv" % "opencsv" % "5.5.2"
	, "org.slf4j" % "slf4j-api" % "1.7.32"
	, "org.slf4j" % "log4j-over-slf4j" % "1.7.32"
	, "ch.qos.logback" % "logback-classic" % "1.2.7"
	, "org.apache.commons" % "commons-configuration2" % "2.7"
	, "org.postgresql" % "postgresql" % "42.3.1"
	, "mysql" % "mysql-connector-java" % "8.0.27"
	, "org.apache.hive" % "hive-jdbc" % "3.1.2"
		exclude("org.apache.logging.log4j", "log4j-slf4j-impl")
		exclude("log4j", "log4j")
		exclude("org.slf4j", "slf4j-log4j12")
	, "com.amazon.redshift" % "redshift-jdbc42" % "2.1.0.3"
	, "com.jcraft" % "jsch" % "0.1.55"
)
enablePlugins(JavaAppPackaging)
