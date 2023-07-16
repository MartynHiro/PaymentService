package ru.martinov.paymentService.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

//сущность платежа
@Data
@NoArgsConstructor
@Entity
@Table(name = "Payments")
public class Payment {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private int id;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "payment_date")
    private LocalDate date;

    @Column(name = "status")
    private String status;

    public Payment(BigDecimal amount, LocalDate date, String status) {
        this.amount = amount;
        this.date = date;
        this.status = status;
    }
}
