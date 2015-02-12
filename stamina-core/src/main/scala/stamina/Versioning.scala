package stamina

/**
 * Superclass of all version classes.
 * Represents a version number as a type.
 */
sealed abstract class Version

/** Type class that supplies relevant information for a specific Version subclass. */
@annotation.implicitNotFound(msg = "Cannot find VersionInfo type class for ${V}")
abstract class VersionInfo[V <: Version](val versionNumber: Int)

/** Type class used to indicate that a Version subclass can be migrated (i.e. it is higher than V1) */
@annotation.implicitNotFound(msg = "Cannot find proof that ${V} is a migratable version (i.e. it is higher than V1)")
sealed trait Migratable[V <: Version]

/** Type class used by the API to enforce two Version subclasses to be sequential. */
@annotation.implicitNotFound(msg = "Cannot find proof that ${A} is the next version after ${B}")
sealed trait IsNextAfter[A <: Version, B <: Version]

object Version {
  def numberFor[V <: Version: VersionInfo]: Int = implicitly[VersionInfo[V]].versionNumber
}

trait V1 extends Version
object V1 {
  implicit object Info extends VersionInfo[V1](1)
}

trait V2 extends Version
object V2 {
  implicit object Info extends VersionInfo[V2](2) with Migratable[V2] with IsNextAfter[V2, V1]
}

trait V3 extends Version
object V3 {
  implicit object Info extends VersionInfo[V3](3) with Migratable[V3] with IsNextAfter[V3, V2]
}

trait V4 extends Version
object V4 {
  implicit object Info extends VersionInfo[V4](3) with Migratable[V4] with IsNextAfter[V4, V3]
}

trait V5 extends Version
object V5 {
  implicit object Info extends VersionInfo[V5](5) with Migratable[V5] with IsNextAfter[V5, V4]
}

trait V6 extends Version
object V6 {
  implicit object Info extends VersionInfo[V6](6) with Migratable[V6] with IsNextAfter[V6, V5]
}

trait V7 extends Version
object V7 {
  implicit object Info extends VersionInfo[V7](7) with Migratable[V7] with IsNextAfter[V7, V6]
}

trait V8 extends Version
object V8 {
  implicit object Info extends VersionInfo[V8](8) with Migratable[V8] with IsNextAfter[V8, V7]
}

trait V9 extends Version
object V9 {
  implicit object Info extends VersionInfo[V9](9) with Migratable[V9] with IsNextAfter[V9, V8]
}

trait V10 extends Version
object V10 {
  implicit object Info extends VersionInfo[V10](10) with Migratable[V10] with IsNextAfter[V10, V9]
}

trait V11 extends Version
object V11 {
  implicit object Info extends VersionInfo[V11](11) with Migratable[V11] with IsNextAfter[V11, V10]
}

trait V12 extends Version
object V12 {
  implicit object Info extends VersionInfo[V12](12) with Migratable[V12] with IsNextAfter[V12, V11]
}

trait V13 extends Version
object V13 {
  implicit object Info extends VersionInfo[V13](13) with Migratable[V13] with IsNextAfter[V13, V12]
}

trait V14 extends Version
object V14 {
  implicit object Info extends VersionInfo[V14](14) with Migratable[V14] with IsNextAfter[V14, V13]
}

trait V15 extends Version
object V15 {
  implicit object Info extends VersionInfo[V15](15) with Migratable[V15] with IsNextAfter[V15, V14]
}

trait V16 extends Version
object V16 {
  implicit object Info extends VersionInfo[V16](16) with Migratable[V16] with IsNextAfter[V16, V15]
}

trait V17 extends Version
object V17 {
  implicit object Info extends VersionInfo[V17](17) with Migratable[V17] with IsNextAfter[V17, V16]
}

trait V18 extends Version
object V18 {
  implicit object Info extends VersionInfo[V18](18) with Migratable[V18] with IsNextAfter[V18, V17]
}

trait V19 extends Version
object V19 {
  implicit object Info extends VersionInfo[V19](19) with Migratable[V19] with IsNextAfter[V19, V18]
}

trait V20 extends Version
object V20 {
  implicit object Info extends VersionInfo[V20](20) with Migratable[V20] with IsNextAfter[V20, V19]
}

trait V21 extends Version
object V21 {
  implicit object Info extends VersionInfo[V21](21) with Migratable[V21] with IsNextAfter[V21, V20]
}

trait V22 extends Version
object V22 {
  implicit object Info extends VersionInfo[V22](22) with Migratable[V22] with IsNextAfter[V22, V21]
}

trait V23 extends Version
object V23 {
  implicit object Info extends VersionInfo[V23](23) with Migratable[V23] with IsNextAfter[V23, V22]
}

trait V24 extends Version
object V24 {
  implicit object Info extends VersionInfo[V24](24) with Migratable[V24] with IsNextAfter[V24, V23]
}

trait V25 extends Version
object V25 {
  implicit object Info extends VersionInfo[V25](25) with Migratable[V25] with IsNextAfter[V25, V24]
}

trait V26 extends Version
object V26 {
  implicit object Info extends VersionInfo[V26](26) with Migratable[V26] with IsNextAfter[V26, V25]
}

trait V27 extends Version
object V27 {
  implicit object Info extends VersionInfo[V27](27) with Migratable[V27] with IsNextAfter[V27, V26]
}

trait V28 extends Version
object V28 {
  implicit object Info extends VersionInfo[V28](28) with Migratable[V28] with IsNextAfter[V28, V27]
}

trait V29 extends Version
object V29 {
  implicit object Info extends VersionInfo[V29](29) with Migratable[V29] with IsNextAfter[V29, V28]
}

trait V30 extends Version
object V30 {
  implicit object Info extends VersionInfo[V30](30) with Migratable[V30] with IsNextAfter[V30, V29]
}

trait V31 extends Version
object V31 {
  implicit object Info extends VersionInfo[V31](31) with Migratable[V31] with IsNextAfter[V31, V30]
}

trait V32 extends Version
object V32 {
  implicit object Info extends VersionInfo[V32](32) with Migratable[V32] with IsNextAfter[V32, V31]
}

trait V33 extends Version
object V33 {
  implicit object Info extends VersionInfo[V33](33) with Migratable[V33] with IsNextAfter[V33, V32]
}

trait V34 extends Version
object V34 {
  implicit object Info extends VersionInfo[V34](34) with Migratable[V34] with IsNextAfter[V34, V33]
}

trait V35 extends Version
object V35 {
  implicit object Info extends VersionInfo[V35](35) with Migratable[V35] with IsNextAfter[V35, V34]
}

trait V36 extends Version
object V36 {
  implicit object Info extends VersionInfo[V36](36) with Migratable[V36] with IsNextAfter[V36, V35]
}

trait V37 extends Version
object V37 {
  implicit object Info extends VersionInfo[V37](37) with Migratable[V37] with IsNextAfter[V37, V36]
}

trait V38 extends Version
object V38 {
  implicit object Info extends VersionInfo[V38](38) with Migratable[V38] with IsNextAfter[V38, V37]
}

trait V39 extends Version
object V39 {
  implicit object Info extends VersionInfo[V39](39) with Migratable[V39] with IsNextAfter[V39, V38]
}

trait V40 extends Version
object V40 {
  implicit object Info extends VersionInfo[V40](40) with Migratable[V40] with IsNextAfter[V40, V39]
}

trait V41 extends Version
object V41 {
  implicit object Info extends VersionInfo[V41](41) with Migratable[V41] with IsNextAfter[V41, V40]
}

trait V42 extends Version
object V42 {
  implicit object Info extends VersionInfo[V42](42) with Migratable[V42] with IsNextAfter[V42, V41]
}

trait V43 extends Version
object V43 {
  implicit object Info extends VersionInfo[V43](43) with Migratable[V43] with IsNextAfter[V43, V42]
}

trait V44 extends Version
object V44 {
  implicit object Info extends VersionInfo[V44](44) with Migratable[V44] with IsNextAfter[V44, V43]
}

trait V45 extends Version
object V45 {
  implicit object Info extends VersionInfo[V45](45) with Migratable[V45] with IsNextAfter[V45, V44]
}

trait V46 extends Version
object V46 {
  implicit object Info extends VersionInfo[V46](46) with Migratable[V46] with IsNextAfter[V46, V45]
}

trait V47 extends Version
object V47 {
  implicit object Info extends VersionInfo[V47](47) with Migratable[V47] with IsNextAfter[V47, V46]
}

trait V48 extends Version
object V48 {
  implicit object Info extends VersionInfo[V48](48) with Migratable[V48] with IsNextAfter[V48, V47]
}

trait V49 extends Version
object V49 {
  implicit object Info extends VersionInfo[V49](49) with Migratable[V49] with IsNextAfter[V49, V48]
}

trait V50 extends Version
object V50 {
  implicit object Info extends VersionInfo[V50](50) with Migratable[V50] with IsNextAfter[V50, V49]
}
