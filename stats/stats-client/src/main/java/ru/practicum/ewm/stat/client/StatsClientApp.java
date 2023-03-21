package ru.practicum.ewm.stat.client;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// todo этот файл нужно удалить, он не нужен, так как мы испльзуем клиент как библиотеку, а не как приложение
@SpringBootApplication
public class StatsClientApp {
    public static void main(String[] args) {
        SpringApplication.run(StatsClientApp.class, args);
    }
}