package ru.martinov.paymentService.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.martinov.paymentService.models.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {}
