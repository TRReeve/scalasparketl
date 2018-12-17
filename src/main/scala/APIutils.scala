import net.liftweb.json.Serialization.write
import net.liftweb.json.{DefaultFormats, parse}


//case classes define a data model for JSON serialization
case class MeasuresList(value: List[Int])

case class DimensionMap (index: Map[String,String],label: Map[String,String])

case class OutputMap (data: List[RecordValue])

case class RecordValue (area: String, population: BigInt, ukpoptotal: BigInt)

class ApiRequest(url: String) {

  implicit val formats = DefaultFormats

  val init_url = url


  def get_json_data(): String = {

    val geturl = scala.io.Source.fromURL(init_url).mkString

    return geturl
  }

  def api_data_to_json(json_string:String, outputfile:String): Unit = {

    //Serialize to json objects and write to ukpopdata file

    val uk_data = parse(json_string)

    //get measures information
    val measures = uk_data.extract[MeasuresList]

    //get uk population total from list of values
    val ukpoptotal = measures.value.filter(_ > 0).sum

    //get dimensions info from json data and serialise JSON to case class template
    val dimensions = (uk_data \ "dimension" \ "geography" \ "category").extract[DimensionMap]

    //swap keys and values for sake of indexing
    val index = dimensions.index.map(_.swap)

    //map together values and dimensions via data index and yield to record
    val region_value_map = OutputMap({for ((value, increment) <- measures.value.zipWithIndex) 
                                      yield RecordValue(dimensions.label(index(increment.toString)), value, ukpoptotal)})

    //write to json
    val map_to_json =write(region_value_map.data)

    //export data to file
    Main.writeToFile(outputfile,map_to_json)

  }
}
