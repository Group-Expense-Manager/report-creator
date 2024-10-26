package pl.edu.agh.gem.external.controller

import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import pl.edu.agh.gem.error.SimpleError
import pl.edu.agh.gem.error.SimpleErrorsHolder
import pl.edu.agh.gem.error.handleError
import pl.edu.agh.gem.error.withCode
import pl.edu.agh.gem.error.withDetails
import pl.edu.agh.gem.error.withMessage
import pl.edu.agh.gem.error.withUserMessage
import pl.edu.agh.gem.exception.UserWithoutGroupAccessException
import pl.edu.agh.gem.internal.client.GroupManagerClientException

@ControllerAdvice
@Order(LOWEST_PRECEDENCE)
class ApiExceptionHandler {

    @ExceptionHandler(UserWithoutGroupAccessException::class)
    fun handleUserWithoutGroupAccessException(
        exception: UserWithoutGroupAccessException,
    ): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), FORBIDDEN)
    }

    @ExceptionHandler(GroupManagerClientException::class)
    fun handleGroupManagerClientException(exception: GroupManagerClientException): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        exception: MethodArgumentNotValidException,
    ): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleNotValidException(exception), BAD_REQUEST)
    }

    private fun handleNotValidException(exception: MethodArgumentNotValidException): SimpleErrorsHolder {
        val errors = exception.bindingResult.fieldErrors
            .map { error ->
                SimpleError()
                    .withCode("VALIDATION_ERROR")
                    .withDetails(error.field)
                    .withUserMessage(error.defaultMessage)
                    .withMessage(error.defaultMessage)
            }
        return SimpleErrorsHolder(errors)
    }
}
