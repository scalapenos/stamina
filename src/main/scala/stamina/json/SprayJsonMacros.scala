package stamina.json

import spray.json._
import scala.reflect.macros.blackbox.Context

trait SprayJsonFormats extends DefaultJsonProtocol with LowPrioritySprayJsonFormats
object SprayJsonFormats extends SprayJsonFormats

trait LowPrioritySprayJsonFormats {
  // TODO: better way to recognize case classes in the type
  //       signature in order to prevent compiler errors when
  //       encountering non-case classes.
  implicit def caseClassRootJsonFormat[T <: Product]: RootJsonFormat[T] = macro SprayJsonMacros.materializeRootJsonFormat[T]
}

object SprayJsonMacros {
  def materializeRootJsonFormat[T: c.WeakTypeTag](c: Context): c.Expr[RootJsonFormat[T]] = {
    import c.universe._

    val tpe = weakTypeOf[T]
    val methodNames = tpe.decls.toList collect {
      case method: MethodSymbol if method.isCaseAccessor ⇒ q"${method.name.toString}"
    }

    if (methodNames.length < 1) {
      c.abort(c.enclosingPosition, s"${tpe} is not a case class. SprayJsonMacros can only generate a RootJsonFormat[T] for case classes!")
    }

    // TODO: generate lazy formats for trees

    c.Expr[RootJsonFormat[T]](q"jsonFormat(${tpe.typeSymbol.companion}, ..$methodNames)")
  }

  def lazyRootFormat[T](format: ⇒ RootJsonFormat[T]) = new RootJsonFormat[T] {
    lazy val delegate = format;
    def write(x: T) = delegate.write(x);
    def read(value: JsValue) = delegate.read(value);
  }
}
