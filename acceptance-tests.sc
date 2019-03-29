import $ivy.`io.get-coursier::coursier:1.1.0-M13-2`
import $ivy.`io.get-coursier::coursier-cache:1.1.0-M13-2`

import java.io.File

import coursier._
import coursier.core.Configuration
import coursier.util.Task.sync
import coursier.util._

import scala.collection.JavaConverters._

@main
def entryPoint(version: String, projects: String*): Unit = {
  val projectNames = if(projects.isEmpty) allProjects else projects.toList
  projectNames.foreach { projectName =>
    new TestModule(projectName, version).run()
  }
}

lazy val allProjects = List(
  "csw-admin-server",
  "csw-location-agent",
  "csw-location-api",
  "csw-location-client",
  "csw-location-server",
  "csw-config-api",
  "csw-config-cli",
  "csw-config-client",
  "csw-config-server",
  "csw-logging-client",
  "csw-framework",
  "csw-params",
  "csw-command-api",
  "csw-command-client",
  "csw-event-api",
  "csw-event-cli",
  "csw-event-client",
  "csw-alarm-api",
  "csw-alarm-cli",
  "csw-alarm-client",
  "csw-database",
  "csw-aas-core",
  "csw-time-core",
  "csw-aas-http",
  "csw-aas-installed",
  "csw-time-scheduler",
  "csw-time-clock",
  "csw-network-utils"
)

class TestModule(projectName: String, version: String) {

  val testArtifact = Dependency(
    Module(
      Organization("com.github.tmtsoftware.csw"),
      ModuleName(s"${projectName}_2.12")
    ),
    version,
    Configuration.test
  )

  val files: Seq[File] = Fetch()
    .allArtifactTypes()
    .addDependencies(testArtifact)
    .addRepositories(Repositories.jitpack)
    .run()

  val classpath: String = files.mkString(":")

  val testJarRunpath: String =
    files
      .map(_.toString)
      .find(x ⇒ x.contains(projectName) && x.contains("tests.jar"))
      .getOrElse("")

  val cmds = List(
    "java",
    "-cp",
    classpath,
    "org.scalatest.tools.Runner",
    "-oDF",
    "-l",
    "csw.commons.tags.FileSystemSensitive",
    "-R",
    testJarRunpath
  )

  def run(): Int = {
    val builder = new ProcessBuilder(cmds.asJava).inheritIO()
    println(s"================================ Running acceptance tests for [$projectName] ================================")
    println(s"Test jar: [$testJarRunpath] ")
    println("===============================================================================================================")
    val process = builder.start()
    process.waitFor()
  }
}
