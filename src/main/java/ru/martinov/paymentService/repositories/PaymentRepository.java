package ru.martinov.paymentService.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.martinov.paymentService.models.Payment;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    //метод для получения всех платежей в промежутке заданных дат
    List<Payment> findByDateBetween(LocalDate fromDate, LocalDate toDate);
}
