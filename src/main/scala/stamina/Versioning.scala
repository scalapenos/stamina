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

class V1 extends Version
object V1 {
  implicit object Info extends VersionInfo[V1](1)
}

class V2 extends Version
object V2 {
  implicit object Info extends VersionInfo[V2](2) with Migratable[V2] with IsNextAfter[V2, V1]
}

class V3 extends Version
object V3 {
  implicit object Info extends VersionInfo[V3](3) with Migratable[V3] with IsNextAfter[V3, V2]
}

class V4 extends Version
object V4 {
  implicit object Info extends VersionInfo[V4](3) with Migratable[V4] with IsNextAfter[V4, V3]
}

class V5 extends Version
case object V5 {
  implicit object Info extends VersionInfo[V5](5) with Migratable[V5] with IsNextAfter[V5, V4]
}

class V6 extends Version
case object V6 {
  implicit object Info extends VersionInfo[V6](6) with Migratable[V6] with IsNextAfter[V6, V5]
}

class V7 extends Version
case object V7 {
  implicit object Info extends VersionInfo[V7](7) with Migratable[V7] with IsNextAfter[V7, V6]
}

class V8 extends Version
case object V8 {
  implicit object Info extends VersionInfo[V8](8) with Migratable[V8] with IsNextAfter[V8, V7]
}

class V9 extends Version
case object V9 {
  implicit object Info extends VersionInfo[V9](9) with Migratable[V9] with IsNextAfter[V9, V8]
}

class V10 extends Version
case object V10 {
  implicit object Info extends VersionInfo[V10](10) with Migratable[V10] with IsNextAfter[V10, V9]
}

class V11 extends Version
case object V11 {
  implicit object Info extends VersionInfo[V11](11) with Migratable[V11] with IsNextAfter[V11, V10]
}

class V12 extends Version
case object V12 {
  implicit object Info extends VersionInfo[V12](12) with Migratable[V12] with IsNextAfter[V12, V11]
}

class V13 extends Version
case object V13 {
  implicit object Info extends VersionInfo[V13](13) with Migratable[V13] with IsNextAfter[V13, V12]
}

class V14 extends Version
case object V14 {
  implicit object Info extends VersionInfo[V14](14) with Migratable[V14] with IsNextAfter[V14, V13]
}

class V15 extends Version
case object V15 {
  implicit object Info extends VersionInfo[V15](15) with Migratable[V15] with IsNextAfter[V15, V14]
}

class V16 extends Version
case object V16 {
  implicit object Info extends VersionInfo[V16](16) with Migratable[V16] with IsNextAfter[V16, V15]
}

class V17 extends Version
case object V17 {
  implicit object Info extends VersionInfo[V17](17) with Migratable[V17] with IsNextAfter[V17, V16]
}

class V18 extends Version
case object V18 {
  implicit object Info extends VersionInfo[V18](18) with Migratable[V18] with IsNextAfter[V18, V17]
}

class V19 extends Version
case object V19 {
  implicit object Info extends VersionInfo[V19](19) with Migratable[V19] with IsNextAfter[V19, V18]
}

class V20 extends Version
case object V20 {
  implicit object Info extends VersionInfo[V20](20) with Migratable[V20] with IsNextAfter[V20, V19]
}

class V21 extends Version
case object V21 {
  implicit object Info extends VersionInfo[V21](21) with Migratable[V21] with IsNextAfter[V21, V20]
}

class V22 extends Version
case object V22 {
  implicit object Info extends VersionInfo[V22](22) with Migratable[V22] with IsNextAfter[V22, V21]
}

class V23 extends Version
case object V23 {
  implicit object Info extends VersionInfo[V23](23) with Migratable[V23] with IsNextAfter[V23, V22]
}

class V24 extends Version
case object V24 {
  implicit object Info extends VersionInfo[V24](24) with Migratable[V24] with IsNextAfter[V24, V23]
}

class V25 extends Version
case object V25 {
  implicit object Info extends VersionInfo[V25](25) with Migratable[V25] with IsNextAfter[V25, V24]
}

class V26 extends Version
case object V26 {
  implicit object Info extends VersionInfo[V26](26) with Migratable[V26] with IsNextAfter[V26, V25]
}

class V27 extends Version
case object V27 {
  implicit object Info extends VersionInfo[V27](27) with Migratable[V27] with IsNextAfter[V27, V26]
}

class V28 extends Version
case object V28 {
  implicit object Info extends VersionInfo[V28](28) with Migratable[V28] with IsNextAfter[V28, V27]
}

class V29 extends Version
case object V29 {
  implicit object Info extends VersionInfo[V29](29) with Migratable[V29] with IsNextAfter[V29, V28]
}

class V30 extends Version
case object V30 {
  implicit object Info extends VersionInfo[V30](30) with Migratable[V30] with IsNextAfter[V30, V29]
}

class V31 extends Version
case object V31 {
  implicit object Info extends VersionInfo[V31](31) with Migratable[V31] with IsNextAfter[V31, V30]
}

class V32 extends Version
case object V32 {
  implicit object Info extends VersionInfo[V32](32) with Migratable[V32] with IsNextAfter[V32, V31]
}

class V33 extends Version
case object V33 {
  implicit object Info extends VersionInfo[V33](33) with Migratable[V33] with IsNextAfter[V33, V32]
}

class V34 extends Version
case object V34 {
  implicit object Info extends VersionInfo[V34](34) with Migratable[V34] with IsNextAfter[V34, V33]
}

class V35 extends Version
case object V35 {
  implicit object Info extends VersionInfo[V35](35) with Migratable[V35] with IsNextAfter[V35, V34]
}

class V36 extends Version
case object V36 {
  implicit object Info extends VersionInfo[V36](36) with Migratable[V36] with IsNextAfter[V36, V35]
}

class V37 extends Version
case object V37 {
  implicit object Info extends VersionInfo[V37](37) with Migratable[V37] with IsNextAfter[V37, V36]
}

class V38 extends Version
case object V38 {
  implicit object Info extends VersionInfo[V38](38) with Migratable[V38] with IsNextAfter[V38, V37]
}

class V39 extends Version
case object V39 {
  implicit object Info extends VersionInfo[V39](39) with Migratable[V39] with IsNextAfter[V39, V38]
}

class V40 extends Version
case object V40 {
  implicit object Info extends VersionInfo[V40](40) with Migratable[V40] with IsNextAfter[V40, V39]
}

class V41 extends Version
case object V41 {
  implicit object Info extends VersionInfo[V41](41) with Migratable[V41] with IsNextAfter[V41, V40]
}

class V42 extends Version
case object V42 {
  implicit object Info extends VersionInfo[V42](42) with Migratable[V42] with IsNextAfter[V42, V41]
}

class V43 extends Version
case object V43 {
  implicit object Info extends VersionInfo[V43](43) with Migratable[V43] with IsNextAfter[V43, V42]
}

class V44 extends Version
case object V44 {
  implicit object Info extends VersionInfo[V44](44) with Migratable[V44] with IsNextAfter[V44, V43]
}

class V45 extends Version
case object V45 {
  implicit object Info extends VersionInfo[V45](45) with Migratable[V45] with IsNextAfter[V45, V44]
}

class V46 extends Version
case object V46 {
  implicit object Info extends VersionInfo[V46](46) with Migratable[V46] with IsNextAfter[V46, V45]
}

class V47 extends Version
case object V47 {
  implicit object Info extends VersionInfo[V47](47) with Migratable[V47] with IsNextAfter[V47, V46]
}

class V48 extends Version
case object V48 {
  implicit object Info extends VersionInfo[V48](48) with Migratable[V48] with IsNextAfter[V48, V47]
}

class V49 extends Version
case object V49 {
  implicit object Info extends VersionInfo[V49](49) with Migratable[V49] with IsNextAfter[V49, V48]
}

class V50 extends Version
case object V50 {
  implicit object Info extends VersionInfo[V50](50) with Migratable[V50] with IsNextAfter[V50, V49]
}
