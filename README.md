Requirements
- Docker
-Unix System

A concept ETL process utilising spark and scala to process a large file of user info
and join this data with other data from an API request in a scalable manner using Apache Spark. Main advantage being
that it could very easily be changed to use a hadoop cluster or a data stream as its sink to process. Due
due to being a distributed computation framework it can also be expanded to process larger datasets.

the end point for now is a JSON file but could as easily be loaded into a database or a cassandra cache etc.

Main Classes

#main workbook with queries
/home/treeve/virgindatatest/src/main/scala/Main.scala

Also contains outputs of the data if you are unable to run the programme

#API requests and JSON transformation
/home/treeve/virgindatatest/src/main/scala/APIutls.scala

Spark functions and configurations
/home/treeve/virgindatatest/src/main/scala/SparkUtils.scala


#INSTALLATION

#1.  Build the docker environment

docker build -t virginsparketl:v1

#2. Run the docker environment to compile the packages with sbt and run the process

docker run virginsparketl:v1



#PROGRAMME OUTPUT/ANSWERS (Unless ONS changes their data):


"""
+----------+
|unique_ids|
+----------+
|    612796|
+----------+

+-------+------+
| gender| users|
+-------+------+
| Female|121053|
|  Other|  1546|
|Missing|369144|
|   Male|131955|
|  Total|612796|
+-------+------+

+--------------------+------+
|              region| users|
+--------------------+------+
|          North West| 25772|
|                null|451966|
|               Wales|  3991|
|          South East| 20607|
|              London| 26507|
|     Channel Islands|    65|
|          South West|  9108|
|                East| 13709|
|       East Midlands| 11021|
|       West Midlands| 15413|
|Yorkshire and the...| 16095|
|          North East| 10861|
|            Scotland| 15076|
|    Northern Ireland|  1432|
|               Total|612796|
+--------------------+------+


+--------------------+------------+------------------+--------------+------------------+------------------+
|              region|red_male_pop| red_males_percent|ons_ukpoptotal|  uk_males_percent|   index_red_vs_uk|
+--------------------+------------+------------------+--------------+------------------+------------------+
|          South West|        9108| 6.902353074911901|       2734200| 8.391827314105772| 82.25089502628084|
|          South East|       20607|15.616687507104695|       4474400|13.732862312279593| 113.7176442316809|
|       West Midlands|       15413|11.680497139176234|       2904300|  8.91389952028286|  131.036894824742|
|          North East|       10861| 8.230836269940509|       1297900| 3.983524493810943|206.62195708168633|
|                East|       13709|10.389147815543177|       3040300| 9.331311748619624|111.33641330845086|
|              London|       26507|20.087908756773142|       4398800|13.500830220645332| 148.7901738520859|
|       East Midlands|       11021| 8.352089727558637|       2359400| 7.241488320130625|115.33664570500859|
|Yorkshire and the...|       16095|12.197340002273501|       2690500| 8.257702943676971|147.70863138898886|
|    Northern Ireland|        1432|1.0852184456822402|        920200| 2.824284797908028| 38.42454013441105|
|            Scotland|       15076|11.425107044068053|       2640300| 8.103628724099725|140.98754314953302|
|          North West|       25772|19.530900685839867|       3581200|10.991446118526657|177.69182030487792|
|               Wales|        3991| 3.024515933462165|       1540200|4.7271934859138725|  63.9812172375563|
+--------------------+------------+------------------+--------------+------------------+------------------+

"""
