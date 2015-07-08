kafka-offset-monitor-graphite
===========
Plugin to [KafkaOffsetMonitor](https://github.com/quantifind/KafkaOffsetMonitor) tool reporting offset data to graphite via [dropwizard metrics](https://github.com/dropwizard/metrics).


Building It
===========
Currently KafkaOffsetMonitor is not available via public artifact repository, so before we build the plugin we need to build KafkaOffsetMonitor and publish it to maven local repo:

```
sbt publishM2
```

Now we can build the plugin:

```
sbt assembly
```

Running It
===========
Check how to run KafkaOffsetMonitor and modify the command by adding a plugin assembly jar file to the classpath, and put graphite configuration properties into a pluginsArgs argument.

See original KafkaOffsetMonitor example command modified with graphite reporter plugin usage:

```
java -cp "KafkaOffsetMonitor-assembly-0.3.0-SNAPSHOT.jar:kafka-offset-monitor-graphite-assembly-0.1.0-SNAPSHOT.jar" \
     com.quantifind.kafka.offsetapp.OffsetGetterWeb \
     --zk zk-server1,zk-server2 \
     --port 8080 \
     --refresh 10.seconds \
     --retain 2.days \
     --pluginsArgs graphiteHost=graphite.host,graphitePort=2003,graphitePrefix=stats.kafka.offset_monitor
```

The pluginArgs used by kafka-offset-monitor-graphite are:

- **graphiteHost** Graphite host (default localhost)
- **graphitePort** Graphite reporting port (default 2003)
- **graphitePrefix** Metrics prefix (default stats.kafka.offset.monitor)
- **graphiteReportPeriod** Reporting period in seconds (default 30)
- **metricsCacheExpireMinutes** Metrics cache TTL in mires (default 10). Offset metrics are stored in expiring cache and reported to Graphite periodically. If metrics are not updated they will be removed.
