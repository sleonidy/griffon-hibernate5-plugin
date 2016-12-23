dataSource {
    driverClassName = 'org.h2.Driver'
    username = 'sa'
    password = ''
    url = 'jdbc:h2:mem:@application.name@-default'
    pool {
        maxWait = 60000
        maxIdle = 5
        maxActive = 8
    }
}
