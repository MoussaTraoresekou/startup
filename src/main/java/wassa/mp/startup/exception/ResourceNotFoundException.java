package wassa.mp.startup.exception;

public class ResourceNotFoundException extends  RuntimeException{
      public ResourceNotFoundException(String message){
          super(message);
      }
}
