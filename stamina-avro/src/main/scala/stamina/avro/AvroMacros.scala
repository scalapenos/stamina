package stamina.avro

import scala.reflect.macros.Context

// trait AvroMacros {
//   def avroSchemaFor[T <: Product]: AvroSchema = macro AvroMacroImpls.avroSchemaFor[T]
// }

// object AvroMacros extends AvroMacros

// object AvroMacroImpls {
//   def avroSchemaFor[T: c.WeakTypeTag](c: Context): c.Expr[AvroSchema] = {
//     import c.universe._

//     val tpe = weakTypeOf[T]
//     val methodNames = tpe.declarations.toList collect {
//       case method: MethodSymbol if method.isCaseAccessor ⇒ q"${method.name.toString}"
//     }

//     if (methodNames.length < 1) {
//       c.abort(c.enclosingPosition, s"${tpe} is not a case class. AvroMacros can only generate a RootJsonFormat[T] for case classes!")
//     }

//     // TODO: how to detect recursive types?

//     c.Expr[RootJsonFormat[T]](q"jsonFormat(${tpe.typeSymbol.companionSymbol}, ..$methodNames)")
//   }

//   private def lazyRootFormat[T](format: ⇒ RootJsonFormat[T]) = new RootJsonFormat[T] {
//     lazy val delegate = format;
//     def write(x: T) = delegate.write(x);
//     def read(value: JsValue) = delegate.read(value);
//   }
// }
