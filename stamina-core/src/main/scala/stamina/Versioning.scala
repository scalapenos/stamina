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
sealed trait MigratableVersion[V <: Version]

/** Type class used by the API to enforce two Version subclasses to be sequential. */
@annotation.implicitNotFound(msg = "Cannot find proof that ${A} is the next version after ${B}")
sealed trait IsNextVersionAfter[A <: Version, B <: Version]

object Version {
  def numberFor[V <: Version: VersionInfo]: Int = implicitly[VersionInfo[V]].versionNumber
}

sealed trait V1 extends Version
object V1 {
  implicit object Info extends VersionInfo[V1](1)
}

sealed trait V2 extends Version
object V2 {
  implicit object Info extends VersionInfo[V2](2) with MigratableVersion[V2] with IsNextVersionAfter[V2, V1]
}

sealed trait V3 extends Version
object V3 {
  implicit object Info extends VersionInfo[V3](3) with MigratableVersion[V3] with IsNextVersionAfter[V3, V2]
}

sealed trait V4 extends Version
object V4 {
  implicit object Info extends VersionInfo[V4](4) with MigratableVersion[V4] with IsNextVersionAfter[V4, V3]
}

sealed trait V5 extends Version
object V5 {
  implicit object Info extends VersionInfo[V5](5) with MigratableVersion[V5] with IsNextVersionAfter[V5, V4]
}

sealed trait V6 extends Version
object V6 {
  implicit object Info extends VersionInfo[V6](6) with MigratableVersion[V6] with IsNextVersionAfter[V6, V5]
}

sealed trait V7 extends Version
object V7 {
  implicit object Info extends VersionInfo[V7](7) with MigratableVersion[V7] with IsNextVersionAfter[V7, V6]
}

sealed trait V8 extends Version
object V8 {
  implicit object Info extends VersionInfo[V8](8) with MigratableVersion[V8] with IsNextVersionAfter[V8, V7]
}

sealed trait V9 extends Version
object V9 {
  implicit object Info extends VersionInfo[V9](9) with MigratableVersion[V9] with IsNextVersionAfter[V9, V8]
}

sealed trait V10 extends Version
object V10 {
  implicit object Info extends VersionInfo[V10](10) with MigratableVersion[V10] with IsNextVersionAfter[V10, V9]
}

sealed trait V11 extends Version
object V11 {
  implicit object Info extends VersionInfo[V11](11) with MigratableVersion[V11] with IsNextVersionAfter[V11, V10]
}

sealed trait V12 extends Version
object V12 {
  implicit object Info extends VersionInfo[V12](12) with MigratableVersion[V12] with IsNextVersionAfter[V12, V11]
}

sealed trait V13 extends Version
object V13 {
  implicit object Info extends VersionInfo[V13](13) with MigratableVersion[V13] with IsNextVersionAfter[V13, V12]
}

sealed trait V14 extends Version
object V14 {
  implicit object Info extends VersionInfo[V14](14) with MigratableVersion[V14] with IsNextVersionAfter[V14, V13]
}

sealed trait V15 extends Version
object V15 {
  implicit object Info extends VersionInfo[V15](15) with MigratableVersion[V15] with IsNextVersionAfter[V15, V14]
}

sealed trait V16 extends Version
object V16 {
  implicit object Info extends VersionInfo[V16](16) with MigratableVersion[V16] with IsNextVersionAfter[V16, V15]
}

sealed trait V17 extends Version
object V17 {
  implicit object Info extends VersionInfo[V17](17) with MigratableVersion[V17] with IsNextVersionAfter[V17, V16]
}

sealed trait V18 extends Version
object V18 {
  implicit object Info extends VersionInfo[V18](18) with MigratableVersion[V18] with IsNextVersionAfter[V18, V17]
}

sealed trait V19 extends Version
object V19 {
  implicit object Info extends VersionInfo[V19](19) with MigratableVersion[V19] with IsNextVersionAfter[V19, V18]
}

sealed trait V20 extends Version
object V20 {
  implicit object Info extends VersionInfo[V20](20) with MigratableVersion[V20] with IsNextVersionAfter[V20, V19]
}

sealed trait V21 extends Version
object V21 {
  implicit object Info extends VersionInfo[V21](21) with MigratableVersion[V21] with IsNextVersionAfter[V21, V20]
}

sealed trait V22 extends Version
object V22 {
  implicit object Info extends VersionInfo[V22](22) with MigratableVersion[V22] with IsNextVersionAfter[V22, V21]
}

sealed trait V23 extends Version
object V23 {
  implicit object Info extends VersionInfo[V23](23) with MigratableVersion[V23] with IsNextVersionAfter[V23, V22]
}

sealed trait V24 extends Version
object V24 {
  implicit object Info extends VersionInfo[V24](24) with MigratableVersion[V24] with IsNextVersionAfter[V24, V23]
}

sealed trait V25 extends Version
object V25 {
  implicit object Info extends VersionInfo[V25](25) with MigratableVersion[V25] with IsNextVersionAfter[V25, V24]
}

sealed trait V26 extends Version
object V26 {
  implicit object Info extends VersionInfo[V26](26) with MigratableVersion[V26] with IsNextVersionAfter[V26, V25]
}

sealed trait V27 extends Version
object V27 {
  implicit object Info extends VersionInfo[V27](27) with MigratableVersion[V27] with IsNextVersionAfter[V27, V26]
}

sealed trait V28 extends Version
object V28 {
  implicit object Info extends VersionInfo[V28](28) with MigratableVersion[V28] with IsNextVersionAfter[V28, V27]
}

sealed trait V29 extends Version
object V29 {
  implicit object Info extends VersionInfo[V29](29) with MigratableVersion[V29] with IsNextVersionAfter[V29, V28]
}

sealed trait V30 extends Version
object V30 {
  implicit object Info extends VersionInfo[V30](30) with MigratableVersion[V30] with IsNextVersionAfter[V30, V29]
}

sealed trait V31 extends Version
object V31 {
  implicit object Info extends VersionInfo[V31](31) with MigratableVersion[V31] with IsNextVersionAfter[V31, V30]
}

sealed trait V32 extends Version
object V32 {
  implicit object Info extends VersionInfo[V32](32) with MigratableVersion[V32] with IsNextVersionAfter[V32, V31]
}

sealed trait V33 extends Version
object V33 {
  implicit object Info extends VersionInfo[V33](33) with MigratableVersion[V33] with IsNextVersionAfter[V33, V32]
}

sealed trait V34 extends Version
object V34 {
  implicit object Info extends VersionInfo[V34](34) with MigratableVersion[V34] with IsNextVersionAfter[V34, V33]
}

sealed trait V35 extends Version
object V35 {
  implicit object Info extends VersionInfo[V35](35) with MigratableVersion[V35] with IsNextVersionAfter[V35, V34]
}

sealed trait V36 extends Version
object V36 {
  implicit object Info extends VersionInfo[V36](36) with MigratableVersion[V36] with IsNextVersionAfter[V36, V35]
}

sealed trait V37 extends Version
object V37 {
  implicit object Info extends VersionInfo[V37](37) with MigratableVersion[V37] with IsNextVersionAfter[V37, V36]
}

sealed trait V38 extends Version
object V38 {
  implicit object Info extends VersionInfo[V38](38) with MigratableVersion[V38] with IsNextVersionAfter[V38, V37]
}

sealed trait V39 extends Version
object V39 {
  implicit object Info extends VersionInfo[V39](39) with MigratableVersion[V39] with IsNextVersionAfter[V39, V38]
}

sealed trait V40 extends Version
object V40 {
  implicit object Info extends VersionInfo[V40](40) with MigratableVersion[V40] with IsNextVersionAfter[V40, V39]
}

sealed trait V41 extends Version
object V41 {
  implicit object Info extends VersionInfo[V41](41) with MigratableVersion[V41] with IsNextVersionAfter[V41, V40]
}

sealed trait V42 extends Version
object V42 {
  implicit object Info extends VersionInfo[V42](42) with MigratableVersion[V42] with IsNextVersionAfter[V42, V41]
}

sealed trait V43 extends Version
object V43 {
  implicit object Info extends VersionInfo[V43](43) with MigratableVersion[V43] with IsNextVersionAfter[V43, V42]
}

sealed trait V44 extends Version
object V44 {
  implicit object Info extends VersionInfo[V44](44) with MigratableVersion[V44] with IsNextVersionAfter[V44, V43]
}

sealed trait V45 extends Version
object V45 {
  implicit object Info extends VersionInfo[V45](45) with MigratableVersion[V45] with IsNextVersionAfter[V45, V44]
}

sealed trait V46 extends Version
object V46 {
  implicit object Info extends VersionInfo[V46](46) with MigratableVersion[V46] with IsNextVersionAfter[V46, V45]
}

sealed trait V47 extends Version
object V47 {
  implicit object Info extends VersionInfo[V47](47) with MigratableVersion[V47] with IsNextVersionAfter[V47, V46]
}

sealed trait V48 extends Version
object V48 {
  implicit object Info extends VersionInfo[V48](48) with MigratableVersion[V48] with IsNextVersionAfter[V48, V47]
}

sealed trait V49 extends Version
object V49 {
  implicit object Info extends VersionInfo[V49](49) with MigratableVersion[V49] with IsNextVersionAfter[V49, V48]
}

sealed trait V50 extends Version
object V50 {
  implicit object Info extends VersionInfo[V50](50) with MigratableVersion[V50] with IsNextVersionAfter[V50, V49]
}
