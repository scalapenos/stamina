package stamina.json

import spray.json._
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

trait SprayJsonMacros extends DefaultJsonProtocol with LowPrioritySprayJsonMacros
object SprayJsonMacros extends SprayJsonMacros

trait LowPrioritySprayJsonMacros {
  // TODO: better way to recognize case classes in the type
  //       signature in order to prevent compiler errors when
  //       encountering non-case classes.
  implicit def caseClassRootJsonFormat[T <: Product]: RootJsonFormat[T] = macro SprayJsonMacroImplementations.materializeRootJsonFormat[T]
}

object SprayJsonMacroImplementations {
  def materializeRootJsonFormat[T: c.WeakTypeTag](c: Context) = {
    import c.universe._

    val tpe = weakTypeOf[T]
    val methods = tpe.decls.toList collect {
      case method: MethodSymbol if method.isCaseAccessor ⇒ method
    }

    if (methods.length < 1) {
      c.abort(c.enclosingPosition, s"${tpe} is not a case class. SprayJsonMacros can only generate a RootJsonFormat[T] for case classes!")
    }

    val methodNames = methods.map(m ⇒ q"${m.name.toString}")
    val tpeCompanion = tpe.typeSymbol.companion
    val imports = q"import spray.json._"
    val res = q"""
      $imports
      jsonFormat(${tpeCompanion}, ..$methodNames)
    """

    // val methodNames = methods.map(method ⇒ q"${method.name}")
    // val methodNameStrings = methods.map(method ⇒ q"${method.name.toString}")
    // val fieldWriters = methods.map(method ⇒ q"(${method.name.toString}, value.${method.name}.toJson)")
    // val fieldReaders = methods.map(method ⇒ q"${method.name}.convertTo[${method.returnType}]")

    // val res = q"""
    // $imports1
    // $imports2
    // new RootJsonFormat[$tpe] {
    //   def write(value: $tpe): JsValue = {
    //     JsObject(..$fieldWriters)
    //   }
    //   def read(value: JsValue): $tpe = {
    //     value.asJsObject.getFields(..${methodNameStrings}) match {
    //       case Seq(..$methodNames) ⇒ ${tpeCompanion}(..${fieldReaders})
    //       case _ ⇒ throw new DeserializationException("Could not produce an instance of T from the following JSON: " + value)
    //     }
    //   }
    // }
    // """

    // println(showCode(res))

    res
  }
}
