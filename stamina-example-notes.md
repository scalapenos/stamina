
# Notes on the Stamina example app

- build a basic Spray/Akka CRUD app using persistent actors per resource + id
- use a simplified version of the cluster-sharding approach, with one parent actor serving as the API but delegating to persistent child actors
- the persistent child actors kill themselves after x minutes of inactivity
- use file-based level db as the akka persistence store
- start out with all domain classes at V1 and then migrate the data in all supported ways, making sure to go up to V5+
- provide an app (or a version of it) for every supported implementation (i.e. json, avro, etc.)
- add some timing/sizing benchmarking code so people can compare performance
- perhaps also add a version using the default Akka implementation so we can compare performance to that
