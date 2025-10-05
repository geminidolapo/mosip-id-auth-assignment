package io.mosip.authentication.service.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Global exception handler for the ID Authentication Service.
 * Handles all exceptions and provides standardized error responses.
 *
 * @author MOSIP
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** The Constant GENERIC_EXCEPTION_CODE. */
    private static final String GENERIC_EXCEPTION_CODE = "IDA-GEN-001";

    /** The Constant VALIDATION_EXCEPTION_CODE. */
    private static final String VALIDATION_EXCEPTION_CODE = "IDA-VAL-001";

    /** The Constant HTTP_MESSAGE_EXCEPTION_CODE. */
    private static final String HTTP_MESSAGE_EXCEPTION_CODE = "IDA-REQ-001";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<ServiceError>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {

        List<ServiceError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::buildServiceError)
                .collect(Collectors.toList());

        log.warn("Request validation failed with {} errors: {}", errors.size(),
                errors.stream().map(ServiceError::getMessage).collect(Collectors.joining(", ")));

        ResponseWrapper<ServiceError> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setErrors(errors);
        responseWrapper.setResponsetime(DateUtils.getUTCCurrentDateTime());

        return new ResponseEntity<>(responseWrapper, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseWrapper<ServiceError>> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        List<ServiceError> errors = violations.stream()
                .map(violation -> new ServiceError(VALIDATION_EXCEPTION_CODE,
                        violation.getPropertyPath() + ": " + violation.getMessage()))
                .collect(Collectors.toList());

        log.warn("Constraint violation occurred: {}", errors);
        return buildErrorResponse(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseWrapper<ServiceError>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {

        ServiceError error = new ServiceError(HTTP_MESSAGE_EXCEPTION_CODE,
                "Malformed JSON request: " + ex.getMostSpecificCause().getMessage());

        log.warn("HTTP message not readable: {}", error.getMessage());
        return buildErrorResponse(List.of(error), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BaseUncheckedException.class)
    public ResponseEntity<ResponseWrapper<ServiceError>> handleBaseUncheckedException(
            BaseUncheckedException ex, WebRequest request) {

        ServiceError error = new ServiceError(ex.getErrorCode(), ex.getErrorText());

        log.error("MOSIP base unchecked exception occurred - Code: {}, Message: {}",
                error.getErrorCode(), error.getMessage(), ex);
        return buildErrorResponse(List.of(error), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<ServiceError>> handleAllExceptions(
            Exception ex, WebRequest request) {

        ServiceError error = new ServiceError(GENERIC_EXCEPTION_CODE,
                "Internal server error occurred");

        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        return buildErrorResponse(List.of(error), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ServiceError buildServiceError(FieldError fieldError) {
        String message = fieldError.getField() + ": " + fieldError.getDefaultMessage();
        return new ServiceError(VALIDATION_EXCEPTION_CODE, message);
    }

    private ResponseEntity<ResponseWrapper<ServiceError>> buildErrorResponse(
            List<ServiceError> errors, HttpStatus status) {

        ResponseWrapper<ServiceError> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setErrors(errors);
        responseWrapper.setResponsetime(DateUtils.getUTCCurrentDateTime());

        return new ResponseEntity<>(responseWrapper, status);
    }
}
