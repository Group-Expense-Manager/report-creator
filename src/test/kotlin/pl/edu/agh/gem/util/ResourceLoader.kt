package pl.edu.agh.gem.util

import org.springframework.core.io.ClassPathResource

object ResourceLoader {
    fun loadResourceAsByteArray(resourcePath: String): ByteArray {
        return ClassPathResource(resourcePath).contentAsByteArray
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")
    }
}
