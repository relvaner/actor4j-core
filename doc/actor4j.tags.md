| Class | Tag | Value |
| :---: | :---: | :---: |
| ActorProtocolTag |||
|| INTERNAL_RESTART             | -1  |
|| INTERNAL_STOP                | -2  |
|| INTERNAL_STOP_SUCCESS        | -3  |
|| INTERNAL_KILL                | -4  |
|| INTERNAL_RECOVER             | -5  |
|| INTERNAL_PERSISTENCE_RECOVER | -6  |
|| INTERNAL_PERSISTENCE_SUCCESS | -7  |
|| INTERNAL_PERSISTENCE_FAILURE | -8  |
|| INTERNAL_ACTIVATE            | -9  |
|| INTERNAL_DEACTIVATE          | -10 |
| Actor |||
|| POISONPILL | INTERNAL_STOP |
|| TERMINATED | INTERNAL_STOP_SUCCESS |
|| KILL       | INTERNAL_KILL |
|| STOP       | INTERNAL_STOP |
|| RESTART    | INTERNAL_RESTART |
|| ACTIVATE   | INTERNAL_ACTIVATE |
|| DEACTIVATE | INTERNAL_DEACTIVATE |
| PersistenceServiceActor |||
|| PERSIST_EVENTS               | 100 |
|| PERSIST_STATE                | 101 |
|| RECOVER                      | 102 |
| BrokerActor |||
|| GET_TOPIC_ACTOR              | 100 |
|| CLEAN_UP                     | 101 |
|| INTERNAL_FORWARDED_BY_BROKER | 102 |
| ReactiveStreamsTag |||
|| SUBSCRIPTION_REQUEST         | 200 |
|| SUBSCRIPTION_REQUEST_RESET   | 201 |
|| SUBSCRIPTION_CANCEL          | 202 |
|| SUBSCRIPTION_BULK            | 203 |
|| SUBSCRIPTION_CANCEL_BULK     | 204 |
|| ON_NEXT                      | 205 |
|| ON_ERROR                     | 206 |
|| ON_COMPLETE                  | 207 |
| ActorWithCache |||
|| GC                           | 300 |
|| EVICT                        | GC  |
|| GET                          | 301 |
|| SET                          | 302 |
|| UPDATE                       | 303 |
|| DEL                          | 304 |
|| DEL_ALL                      | 305 |
|| CLEAR                        | 306 |
|| CAS                          | 307 |
|| CAU                          | 308 |
| DataAccessActor |||
|| HAS_ONE                      | 315 |
|| INSERT_ONE                   | 316 |
|| REPLACE_ONE                  | 317 |
|| UPDATE_ONE                   | 318 |
|| DELETE_ONE                   | 319 |
|| FIND_ONE                     | 320 |
|| FLUSH                        | 321 |
| ServiceDiscoveryActor |||
|| PUBLISH_SERVICE              | 400 |
|| UNPUBLISH_SERVICE            | 401 |
|| LOOKUP_SERVICES              | 402 |
|| LOOKUP_SERVICE               | 403 |