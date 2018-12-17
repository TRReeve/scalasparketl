Requirements

- Docker

-Unix System

A concept ETL process utilising spark and scala to process a large file of user info
and join this data with other data from an API request in a scalable manner using Apache Spark. Main advantage being
that it could very easily be changed to use a hadoop cluster or a data stream as its sink to process. Due
due to being a distributed computation framework it can also be expanded to process larger datasets.

the end point for now is a JSON file but could as easily be loaded into a database or a cassandra cache etc.

Main Classes

Main workbook with queries
/home/treeve/virgindatatest/src/main/scala/Main.scala

Also contains outputs of the data if you are unable to run the programme

API requests and JSON transformation
/home/treeve/virgindatatest/src/main/scala/APIutls.scala

Spark functions and configurations
/home/treeve/virgindatatest/src/main/scala/SparkUtils.scala


INSTALLATION

#1.  Build the docker environment

docker build -t virginsparketl:v1

#2. Run the docker environment to compile the packages with sbt and run the process

docker run virginsparketl:v1
