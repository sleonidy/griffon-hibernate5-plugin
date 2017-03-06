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
import org.codehaus.griffon.runtime.util.ResourceBundleProvider
import org.hibernate.Session
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

@Unroll
class Hibernate5SingleDataSource extends Specification {
    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace")
    }

    @Rule
    public final GriffonUnitRule griffon = new GriffonUnitRule()

    @Inject
    private Hibernate5Handler hibernate5Handler

    @Inject
    private GriffonApplication application

    @BindTo(ResourceBundle)
    @Named("datasource")
    @Singleton
    private Provider<ResourceBundle> dataSourceResourceBundleProvider = new ResourceBundleProvider("SingleDataSource")

    @BindTo(ResourceBundle)
    @Named("hibernate5")
    @Singleton
    private Provider<ResourceBundle> hibernateResourceBundleProvider = new ResourceBundleProvider("SingleDataSource")

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

    void "AnotherPerson class can be used on 'default' datasource"() {
        when:
        List peopleIn = hibernate5Handler.withHbm5Session { String sessionFactoryName, Session session ->
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

        List peopleOut = hibernate5Handler.withHbm5Session { String sessionFactoryName, Session session ->
            session.createQuery('from AnotherPerson').list()*.asMap()
        }

        then:
        peopleIn == peopleOut
    }
}