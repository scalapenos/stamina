package stamina

import spray.json._

/**
 * Adds some handy sugar around reading/writing json from/to ByteStrings.
 */
package object json {
  implicit class AnyWithJsonByteStringConversion[T](any: T) {
    def toJsonBytes(implicit writer: RootJsonWriter[T]): ByteString = ByteString(writer.write(any).compactPrint)
  }

  implicit class ByteStringWithRootJsonReaderSupport(bytes: ByteString) {
    def fromJsonBytes[T](implicit reader: RootJsonReader[T]): T = {
      reader.read(JsonParser(ParserInput(bytes.toArray)))
    }
  }
}
