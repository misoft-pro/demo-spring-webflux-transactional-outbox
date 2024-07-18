package pro.misoft.payment.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

object Mapper: ObjectMapper()  {
    private fun readResolve(): Any = Mapper

    init {
        this.registerModule(KotlinModule.Builder().build())
        this.registerModule(JavaTimeModule())
        this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
}