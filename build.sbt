organization := "org.zouzias"
name := "spark-lucenerdd-examples"
scalaVersion := "2.11.11"
val sparkV = "2.2.0"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

resolvers += "Apache Repos" at "https://repository.apache.org/content/repositories/releases"
resolvers += "OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
	"org.zouzias" %% "spark-lucenerdd" % version.value,
	"org.apache.spark" %% "spark-core" % sparkV % "provided",
	"org.apache.spark" %% "spark-sql" % sparkV % "provided" ,
	"com.holdenkarau"  %% "spark-testing-base" % s"2.2.0_0.7.3" % "test" intransitive()
)

enablePlugins(DockerPlugin)

mainClass in assembly := Some("org.zouzias.spark.lucenerdd.examples.wikipedia.WikipediaSearchExample")


dockerfile in docker := {
	// The assembly task generates a fat JAR file
	val artifact: File = assembly.value
	val artifactTargetPath = s"/app/${artifact.name}"

	new Dockerfile {
		from("java")
		add(artifact, artifactTargetPath)
		entryPoint("java", "-jar", artifactTargetPath)
		expose(8299)
	}
}
