package pl.allegro.tech.kafka.offset.monitor.graphite

import java.io.InputStream
import java.lang
import java.net.ServerSocket
import java.util.concurrent.{Callable, ExecutorService, Executors}

import com.jayway.awaitility.Awaitility._
import com.jayway.awaitility.Duration

class GraphiteMockServer(port: Int) {

  var serverSocket: ServerSocket = null
  val executor: ExecutorService = Executors.newFixedThreadPool(10)
  @volatile var listen: Boolean = false

  var expectedMetrics: scala.collection.mutable.Map[String, Double] = scala.collection.mutable.Map()
  var receivedMetrics: scala.collection.mutable.Map[String, Double] = scala.collection.mutable.Map()
  
  def start() {
    serverSocket = new ServerSocket(port)
    listen = true
    handleConnections()
  }

  private def handleConnections() {
    executor.execute(new Runnable {
      override def run() {
        while(listen) {
            readData(serverSocket.accept().getInputStream())
        }
      }
    })
  }

  private def readData(stream: InputStream) {
    executor.execute(new Runnable {
      override def run() {
        scala.io.Source.fromInputStream(stream).getLines().foreach((line) => handleMetric(line))
      }
    })
  }
  
  private def handleMetric(metricLine: String) {
    val metric = metricLine.split(" ")(0)
    val value = metricLine.split(" ")(1)

    if(expectedMetrics.contains(metric)) {
      receivedMetrics += (metric -> value.toDouble)
    }
  }
  
  def stop() {
    listen = false
    serverSocket.close()
  }
  
  def reset() {
    expectedMetrics.clear()
    receivedMetrics.clear()
  }

  def expectMetric(metricNamePattern: String, value: Double) {
    expectedMetrics += (metricNamePattern -> value)
  }

  def waitUntilReceived() {
    await.atMost(Duration.FIVE_SECONDS).until(new Callable[lang.Boolean] {
      override def call(): lang.Boolean = {
        expectedMetrics.forall { case (k, v) =>
          receivedMetrics.get(k).exists( (rv) => v == rv )
        }
      }
    })
  }
}
