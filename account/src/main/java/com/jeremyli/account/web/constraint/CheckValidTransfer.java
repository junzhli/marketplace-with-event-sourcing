/* (C)2022 */
package com.jeremyli.account.web.constraint;

import com.jeremyli.account.web.validator.AccountBalanceTransferRequestValidator;
import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = AccountBalanceTransferRequestValidator.class)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckValidTransfer {
    String message() default "{com.jeremyli.account.web.validator.CheckValidTransfer.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
