Releasing:

* remove '-SNAPSHOT' from version in project/Build.scala
* commit as 'Preparing for release X'
* sbt +publishSigned
* sbt sonatypeRelease
* bump version to next '-SNAPSHOT' in project/Build.scala
* update README.md to recommend getting the newly released version
