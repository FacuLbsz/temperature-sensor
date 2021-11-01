package faka.cs.tsensor.adapters.web;

import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

    /**
     * Parses all the exception related to constraints on request params.
     *
     * @param ex exception to parse
     * @return well described json error
     */
    @ExceptionHandler(value = {
            ConstraintViolationException.class,
            IllegalArgumentException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<?> handleConstraint(Exception ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * Parses all the exception related to constraints on request body.
     *
     * @param ex exception to parse
     * @return well described json error
     */
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<?> handleMethodArgumentException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getFieldError();
        if (fieldError != null) {
            ErrorResponse errorResponse = new ErrorResponse(
                    fieldError.getField() + " " + fieldError.getDefaultMessage()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Parses all the rest of exceptions.
     *
     * @param ex exception to parse
     * @return well described json error
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
