/* (C)2022 */
package com.jeremyli.account.web.controller;

import com.jeremyli.account.service.AccountService;
import com.jeremyli.account.web.request.AccountBalanceTransferRequest;
import com.jeremyli.account.web.request.AccountCreationRequest;
import com.jeremyli.account.web.response.*;
import com.jeremyli.common.web.response.GenericMessageResponse;
import com.jeremyli.common.web.response.IRestResponse;
import com.jeremyli.common.web.response.RestResponse;
import java.text.MessageFormat;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/api/accounts")
public class AccountController {
    private AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity createAccount(
            @RequestBody @Valid AccountCreationRequest accountCreationRequest) {
        try {
            var account =
                    accountService.createAccount(
                            accountCreationRequest.getUserId(),
                            accountCreationRequest.getName(),
                            accountCreationRequest.getBalance());
            log.info("user {} created", account.getUserId());
            IRestResponse<AccountCreationResponse> res =
                    RestResponse.createOkResponse(new AccountCreationResponse(account.getUserId()));
            return ResponseEntity.status(res.getHttpStatusCode()).body(res);
        } catch (DataIntegrityViolationException exception) {
            log.error("user {} already exists", accountCreationRequest.getUserId());
            IRestResponse<GenericMessageResponse> res =
                    RestResponse.createBadRequestResponse(
                            new GenericMessageResponse(
                                    MessageCode.ACCOUNT_ALREADY_EXIST,
                                    MessageFormat.format(
                                            "user {0} already exists",
                                            String.valueOf(accountCreationRequest.getUserId()))));
            return ResponseEntity.status(res.getHttpStatusCode()).body(res);
        }
    }

    @RequestMapping(value = "/balanceTransfer", method = RequestMethod.POST)
    public ResponseEntity<IRestResponse<AccountBalanceTransferResponse>> transferMoney(
            @RequestBody @Valid AccountBalanceTransferRequest accountBalanceTransferRequest) {
        accountService.transferFundsBetweenAccounts(
                accountBalanceTransferRequest.getUserIdA(),
                accountBalanceTransferRequest.getUserIdB(),
                accountBalanceTransferRequest.getAmount());
        IRestResponse<AccountBalanceTransferResponse> res =
                RestResponse.createOkResponse(new AccountBalanceTransferResponse(true));
        return ResponseEntity.status(res.getHttpStatusCode()).body(res);
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    public ResponseEntity<IRestResponse<AccountInfoResponse>> getAccount(
            @PathVariable(value = "userId") String userId) {
        var account = accountService.getAccountByUserId(userId);
        var res = RestResponse.createOkResponse(new AccountInfoResponse(account));
        return ResponseEntity.status(res.getHttpStatusCode()).body(res);
    }
}
