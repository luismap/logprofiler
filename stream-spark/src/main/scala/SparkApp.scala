import com.typesafe.config.ConfigFactory
import org.apache.log4j.{Level, LogManager, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.{col, current_timestamp, date_format, input_file_name}
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.sql.DriverManager

object SparkApp {
  val config = ConfigFactory.load()
  val uid = config.getString("impala.logging.uid")
  val pass = config.getString("impala.logging.password")
  val accessKey = config.getString("aws.fs.s3a.access.key")
  val secretKey = config.getString("aws.fs.s3a.secret.key")
  val endpoint = config.getString("aws.fs.s3a.endpoint")

  val sparkJdbc = s"jdbc:impala://coordinator-lmap-impala.dw-aws-lmap-ireland.a465-9q4k.cloudera.site:443/default;AuthMech=3;transportMode=http;httpPath=cliservice;ssl=1;UID=$uid;PWD=$pass"
  val saveLocation = "s3a://lmap02/data/warehouse/tablespace/external/hive/profiles.db/impala_profile_txt/"
  val luisLocation = ""

    def main(args: Array[String]): Unit = {



      def readJdbc = {

        val JDBCDriver = "com.cloudera.impala.jdbc41.Driver"
        Class.forName(JDBCDriver).newInstance


        val sparkSession = SparkSession
          .builder()
          .master("local[*]")
          .appName("spark-streamer")
          .getOrCreate()

        val sparkCtx = sparkSession.sparkContext
        sparkCtx.setLogLevel("INFO")

        sparkCtx.hadoopConfiguration.set("fs.s3a.access.key", accessKey)
        sparkCtx.hadoopConfiguration.set("fs.s3a.secret.key", secretKey)
        sparkCtx.hadoopConfiguration.set("fs.s3a.endpoint", endpoint)

        def readWithSpark = {
          sparkSession.read
            .format("jdbc")
            .option("url", sparkJdbc)
            .option("driver", JDBCDriver)
            .option("dbtable", "sk_test")
            .load()
            .show()
        }

        val con = DriverManager.getConnection(sparkJdbc)
        val stmt = con.createStatement()
        val rs = stmt.executeQuery("select * from sk_test")

        println("in jdbc")
        Iterator.continually((rs.next(), rs)).takeWhile(
          _._1
        ).foreach(t => println(t._2.getString(1)))

        con.close()

        readWithSpark

      }

      def local = {

        val fileLocation = "s3a://lmap02-gc22-dwx-external/clusters/env-h5gc22/warehouse-1664094301-2qzt/warehouse/tablespace/external/hive/sys.db/logs/dt=2022-09-26/ns=impala-1664172129-q7vh/app=impala-profiles/lmap-impala/2022-09-26-06-50_coordinator-0_76168ada-a3ac-41ce-8d19-fbe2e5dede8b_0.log.gz"
        val sparkSession = SparkSession
          .builder()
          .master("local[*]")
          .appName("spark-streamer")
          .getOrCreate()
        val sparkCtx = sparkSession.sparkContext
        sparkCtx.hadoopConfiguration.set("fs.s3a.access.key", accessKey)
        sparkCtx.hadoopConfiguration.set("fs.s3a.secret.key", secretKey)
        sparkCtx.hadoopConfiguration.set("fs.s3a.endpoint", endpoint)


        val rdd = sparkSession
          .sparkContext
          .textFile(fileLocation)
          //.map(e => GetImpalaProfile.getFromString(e))

        import sparkSession.implicits._


        val df =
          rdd.toDF()
            .withColumn("start_time_parsed", col("start_time").cast("timestamp"))
            .withColumn("end_time_parsed", col("end_time").cast("timestamp"))
            .withColumn("ingestion_ts", current_timestamp().cast("timestamp"))
            .withColumn("ingestion_date", date_format(col("ingestion_ts"), "yyyyMMdd").cast("int"))
            .withColumn("ingestion_hour", date_format(col("ingestion_ts"), "hh").cast("int"))


        // df.show()

        df.printSchema()

        def save() = {
          df
            .coalesce(1)
            .write
            .format("jdbc")
            .option("url", sparkJdbc)
            .mode("append")
            .option("dbtable", "profiles.impala_profile")
            .partitionBy("ingestion_date", "ingestion_hour")
            .save()

        }

        save()
      }

      def streamer = {

        val sparkSession = SparkSession
          .builder()
          .master("local[*]")
          .appName("spark-streamer")
          .getOrCreate()
        val sc = sparkSession.sparkContext
        val ssc = new StreamingContext(sc, Seconds(20))

        val sparkCtx = ssc.sparkContext
        sparkCtx.hadoopConfiguration.set("fs.s3a.access.key", accessKey)
        sparkCtx.hadoopConfiguration.set("fs.s3a.secret.key", secretKey)
        sparkCtx.hadoopConfiguration.set("fs.s3a.endpoint", endpoint)


        import sparkSession.implicits._
        val streamProfile = ssc
          .textFileStream("")

        streamProfile.foreachRDD(
          rdd => {
            val df = rdd
              //.map(e => GetImpalaProfile.getFromString(e))
              .toDF()
              .withColumn("start_time_parsed", col("start_time").cast("timestamp"))
              .withColumn("end_time_parsed", col("end_time").cast("timestamp"))
              .withColumn("file_name", input_file_name())
              .withColumn("ingestion_ts", current_timestamp().cast("timestamp"))
              .withColumn("ingestion_date", date_format(col("ingestion_ts"), "yyyyMMdd").cast("int"))
              .withColumn("ingestion_hour", date_format(col("ingestion_ts"), "HH").cast("int"))

            df.show()
            //if (df.count() > 0) save(df)
          }
        )

        def save(df: DataFrame) = {
          df
            .write
            .format("jdbc")
            .option("url", sparkJdbc)
            .mode("append")
            .option("dbtable", "profiles.impala_profile")
            .partitionBy("ingestion_date", "ingestion_hour")
            .save()

        }

        ssc.start() // Start the computation
        ssc.awaitTermination() // Wait for the computation to terminate
      }

      //local
      readJdbc
      //streamer
    }

  }