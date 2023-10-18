/* (C)2022 */
package com.jeremyli.common.events;

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

@Table(
        name = "eventStore",
        uniqueConstraints = @UniqueConstraint(columnNames = {"aggregateIdentifier", "version"}))
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Builder
@TypeDefs({@TypeDef(name = "json", typeClass = JsonType.class)})
public class EventModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timeStamp", nullable = false)
    private Date timeStamp;

    @Column(name = "aggregateIdentifier", nullable = false)
    private String aggregateIdentifier;

    @Column(name = "aggregateType", nullable = false)
    private String aggregateType;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "eventType", nullable = false)
    private String eventType;

    @Type(type = "json")
    @Column(columnDefinition = "json", nullable = false)
    private BaseEvent eventData;
}
