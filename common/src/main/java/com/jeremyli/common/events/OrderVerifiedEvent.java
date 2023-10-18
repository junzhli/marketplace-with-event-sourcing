/* (C)2022 */
package com.jeremyli.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public class OrderVerifiedEvent extends BaseEvent {
    /* Get the following done before moving to next phase */
    private boolean accountAmountDebited;
}
