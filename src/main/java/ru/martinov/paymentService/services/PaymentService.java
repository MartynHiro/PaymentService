package ru.martinov.paymentService.services;

import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import ru.martinov.paymentService.models.Payment;
import ru.martinov.paymentService.models.Report;
import ru.martinov.paymentService.repositories.PaymentRepository;
import ru.martinov.paymentService.repositories.ReportRepository;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ReportRepository reportRepository;

    //путь для отчетов
    private static final String PATH = "./src/main/resources/reports/";
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, ReportRepository reportRepository) {
        this.paymentRepository = paymentRepository;
        this.reportRepository = reportRepository;
    }

    //добавление нового платежа
    @Transactional
    public boolean addPayment(BigDecimal amount) {

        //проверка на то был ли введен платеж
        int comparisonResult = amount.compareTo(BigDecimal.valueOf(-1));
        boolean isNegative = amount.compareTo(BigDecimal.ZERO) < 0;

        if (comparisonResult == 0 || isNegative) {
            log.debug("Некорректный платеж {}", amount);

            lock.writeLock().lock();

            try {

                Payment payment = new Payment(amount, LocalDate.now(), "REJECTED");

                paymentRepository.save(payment);

            } finally {
                lock.writeLock().unlock();
            }
            return false;
        }


        lock.writeLock().lock();

        Payment payment = new Payment(amount, LocalDate.now(), "EXECUTED");

        try {
            paymentRepository.save(payment);

        } catch (TransactionSystemException e) {
            log.debug("Ошибка транзакции создания отчета в методе addPayment");
            e.printStackTrace();
            return false;
        } catch (DataAccessException e) {
            log.debug("Ошибка доступа к БД в методе addPayment");
            e.printStackTrace();
            return false;
        } catch (PersistenceException e) {
            log.debug("Нарушение правил работы с сущностями в методе addPayment");
            e.printStackTrace();
            return false;

        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

    //метод создания отчета по платежам из БД по выбранным датам
    @Transactional
    public int createReport(LocalDate fromDate, LocalDate toDate) {

        if (fromDate.toString().equals("0001-01-01") || toDate.toString().equals("0001-01-01")) return -1;

        lock.writeLock().lock();
        try {
            Report report = new Report(fromDate, toDate);

            int reportId = saveReportAndGetId(report);

            List<Payment> payments = paymentRepository.findByDateBetween(fromDate, toDate);

            boolean isFileCreate = createReportFile(fromDate, toDate, payments, reportId);

            if (!isFileCreate) reportId = -1;

            return reportId;

        } catch (TransactionSystemException e) {
            log.debug("Ошибка транзакции создания отчета в методе createReport");
            e.printStackTrace();
            return -1;
        } catch (DataAccessException e) {
            log.debug("Ошибка доступа к БД в методе createReport");
            e.printStackTrace();
            return -1;
        } catch (PersistenceException e) {
            log.debug("Нарушение правил работы с сущностями в методе saveReportAndGetId");
            e.printStackTrace();
            return -1;

        } finally {
            lock.writeLock().unlock();
        }
    }

    //сохранение отчета в БД и получение его id
    @Transactional
    private int saveReportAndGetId(Report report) {

        try {
            Report savedReport = reportRepository.save(report);
            return savedReport.getId();

        } catch (TransactionSystemException e) {
            log.debug("Ошибка транзакции создания отчета в методе saveReportAndGetId");
            e.printStackTrace();
            return -1;
        } catch (DataAccessException e) {
            log.debug("Ошибка доступа к БД в методе saveReportAndGetId");
            e.printStackTrace();
            return -1;
        } catch (PersistenceException e) {
            log.debug("Нарушение правил работы с сущностями в методе saveReportAndGetId");
            e.printStackTrace();
            return -1;
        }
    }

    //создание файла отчета
    private boolean createReportFile(LocalDate fromDate, LocalDate toDate, List<Payment> payments, int reportId) {

        StringBuilder textForReportSB = new StringBuilder();

        textForReportSB.append("Дата ОТ: ").append(fromDate).append("\r\n");
        textForReportSB.append("Дата ДО: ").append(toDate).append("\r\n");
        textForReportSB.append("\r\n");  //тело отчета отделяем двойным CRLF

        //формирование упорядоченных данных для записи в отчет
        payments.stream()
                .filter(payment -> payment.getStatus().equals("EXECUTED"))
                .sorted(Comparator.comparing(Payment::getAmount))
                .forEachOrdered(payment -> textForReportSB.append(payment).append("\r\n"));

        payments.stream()
                .filter(payment -> payment.getStatus().equals("REJECTED"))
                .sorted(Comparator.comparing(Payment::getAmount))
                .forEachOrdered(payment -> textForReportSB.append(payment).append("\r\n"));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PATH + reportId + ".txt"))) {

            writer.write(textForReportSB.toString());

        } catch (IOException e) {
            log.debug("Ошибка в создании файла {}", PATH + reportId + ".txt методом createReportFile");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //получение списка отчетов
    @Transactional
    public List<Report> getReportsList() {

        lock.readLock().lock();
        try {
            return reportRepository.findAll();

        } catch (TransactionSystemException e) {
            log.debug("Ошибка транзакции создания отчета в методе getReportsList");
            e.printStackTrace();
            return null;
        } catch (DataAccessException e) {
            log.debug("Ошибка доступа к БД в методе getReportsList");
            e.printStackTrace();
            return null;
        } catch (PersistenceException e) {
            log.debug("Нарушение правил работы с сущностями в методе getReportsList");
            e.printStackTrace();
            return null;

        } finally {
            lock.readLock().unlock();
        }
    }

    //получение отчета по его id
    public String showReportById(int id) {

        StringBuilder path = new StringBuilder();

        path.append("src/main/resources/reports/").append(id).append(".txt");

        String content = "";

        lock.readLock().lock();
        try {
            content = new String(Files.readAllBytes(Paths.get(path.toString())));

        } catch (IOException e) {
            log.debug("Ошибка в чтении файла {} методом showReportById", path);
            e.printStackTrace();
            return "Файл отчета не найден";
        } finally {
            lock.readLock().unlock();
        }
        return content;
    }
}