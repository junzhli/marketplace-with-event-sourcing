/* (C)2022 */
package com.jeremyli.common.events;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountTransferredEvent extends BaseEvent {
    private String from;
    private String to;
    private BigDecimal amount;

    @Override
    public String toString() {
        return "AccountTransferredEvent{"
                + "from="
                + from
                + ", to="
                + to
                + ", amount="
                + amount.toString()
                + '}';
    }

    public AccountTransferredEvent() {}

    public AccountTransferredEvent(String from, String to, BigDecimal amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }
}
