/* (C)2022 */
package com.jeremyli.account;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeremyli.account.web.request.AccountCreationRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AccountApplication.class)
@AutoConfigureMockMvc
@EmbeddedKafka
class AccountIntegrationTests {

    private final String baseUri = "/api/accounts/";
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private MockMvc mockMvc;

    @Test
    void testAccountCreation() throws Exception {
        AccountCreationRequest accountCreationRequest =
                new AccountCreationRequest("123", "jeremy", new BigDecimal("1000"));
        var payload = objectMapper.writeValueAsString(accountCreationRequest);
        mockMvc.perform(
                        post(baseUri + "create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.responseStatus").value("OK"))
                .andExpect(jsonPath("$.response.id").value("123"));
    }
}
