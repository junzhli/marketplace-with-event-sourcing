/* (C)2022 */
package com.jeremyli.account.infra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStateVerifier {

    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public ApplicationStateVerifier(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void verifyAndPublish() {
        AvailabilityChangeEvent.publish(
                this.eventPublisher, new RuntimeException("nothing good"), LivenessState.BROKEN);
    }
}
