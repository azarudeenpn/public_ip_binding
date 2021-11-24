import java.io._
import java.net.URL

import scalaj.http.{Http, HttpOptions}

import scala.io.Source


object AppMain {
  /**
   * Main method where the old ip address from file is collected in a variable oldIpAddress to compare with
   * new external ip collected by method ipaddress()
   * @param args: currently no use
   */
  def main(args: Array[String]): Unit = {
    val file = new File("ip_address.txt")
    file.createNewFile()
    val oldIpAddress = Source.fromFile("ip_address.txt").getLines().mkString
    val ip_addr = ipAddress()
    println("old: "+oldIpAddress)
//checking if the ip_addr is new. Initiate API call only when new ip is generated.
    if(ip_addr != oldIpAddress){
      write(ip_addr,file)
      val url = "https://api.digitalocean.com/v2/domains/example.com/records/12345"
      val authToken = Source.fromFile("config.txt").getLines().mkString
      updateInDroplet(url,authToken,ip_addr)
    }else{
      println("NO CHANGE IN IP ADDRESS.. Skipping...")
    }
  }

  /**
   * This collect current external ip address.
   * @return: current external ip address of the system.
   */
  def ipAddress(): String = {
    val myip = new URL("http://checkip.amazonaws.com")
    val in:BufferedReader = new BufferedReader(new InputStreamReader(
      myip.openStream()))
    in.readLine()
  }

  /**
   * Method to write ip address to a file for compare it with generated ip address to find its new or not.
   * @param text: New ip address
   * @param file: filepath to store ip address
   */
  def write(text: String,file: File): Unit ={
    val bufferedWriter = new BufferedWriter(new FileWriter(file))
    bufferedWriter.write(text)
    bufferedWriter.close()
  }

  /**
   * http request to update the ip address in digitalocean droplet.
   * @param url: Api URL
   * @param authToken: Bearer-token
   * @param data: ip address to update
   */
  def updateInDroplet(url: String, authToken: String, data: String): Unit ={
    val response = Http(url)
      .put(s"""{ "data": "$data" }""")
      .header("Content-Type", "application/json")
      .header("Authorization" , "Bearer " + authToken)
      .option(HttpOptions.readTimeout(10000)).asString
    println(response)
  }
}
