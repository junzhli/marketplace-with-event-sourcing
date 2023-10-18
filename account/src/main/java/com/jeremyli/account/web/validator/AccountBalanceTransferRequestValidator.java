/* (C)2022 */
package com.jeremyli.account.web.validator;

import com.jeremyli.account.web.constraint.CheckValidTransfer;
import com.jeremyli.account.web.request.AccountBalanceTransferRequest;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccountBalanceTransferRequestValidator
        implements ConstraintValidator<CheckValidTransfer, AccountBalanceTransferRequest> {
    @Override
    public void initialize(CheckValidTransfer constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(
            AccountBalanceTransferRequest value, ConstraintValidatorContext context) {
        return (value.getUserIdB() != value.getUserIdA());
    }
}
