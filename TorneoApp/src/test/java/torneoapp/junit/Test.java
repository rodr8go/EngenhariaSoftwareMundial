package torneoapp.junit;
import java.lang.annotation.*;
/** Marca um método como teste unitário. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {}
