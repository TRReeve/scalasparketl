import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

class SparkUtils(sessionname: String,host:String) {

  //initialise spark session
  val sparkSession:SparkSession = SparkSession
    .builder
    .master(host)
    .appName(sessionname)
    .getOrCreate()
  sparkSession.sparkContext.setLogLevel("OFF")

  def file_to_sparkview(source_format:String, target_file:String, viewname: String) {

    val newsparksqlcontext = sparkSession.read.format(source_format)
      .option("header", "True")
      .option("inferschema", "True")
      .load(target_file)
      .createOrReplaceTempView(viewname)

    println(s"loaded $target_file to spark view $viewname \n")
  }

  def query_to_sparkview(query:String, viewname: String): Unit = {

    val newContext = sparkSession.sql(query)
      .cache()
      .createOrReplaceTempView(viewname)

    println(s"created view $viewname from $query \n")
  }

  def spark_query(query:String) {

    println(s"Processing spark job from SQL: $query  \n")

    sparkSession.sql(query).show()

  }

}

