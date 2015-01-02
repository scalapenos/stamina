package stamina

@scala.annotation.implicitNotFound(msg = "Cannot find an implementation of the Encoding type class for ${T}")
trait Encoding[T] {
  def encode(t: T): ByteString
  def decode(bytes: ByteString): T
}
