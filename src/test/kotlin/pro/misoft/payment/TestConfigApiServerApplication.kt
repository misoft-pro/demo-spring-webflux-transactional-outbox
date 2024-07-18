package pro.misoft.payment

import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestConfigApiServerApplication {

    @Bean
    @ServiceConnection(name = "postgres")
    fun postgresContainer(): PostgreSQLContainer<*> {
        return PostgreSQLContainer(DockerImageName.parse("postgres:16.3")).withExposedPorts(5432)
    }
}

fun main(args: Array<String>) {
    fromApplication<PaymentServiceApplication>().with(TestConfigApiServerApplication::class.java).run(*args)
}
