/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package griffon.plugins.hibernate5

import griffon.core.GriffonApplication
import griffon.core.RunnableWithArgs
import griffon.core.test.GriffonUnitRule
import griffon.inject.BindTo
import griffon.plugins.hibernate5.exceptions.RuntimeHibernate5Exception
import org.hibernate.Session
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject

@Unroll
class Hibernate5Spec extends Specification {
    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace")
    }

    @Rule
    public final GriffonUnitRule griffon = new GriffonUnitRule()

    @Inject
    private Hibernate5Handler hibernate5Handler

    @Inject
    private GriffonApplication application

    void 'Open and close default hibernate5'() {
        given:
        List eventNames = [
                'Hibernate5ConnectStart', 'DataSourceConnectStart',
                'DataSourceConnectEnd', 'Hibernate5ConfigurationAvailable', 'Hibernate5ConnectEnd',
                'Hibernate5DisconnectStart', 'DataSourceDisconnectStart',
                'DataSourceDisconnectEnd', 'Hibernate5DisconnectEnd'
        ]
        List events = []
        eventNames.each { name ->
            application.eventRouter.addEventListener(name, { Object... args ->
                events << [name: name, args: args]
            } as RunnableWithArgs)
        }

        when:
        hibernate5Handler.withHbm5Session { String sessionFactoryName, Session session ->
            true
        }
        hibernate5Handler.closeHbm5Session()
        // second call should be a NOOP
        hibernate5Handler.closeHbm5Session()

        then:
        events.size() == 9
        events.name == eventNames
    }

    void 'Connect to default SessionFactory'() {
        expect:
        hibernate5Handler.withHbm5Session { String sessionFactoryName, Session session ->
            sessionFactoryName == 'default' && session
        }
    }

    void 'Bootstrap init is called'() {
        given:
        assert !bootstrap.initWitness

        when:
        hibernate5Handler.withHbm5Session { String sessionFactoryName, Session session -> }

        then:
        bootstrap.initWitness
        !bootstrap.destroyWitness
    }

    void 'Bootstrap destroy is called'() {
        given:
        assert !bootstrap.initWitness
        assert !bootstrap.destroyWitness

        when:
        hibernate5Handler.withHbm5Session { String sessionFactoryName, Session session -> }
        hibernate5Handler.closeHbm5Session()

        then:
        bootstrap.initWitness
        bootstrap.destroyWitness
    }

    void 'Can connect to #name SessionFactory'() {
        expect:
        hibernate5Handler.withHbm5Session(name) { String sessionFactoryName, Session session ->
            sessionFactoryName == name && session
        }

        where:
        name       | _
        'default'  | _
        'internal' | _
        'people'   | _
    }

    void 'Bogus SessionFactory name (#name) results in error'() {
        when:
        hibernate5Handler.withHbm5Session(name) { String sessionFactoryName, Session session ->
            true
        }

        then:
        thrown(IllegalArgumentException)

        where:
        name    | _
        null    | _
        ''      | _
        'bogus' | _
    }

    void 'Execute statements on people table'() {
        when:
        List peopleIn = hibernate5Handler.withHbm5Session() { String sessionFactoryName, Session session ->
            [[id: 1, name: 'Danno', lastname: 'Ferrin'],
             [id: 2, name: 'Andres', lastname: 'Almiray'],
             [id: 3, name: 'James', lastname: 'Williams'],
             [id: 4, name: 'Guillaume', lastname: 'Laforge'],
             [id: 5, name: 'Jim', lastname: 'Shingler'],
             [id: 6, name: 'Alexander', lastname: 'Klein'],
             [id: 7, name: 'Rene', lastname: 'Groeschke']].each { data ->
                session.save(new Person(data))
            }
        }

        List peopleOut = hibernate5Handler.withHbm5Session() { String sessionFactoryName, Session session ->
            session.createQuery('from Person').list()*.asMap()
        }

        then:
        peopleIn == peopleOut
    }

    void 'A runtime exception is thrown within session handling'() {
        when:
        hibernate5Handler.withHbm5Session { String sessionFactoryName, Session session ->
            session.save(new Person())
        }

        then:
        thrown(RuntimeHibernate5Exception)
    }

    void "Exception is thrown when using Person class on 'people' datasource"() {
        when:
        hibernate5Handler.withHbm5Session(name) { String sessionFactoryName, Session session ->
            session.createQuery("from Person").list()
        }

        then:
        thrown(RuntimeHibernate5Exception)

        where:
        name     | _
        'people' | _
    }

    void "AnotherPerson class can be used on 'people' datasource"() {
        when:
        List peopleIn = hibernate5Handler.withHbm5Session(name) { String sessionFactoryName, Session session ->
            [[id: 1, name: 'Danno', lastname: 'Ferrin'],
             [id: 2, name: 'Andres', lastname: 'Almiray'],
             [id: 3, name: 'James', lastname: 'Williams'],
             [id: 4, name: 'Guillaume', lastname: 'Laforge'],
             [id: 5, name: 'Jim', lastname: 'Shingler'],
             [id: 6, name: 'Alexander', lastname: 'Klein'],
             [id: 7, name: 'Rene', lastname: 'Groeschke']].each { data ->
                session.save(new AnotherPerson(data))
            }
        }

        List peopleOut = hibernate5Handler.withHbm5Session(name) { String sessionFactoryName, Session session ->
            session.createQuery('from AnotherPerson').list()*.asMap()
        }

        then:
        peopleIn == peopleOut

        where:
        name     | _
        'people' | _
    }

    void 'Execute statements on annotated class'() {
        when:
        List userIn = hibernate5Handler.withHbm5Session() { String sessionFactoryName, Session session ->
            [[id: 1, name: 'Danno', lastname: 'Ferrin'],
             [id: 2, name: 'Andres', lastname: 'Almiray'],
             [id: 3, name: 'James', lastname: 'Williams'],
             [id: 4, name: 'Guillaume', lastname: 'Laforge'],
             [id: 5, name: 'Jim', lastname: 'Shingler'],
             [id: 6, name: 'Alexander', lastname: 'Klein'],
             [id: 7, name: 'Rene', lastname: 'Groeschke']].each { data ->
                session.save(new User(data))
            }
        }

        List userOut = hibernate5Handler.withHbm5Session() { String sessionFactoryName, Session session ->
            session.createCriteria(User).list()*.asMap()
        }

        then:
        userIn == userOut
    }

    void 'Mapped class without @Entity annotation should throw exception'() {
        when:
        hibernate5Handler.withHbm5Session() { String sessionFactoryName, Session session ->
            session.saveOrUpdate(new NotAnnotatedClass([id: 2, name: 'Andres', lastname: 'Almiray']))
        }

        then:
        thrown(RuntimeHibernate5Exception)
    }
    @BindTo(Hibernate5Bootstrap)
    private TestHibernate5Bootstrap bootstrap = new TestHibernate5Bootstrap()
}
