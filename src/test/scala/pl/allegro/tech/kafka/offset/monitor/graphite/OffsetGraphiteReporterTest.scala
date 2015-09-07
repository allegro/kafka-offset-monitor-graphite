package pl.allegro.tech.kafka.offset.monitor.graphite

import com.quantifind.kafka.OffsetGetter.OffsetInfo
import org.scalatest.{BeforeAndAfter, FlatSpec, GivenWhenThen}

class OffsetGraphiteReporterTest extends FlatSpec with BeforeAndAfter with GivenWhenThen {
  
  val GRAPHITE_PORT = 48213
  
  val graphite = new GraphiteMockServer(GRAPHITE_PORT)
  
  val reporter = new OffsetGraphiteReporter(s"graphiteHost=localhost,graphitePort=$GRAPHITE_PORT,graphitePrefix=offset,metricsCacheExpireSeconds=1,graphiteReportPeriod=1")
  
  before {
    graphite.start()
  }
  
  after {
    graphite.stop()
  }
  
  it should "report metrics to graphite" in {
    Given("offset")
    val offset = OffsetInfo("group", "topic", 0, 10, 11, Option.empty, null, null)
    graphite.expectMetric("offset.topic.group.0.offset", 10)
    graphite.expectMetric("offset.topic.group.0.logSize", 11)
    graphite.expectMetric("offset.topic.group.0.lag", 1)

    When("reporting")
    reporter.report(Array(offset))
    
    Then("expect metrics to be delivered")
    graphite.waitUntilReceived()
  }
  
  it should "escape names of topic and groups" in {
    Given("offset for topic and group with dots")
    val offset = OffsetInfo("escape.group", "escape.topic", 0, 10, 11, Option.empty, null, null)
    
    When("reporting")
    graphite.expectMetric("offset.escape_topic.escape_group.0.offset", 10)
    graphite.expectMetric("offset.escape_topic.escape_group.0.logSize", 11)
    graphite.expectMetric("offset.escape_topic.escape_group.0.lag", 1)
    reporter.report(Array(offset))

    Then("expect metrics to be delivered")
    graphite.waitUntilReceived()
  }
  
  it should "not fail to recreate metrics after cache has expired" in {
    Given("offset")
    val offset = OffsetInfo("expired_group", "expired_topic", 0, 10, 11, Option.empty, null, null)
    reporter.report(Array(offset))
    
    When("waiting for cache to expire and reporting again")
    Thread.sleep(2000)
    graphite.expectMetric("offset.expired_topic.expired_group.0.offset", 10)
    reporter.report(Array(offset))

    Then("expect metrics to be delivered")
    graphite.waitUntilReceived()
  }
}
