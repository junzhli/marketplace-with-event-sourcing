/* (C)2022 */
package com.jeremyli.account.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(
        name = "transaction",
        uniqueConstraints = @UniqueConstraint(columnNames = {"transactionId", "userId"}))
@Entity(name = "transaction")
@Builder
public class Transaction {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private Account account;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;
}
