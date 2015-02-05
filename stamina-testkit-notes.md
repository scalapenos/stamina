
# Notes on Stamina Testkit

- per `Persister[T, V]` + sample instance of T
    + generate a roundtrip testcase to prove basic behavior
    + if no regression test file exists, generate a file containing the persisted bytes, named using the relevant key and the version
    + if a regression test file exists for the current version, unpersist it to make sure no incompatible changes were made without incrementing the version
    + for all versions before the current version, assume a regression test file exists and use it to unpersist the persisted data to verify migrations work correctly
- allow testing at the `Persister[T, V]` level or at the `Persisters` level
- you can always get the key and the version required to check for regression test files by simply persisting the same instance of T and getting them from the instance of `Persisted` produced.
