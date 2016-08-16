package org.zouzias.spark.lucenerdd.examples.shape

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.zouzias.spark.lucenerdd.LuceneRDD
import org.zouzias.spark.lucenerdd.spatial.shape.ShapeLuceneRDD


object ShapeLinkageExample {

  def main(): Unit = {
    // initialise spark context
    val conf = new SparkConf().setAppName("ShapeLinkageExample")

    //
    implicit val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    // Load all countries
    val allCountries = sqlContext.read.parquet("data/countries-poly.parquet").select("name", "shape").map(row => (row.getString(1), row.getString(0)))

    // Load all cities
    val citiesPoint = sqlContext.read.parquet("data/world-cities-points.parquet").select("city", "shape").map(row => (row.getString(1), row.getString(0)))

    def parseDouble(s: String): Double = try { s.toDouble } catch { case _: Throwable => 0.0 }

    def coords(city: (String, String)): (Double, Double) = {
      val str = city._1
      val nums = str.dropWhile(x => x.compareTo('(') != 0).drop(1).dropRight(1)
      val coords = nums.split(" ").map(_.trim)
      (parseDouble(coords(0)), parseDouble(coords(1)))
    }

    val shapes = ShapeLuceneRDD(allCountries)
    shapes.cache


    val linked = shapes.linkByKnn(citiesPoint, coords, 3)
    linked.cache

    linked.map(x => (x._1, x._2.head.doc.textField("_1"))).foreach(println)

    // terminate spark context
    sc.stop()

  }
}
