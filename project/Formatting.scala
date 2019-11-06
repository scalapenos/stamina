import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

object FormattingPlugin extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = SbtScalariform
  override def projectSettings =
    Seq(
      ScalariformKeys.preferences := ScalariformKeys.preferences.value
        .setPreference(AlignParameters, false)
        .setPreference(AlignSingleLineCaseStatements, true)
        .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 90)
        .setPreference(DoubleIndentConstructorArguments, true)
        .setPreference(DoubleIndentMethodDeclaration, true)
        .setPreference(RewriteArrowSymbols, true)
        .setPreference(UseUnicodeArrows, false)
        .setPreference(DanglingCloseParenthesis, Preserve)
        .setPreference(NewlineAtEndOfFile, true)
    )
}
