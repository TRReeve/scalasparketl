/*
1. Creates a spark session and combines data to core dataframe in Spark Analytics
2. Makes API request for UK population data
3. Combines data and exports to external documents.
 */
import java.io.{File, PrintWriter}
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.{writePretty}

case class OutputReportRow(region: Any, red_male_pop:Any,percentage_of_males_country: Any,uk_male_pop:Any,uk_male_percentage:Any,index_red_vs_uk:Any)

object Main extends App {

  //Json serialisation defaults
  implicit val formats = DefaultFormats

  def writeToFile(p: String, s: String): Unit = {
    val pw = new PrintWriter(new File(p))
    try pw.write(s) finally pw.close()
  }

  println("--INITALISING SPARK CLUSTER AND RUNNING JOB--")

  val spark_etl = new SparkUtils("etl_exercise","local")

  //count of unique users saved as programme variable
  val count_unique_uk_users = spark_etl.sparkSession.sqlContext.read.csv("dummyUsers.csv").select("_c0").where("_c2 = 'Male' or _c2 = 'M'").distinct().count()

  //load user info and clean addresses
  val dummyusersview:String = "userinfo"
  spark_etl.file_to_sparkview("csv","dummyUsers.csv",dummyusersview)
  spark_etl.query_to_sparkview(s"Select *,CASE WHEN postcodeDistrict != 'Missing' THEN regexp_replace(substring(postcodeDistrict,0,2),'[0-9]','') END as postcode_area from $dummyusersview",dummyusersview)

  //load geo info and clean region names
  val geomappingview:String = "postcodeareatoregion"
  spark_etl.file_to_sparkview("csv","postcode_area_to_region.csv",geomappingview)
  spark_etl.query_to_sparkview(
    s"""Select *,CASE WHEN Region LIKE '%of England%' THEN regexp_replace(Region,' of England','')
       |ELSE regexp_replace(Region,' England','') END as region_cleaned
       |from $geomappingview""".stripMargin,geomappingview)

  //join user info and geo info and clean user gender info
  val joinedusersview:String = "usersframe"
  spark_etl.query_to_sparkview(query =
    s"""Select userid, postcodeDistrict as district_postcode,
      |CASE WHEN gender = 'F' THEN 'Female'
      |WHEN gender = 'M'
      |THEN 'Male'
      |ELSE gender END as gender,
      |user.postcode_area,
      |region_cleaned as region
      |FROM $dummyusersview user
      |LEFT JOIN $geomappingview geo
      |ON user.postcode_area = geo.postcode_area""".stripMargin, joinedusersview)



  //Question 1: How many Red user IDs are contained in dummyUsers.csv?
  spark_etl.spark_query(query = s"Select count(distinct userid) as unique_ids FROM $joinedusersview")

  //Count the number of users by gender
  spark_etl.spark_query(query = s"""Select gender,
        count(distinct userid) as users FROM $joinedusersview Group By 1 UNION ALL Select
        'Total', count(distinct userid) from $joinedusersview""")


  //users by region
  spark_etl.spark_query(s"""Select region,count(distinct userid) as users FROM $joinedusersview GROUP BY 1 UNION ALL Select 'Total',count(distinct userid) FROM $joinedusersview Group By 1""")

  //target url for api call
  val apiurl ="""https://www.nomisweb.co.uk/api/v01/dataset/NM_31_1.jsonstat.json?geography=2092957697TYPE480&sex=5&time=2017&measures=20100&age=0&freq=A&select=geography_name,sex_name,obs_value&rows=geography_name&cols=sex_name"""

  println(s"getting api data from $apiurl")

  //initalise API class, map data and dump data to .json
  val apisession = new ApiRequest(apiurl)

  //make api request and transform
  val apidata = apisession.get_json_data()

  println(apidata)

  val apioutputfile = "ukpopdata.json"

  //transform raw json request to .Json file
  apisession.api_data_to_json(apidata,apioutputfile)

  println("\n Exercise two completed, combining data for exercise 3")

  //join with new data from api and clean to final view
  val kpis_source_table= "kpis_source_table"
  val kpis_view = "kpis_view"

  spark_etl.file_to_sparkview("json",apioutputfile,kpis_source_table)

  //querying population data view
  spark_etl.spark_query(s"Select * FROM $kpis_source_table")

  spark_etl.query_to_sparkview(s"""Select
                                 COALESCE(vu.region,ukpop.area) as region,
                                 count(distinct userid) as red_male_pop,
                                 ukpop.population as ons_regional_population,
                                 '$count_unique_uk_users' as red_users_total,
                                 ukpop.ukpoptotal as ons_ukpoptotal
                                 from $kpis_source_table ukpop
                                 LEFT JOIN $joinedusersview vu
                                 ON lower(vu.region) = lower(ukpop.area)
                                 Group By 1,3,4,5""",kpis_source_table)

  //calculate kpis
  spark_etl.query_to_sparkview(
    s"""Select
      |*,
      |CASE WHEN red_males_percent >0 AND uk_males_percent > 0
      |THEN(red_males_percent/uk_males_percent) * 100 END as index_red_vs_uk
      |FROM
      |(
      |
      |Select region
      |, red_male_pop,
      |CASE WHEN red_male_pop > 0 THEN (red_male_pop / red_users_total) * 100 END as red_males_percent,
      |ons_regional_population ons_ukpoptotal,
      |CASE WHEN ons_regional_population > 0 THEN (ons_regional_population / ons_ukpoptotal) * 100 END as uk_males_percent
      |from $kpis_source_table)sub1""".stripMargin, kpis_view)

  //show final kpis table
  spark_etl.spark_query(s"Select * FROM $kpis_view")

  //collect data to local node to load to file
  val business_view = spark_etl.sparkSession.sql(s"Select * FROM $kpis_view").collect()

  //map kpis view to array and serialize to OutputReportRow
  val view_to_json = for (x <- business_view) yield OutputReportRow(x(0),x(1),x(2),x(3),x(4),x(5))

  //serialize as json
  val map_to_json = writePretty(view_to_json)

  //write to file
  writeToFile("kpi_report.json",map_to_json)

  println("wrote data to kpi_report.json")

  //tear-down session
  spark_etl.sparkSession.close()

  println("--JOB COMPLETE, SPARK CLUSTER SHUTDOWN--")

}
