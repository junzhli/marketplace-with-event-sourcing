/* (C)2022 */
package com.jeremyli.common.outbox;

import com.jeremyli.common.events.BaseEvent;
import com.vladmihalcea.hibernate.type.json.JsonType;
import java.util.Date;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

@TypeDefs({@TypeDef(name = "json", typeClass = JsonType.class)})
@Entity
@Table(name = "outboxEvent")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OutboxEventModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timeStamp", nullable = false)
    private Date timeStamp;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "key")
    private String key;

    @Type(type = "json")
    @Column(columnDefinition = "json", nullable = false)
    private BaseEvent eventData;
}
