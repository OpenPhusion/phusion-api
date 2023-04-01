package cloud.phusion.application;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InboundEndpoint {

    String address(); // The relative URL to listen to

    String connectionKeyInConfig() default ""; // By default, use connectionId as the connection key. All endpoints must have the same value

    String connectionKeyInReqeust();

}
