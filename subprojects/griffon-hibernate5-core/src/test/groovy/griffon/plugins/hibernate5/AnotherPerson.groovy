package griffon.plugins.hibernate5

import griffon.metadata.TypeProviderFor
import groovy.transform.ToString

/**
 * Created by leonidyanovsky on 9/10/15.
 */
@ToString
@TypeProviderFor(Hibernate5Mapping)
class AnotherPerson implements Serializable, Hibernate5Mapping {
    int id
    String name
    String lastname

    Map asMap() {
        [
                id      : id,
                name    : name,
                lastname: lastname
        ]
    }
}