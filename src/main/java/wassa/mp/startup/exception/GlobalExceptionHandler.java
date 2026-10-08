package wassa.mp.startup.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErreurApi> handleResourceNotFound(ResourceNotFoundException ex) {
        ErreurApi error = new ErreurApi(HttpStatus.NOT_FOUND.value(), "RESOURCE_NOT_FOUND", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }
    @ExceptionHandler(NonAutoriseException.class)
    public ResponseEntity<ErreurApi> handleNonAutorise(NonAutoriseException ex) {
        ErreurApi error = new ErreurApi(HttpStatus.FORBIDDEN.value(), "ACCESS_DENIED", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }
    @ExceptionHandler(OperationInterditeException.class)
    public ResponseEntity<ErreurApi> handleEtapeIncomplete(OperationInterditeException ex) {
        ErreurApi error = new ErreurApi(HttpStatus.BAD_REQUEST.value(), "ETAPE_INCOMPLETE", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }
}