package pl.allegro.tech.kafka.offset.monitor.graphite

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import com.codahale.metrics.{MetricRegistry, MetricFilter}
import com.codahale.metrics.graphite.{GraphiteReporter, Graphite}
import com.google.common.cache._
import com.quantifind.kafka.OffsetGetter.OffsetInfo
import com.codahale.metrics.Gauge

class OffsetGraphiteReporter (pluginsArgs: String) extends com.quantifind.kafka.offsetapp.OffsetInfoReporter {

  GraphiteReporterArguments.parseArguments(pluginsArgs)

  val metrics : MetricRegistry = new MetricRegistry()

  val graphite : Graphite = new Graphite(new InetSocketAddress(GraphiteReporterArguments.graphiteHost, GraphiteReporterArguments.graphitePort))
  val reporter : GraphiteReporter = GraphiteReporter.forRegistry(metrics)
    .prefixedWith(GraphiteReporterArguments.graphitePrefix)
    .convertRatesTo(TimeUnit.SECONDS)
    .convertDurationsTo(TimeUnit.MILLISECONDS)
    .filter(MetricFilter.ALL)
    .build(graphite)

  reporter.start(GraphiteReporterArguments.graphiteReportPeriod, TimeUnit.SECONDS)

  val removalListener : RemovalListener[String, GaugesValues] = new RemovalListener[String, GaugesValues] {
    override def onRemoval(removalNotification: RemovalNotification[String, GaugesValues]) = {
      metrics.remove(removalNotification.getKey())
    }
  }

  val gauges : LoadingCache[String, GaugesValues] = CacheBuilder.newBuilder()
    .expireAfterAccess(GraphiteReporterArguments.metricsCacheExpireMinutes, TimeUnit.MINUTES)
    .removalListener(removalListener)
    .build(
      new CacheLoader[String, GaugesValues]() {
        def load(key: String): GaugesValues = {
          val values: GaugesValues = new GaugesValues()

          val offsetGauge: Gauge[Long] = new Gauge[Long] {
            override def getValue: Long = {
              values.offset
            }
          }

          val lagGauge: Gauge[Long] = new Gauge[Long] {
            override def getValue: Long = {
              values.lag
            }
          }

          val logSizeGauge: Gauge[Long] = new Gauge[Long] {
            override def getValue: Long = {
              values.logSize
            }
          }

          metrics.register(key + ".offset", offsetGauge)
          metrics.register(key + ".logSize", logSizeGauge)
          metrics.register(key + ".lag", lagGauge)

          values
        }
      }
   )

  override def report(info: scala.IndexedSeq[OffsetInfo]) =  {
    info.foreach(i => {
      val values: GaugesValues = gauges.get(getMetricName(i))
      values.logSize = i.logSize
      values.offset = i.offset
      values.lag = i.lag
    })
  }

  def getMetricName(offsetInfo: OffsetInfo): String = {
    offsetInfo.topic.replace(".", "_") + "." + offsetInfo.group.replace(".", "_") + "." + offsetInfo.partition
  }

}
