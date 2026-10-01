package com.example;

import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class RabbitmqTutorialsApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(RabbitmqTutorialsApplication.class, args);

        Sender sender = context.getBean(Sender.class);

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;

            while (running) {
                System.out.println("\n--- Menú ---");
                System.out.println("1. Enviar mensaje");
                System.out.println("2. Enviar múltiples mensajes");
                System.out.println("3. Salir");
                System.out.print("Selecciona una opción: ");

                String option = scanner.nextLine().trim();

                switch (option) {
                    case "1":
                        System.out.print("Escribe el mensaje: ");
                        sender.sendMessage(scanner.nextLine());
                        break;

                    case "2":
                        System.out.print("¿Cuántos mensajes? ");

                        try {
                            int count = Integer.parseInt(scanner.nextLine());

                            for (int i = 1; i <= count; i++) {
                                sender.sendMessage(
                                        "Mensaje #" + i + " - Hello RabbitMQ!"
                                );
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Introduce un número entero.");
                        }
                        break;

                    case "3":
                        running = false;
                        break;

                    default:
                        System.out.println("Opción no válida.");
                }
            }
        } finally {
            context.close();
        }
    }
}