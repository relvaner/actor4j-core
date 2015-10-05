package actor4j.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target(value=TYPE)
@Retention(value=RUNTIME)
public @interface Stateless {
}
