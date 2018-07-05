package csw.acceptance.runner

import java.net.URLClassLoader
import java.util.jar.JarFile

import org.scalatest.tools.Runner

import scala.collection.JavaConverters._

class AcceptanceTestRunner(testProjectName: String) {

  private val testJarRunpath =
    getClass.getClassLoader
      .asInstanceOf[URLClassLoader]
      .getURLs
      .map(_.getPath)
      .find(x ⇒ x.contains(testProjectName) && x.contains("tests.jar"))
      .getOrElse("")

  def run(args: Array[String]): Unit = {
    val scalaTestParams = generateScalaTestParams
    val javaTestParams  = generateJavaTestParams

    var javaTestsResult: Boolean  = true
    var scalaTestsResult: Boolean = true

    if (args.length == 0) {
      scalaTestsResult = runTests("scala", scalaTestParams)
      javaTestsResult = runTests("java", javaTestParams)
    } else if (args.length == 1 && (args(0) == "-java" || args(0) == "-scala")) {
      if (args(0) == "-java") javaTestsResult = runTests("java", javaTestParams)
      if (args(0) == "-scala") scalaTestsResult = runTests("scala", scalaTestParams)
    } else {
      println(
        "Invalid arguments provided, supported arguments are: \n" +
        "1. No Arguments: Runs scala and java tests\n" +
        "2. -java: Runs java tests\n" +
        "3. -scala: Runs scala tests\n"
      )
      System.exit(1)
    }

    if (javaTestsResult && scalaTestsResult) System.exit(0)
    else System.exit(1)
  }

  private def generateScalaTestParams: Array[String] = {
    Array(
      "-o",
      "-l",
      "csw.commons.tags.FileSystemSensitive",
      "-l",
      "csw.commons.tags.LoggingSystemSensitive",
      "-R",
      testJarRunpath
    )
  }

  private def generateJavaTestParams: Array[String] = {
    val junitTests = new JarFile(testJarRunpath)
      .stream()
      .iterator()
      .asScala
      .toArray
      .filter(_.getName.endsWith(".class"))
      .map(entry ⇒ entry.getName.substring(0, entry.getName.length - 6).replace('/', '.'))
      .filter(x ⇒ x.split("\\.").last.matches("J[A-Z].*") && x.endsWith("Test"))
      .filterNot(x ⇒ x.endsWith("JEventPublisherTest") || x.endsWith("JEventSubscriberTest") || x.endsWith("JEventSubscriptionFrequencyTest")) // these are testng tests and are run as a part of scala tests.
      .flatMap(x ⇒ Array("-j", x))

    if (junitTests.isEmpty) Array.empty[String]
    else
      Array(
        "-o",
        "-l",
        "csw.commons.tags.FileSystemSensitive",
        "-l",
        "csw.commons.tags.LoggingSystemSensitive"
      ) ++
      junitTests ++
      Array("-R", testJarRunpath)
  }

  private def printReport(msg: String): Unit = {
    println("=" * 100)
    println(s"Running $msg tests from jar: [$testJarRunpath]")
    println("=" * 100)
  }

  private def runTests(msg: String, params: Array[String]): Boolean = {
    if (!params.isEmpty) {
      printReport(msg)
      Runner.run(params)
    } else true
  }
}
