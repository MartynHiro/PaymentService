## Цель :
Создать сервис на базе spring, maven генерирующий отчеты по существующим платежам за некий временной интервал.

1. Сервис должен реализовывать следующие методы АПИ:
   
    1)Запросить создание отчета за временной интервал. Метод принимает параметры: дата от, дата до. Возвращает айди отчета.
    2)Получить отчет по его айди. Формат отчета на усмотрение, можно просто txt в свободной форме.
3. У сервиса должна быть БД postgresql содержащая данные о платежах и отчетах. Структура бд задается init sql скриптами, желательно через flyway. Тестовая история платежей пусть создается так же в sql скриптах.
4. Платеж состоит из суммы, даты, статуса.
5. Сами отчеты хранить в файловой системе сервиса
6. Сервис должен быть спроектирован так, чтоб обрабатывать большие нагрузки и нормально работать, если будут запущены 2 инстанса сервиса на одну БД. Нужно использовать многопоточность.
7. Возможные ошибки и исключения должны обрабатываться и логироваться
8. В отчете должны сначала идти успешные платежи, потом все остальные. В рамках одного статуса отсортировать по сумме.

## Использованный стек
1. Spring Boot (Data JPA + Web MVC + Logging)
2. Flyway
3. Lombok
4. PostgreSQL (DBeaver)
5. Thymeleaf
6. Асинхронность
				




