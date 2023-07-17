package ru.martinov.paymentService.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.martinov.paymentService.models.Report;
import ru.martinov.paymentService.services.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    //главная страница
    @GetMapping()
    public String enterStartPage() {
        return "startPage";
    }

    //ввод платежа
    @GetMapping("/add")
    public String enterPayment() {
        return "add";
    }

    //добавление платежа
    @PostMapping("/isAdditionSuccess")
    public String addPayment(@RequestParam(value = "amount", defaultValue = "0") BigDecimal amount,
                             Model model) {

        if (paymentService.addPayment(amount)) {
            model.addAttribute("success", "EXECUTED");
        } else {
            model.addAttribute("success", "REJECTED");
        }

        return "isAdditionSuccess";
    }

    //ввод дат для отчета
    @GetMapping("/createReport")
    public String enterDates() {
        return "createReport";
    }

    //создание отчета
    @PostMapping("/isReportCreating")
    public String creatingReport(@RequestParam(value = "fromDate", defaultValue = "0001-01-01") LocalDate fromDate,
                                 @RequestParam(value = "toDate", defaultValue = "0001-01-01") LocalDate toDate,
                                 Model model) {

        int reportId = paymentService.createReport(fromDate, toDate);

        if (reportId < 0) {
            model.addAttribute("id", "Ошибка создания");

        } else {
            model.addAttribute("id", reportId);
        }

        return "isCreationSuccess";
    }

    //вывод списка отчетов из БД и предоставление ссылок на них
    @GetMapping("/enterReportId")
    public String enterReportId(Model model) {

        List<Report> reportsList = paymentService.getReportsList();

        if (reportsList == null) {
            model.addAttribute("reports", "Нет отчетов для получения");
            return "noReportsToDisplay";
        }

        model.addAttribute("reports", reportsList);
        return "enterReportId";
    }

    //вывод страницы выбранного отчета
    @GetMapping("/showReport/{id}")
    public String showReport(@PathVariable("id") int id, Model model) {

        model.addAttribute("report", paymentService.showReportById(id));

        return "showReport";
    }
}
