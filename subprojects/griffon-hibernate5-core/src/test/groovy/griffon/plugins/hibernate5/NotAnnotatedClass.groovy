package griffon.plugins.hibernate5

import griffon.metadata.TypeProviderFor
import groovy.transform.ToString

import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

/**
 * @author Leonid Yanovsky
 */
@ToString
@TypeProviderFor(Hibernate5Mapping)
class NotAnnotatedClass implements Hibernate5Mapping {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id
    @Column
    String name
    @Column
    String lastname

    Map asMap() {
        [
                id      : id,
                name    : name,
                lastname: lastname
        ]
    }
}
