package pro.misoft.payment.config

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import jakarta.annotation.PostConstruct
import jakarta.validation.Validator
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.AbstractResourceBasedMessageSource
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.transaction.reactive.TransactionalEventPublisher
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import reactor.core.publisher.Hooks
import java.time.ZoneOffset
import java.util.*


@Configuration
@EnableR2dbcAuditing
class AppConfig {

    @Bean
    fun transactionalEventPublisher(applicationEventPublisher: ApplicationEventPublisher) =
        TransactionalEventPublisher(applicationEventPublisher)

    @Bean
    fun dateTimeProvider(): DateTimeProvider = DateTimeProvider { Optional.of(ZoneOffset.UTC) }

    @Bean
    fun messageSource(): MessageSource {
        val messageSource = ResourceBundleMessageSource()
        messageSource.setBasename("i18n/messages")
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }

    @Bean
    fun jackson2ObjectMapperBuilderCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { jacksonObjectMapperBuilder ->
            jacksonObjectMapperBuilder
                .modules(KotlinModule.Builder().build(), JavaTimeModule())
        }
    }

    @PostConstruct
    fun init() {
        Hooks.enableAutomaticContextPropagation()
    }

    @Bean
    fun localisedConstraintValidator(messageSource: AbstractResourceBasedMessageSource): Validator {
        val bean = LocalValidatorFactoryBean()
        bean.setValidationMessageSource(messageSource)
        return bean
    }
}