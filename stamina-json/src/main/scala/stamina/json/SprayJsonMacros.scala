package stamina.json

import spray.json._
import scala.reflect.macros.Context

trait SprayJsonMacros extends DefaultJsonProtocol with LowPrioritySprayJsonMacros
object SprayJsonMacros extends SprayJsonMacros

sealed trait LowPrioritySprayJsonMacros {
  implicit def materializeRootJsonFormat[T <: Product]: RootJsonFormat[T] = macro SprayJsonMacroImpls.materializeRootJsonFormat[T]
}

object SprayJsonMacroImpls {
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
    //       --> how to detect the need for laziness? Or just always generate lazy versions?

    c.Expr[RootJsonFormat[T]](q"jsonFormat(${tpe.typeSymbol.companion}, ..$methodNames)")
  }

  private def lazyRootFormat[T](format: ⇒ RootJsonFormat[T]) = new RootJsonFormat[T] {
    lazy val delegate = format;
    def write(x: T) = delegate.write(x);
    def read(value: JsValue) = delegate.read(value);
  }
}
