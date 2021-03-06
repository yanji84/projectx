package com.projectx.jobserver

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark._
import org.apache.spark.AccumulatorParam
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.SparkContext._
import org.apache.spark.sql.SQLContext
import scala.util.Try
import spark.jobserver.SparkJob
import spark.jobserver.SparkHiveJob
import spark.jobserver.SparkSqlJob
import spark.jobserver.SparkJobValid
import spark.jobserver.SparkJobInvalid
import java.net.URLDecoder

/**
*
* File Name: sqlRelay.scala
* Date: Nov 09, 2015
* Author: Ji Yan
*
* Jobserver job execute hive sql query
*/

object sqlRelay extends SparkSqlJob {
	override def runJob(sc:SQLContext, config: Config): Any = {
		val input = URLDecoder.decode(URLDecoder.decode(config.getString("input")))
		// this only supports one FROM keyword in the query for prototype
		val tokens = input.split(" ")
		var index = 0
		var datasetNameIndex = -1
		for (t <- tokens) {
			if (t.toLowerCase() == "from") {
				datasetNameIndex = index + 1
			}
			index += 1
		}
		val dataset = tokens(datasetNameIndex)
		val sqlStatement = input
		var table = sc.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("/projectx/datasets/" + dataset + "/*")
		table.registerTempTable(dataset)
		val dataTable = sc.sql(sqlStatement)
		val data = dataTable.collect
		var result = "[["
		for (c <- dataTable.columns) {
			result = result + "\"" + c + "\","
		}
		result = result.dropRight(1) + "],["
		for (r <- data) {
			result = result + "[" + r.toSeq.toArray.map(s => "\"" + s + "\"").mkString(",") + "],"
		}
		result = result.dropRight(1) + "]]"
		result
	}
	override def validate(sc:SQLContext, config: Config): spark.jobserver.SparkJobValidation = {
		Try(config.getString("input")).map(x => SparkJobValid).getOrElse(SparkJobInvalid("No input config param"))
	}
}
