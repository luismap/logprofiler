package features.parser.data.sources

import features.parser.data.models.ImpalaMetricProfile
import features.parser.data.sources.api.StringSource
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.IOUtils
import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.impala.thrift.TRuntimeProfileTree
import org.apache.thrift.TDeserializer
import org.apache.thrift.protocol.TCompactProtocol

class ImpalaMetricProfileStringSource extends Serializable with StringSource {
  /**
   * decode a string of zlib compressed TRuntimeProfileTree object
   * base64, thrift
   *
   * @param line
   * @return
   */
  override def decode(line: String): ImpalaMetricProfile = {
    import java.io.ByteArrayInputStream
    import java.util.zip.InflaterInputStream


    val data: Array[Byte] = line.split(" ")(2).getBytes

    // Decoding base64 encoded query profile.
    val decodedBytes: Array[Byte] = Base64.decodeBase64(data)

    val in: InflaterInputStream = new InflaterInputStream(new ByteArrayInputStream(decodedBytes))
    val out = new ByteArrayOutputStream();
    IOUtils.copy(in, out)

    val profileTree = new TRuntimeProfileTree()
    val deserializer: TDeserializer = new TDeserializer(new TCompactProtocol.Factory())
    deserializer.deserialize(profileTree, out.toByteArray());

    //a profile tree represents the impala profile structure of the file
    val profileNodeList = profileTree.getNodes()

    val summaryInfo = profileNodeList.get(1).info_strings



    ImpalaMetricProfile(
      summaryInfo.get("Session ID"),
      summaryInfo.get("Session Type"),
      summaryInfo.get("User"),
      summaryInfo.get("Sql Statement"),
      summaryInfo.get("Start Time"),
      summaryInfo.get("End Time"),
      summaryInfo.get("Query Status"),
      summaryInfo.get("Query Type"))
      .clean

  }
}
