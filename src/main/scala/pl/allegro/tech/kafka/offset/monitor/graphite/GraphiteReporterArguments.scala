package pl.allegro.tech.kafka.offset.monitor.graphite

import java.lang.Integer.parseInt

object GraphiteReporterArguments {

  var graphiteHost : String = "localhost"

  var graphitePort : Int = 2003

  var graphitePrefix : String = "stats.kafka.offset.monitor"

  var graphiteReportPeriod : Int = 30

  var metricsCacheExpireSeconds : Int = 600

  def parseArguments(args: String) = {
    val argsMap: Map[String, String] = args.split(",").map(_.split("=", 2)).filter(_.length > 1).map(arg => { arg(0) -> arg(1) }).toMap
    argsMap.get("graphiteHost").foreach(graphiteHost = _)
    argsMap.get("graphitePort").foreach(str => {graphitePort = parseInt(str)})
    argsMap.get("graphitePrefix").foreach(graphitePrefix = _)
    argsMap.get("graphiteReportPeriod").foreach(str => {graphiteReportPeriod = parseInt(str)})
    argsMap.get("metricsCacheExpireSeconds").foreach(str => {metricsCacheExpireSeconds = parseInt(str)})
  }
}
