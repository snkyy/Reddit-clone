package impl;

import forum.entities.User;
import forum.services.Emailer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// sztuczne wysyłanie e-maili poprzez zapisywanie wiadomości do pliku
public class FakeEmailer implements Emailer {
    private static final String file = "emails.txt";
    private static final String adminAddress = "admin";
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private synchronized void send(String address, String message) {
        try (var writer = new PrintWriter(new FileWriter(file, true))) {
            var time = dateFormat.format(new Date());
            writer.println(time + "\t" + address + "\t" + message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void emailUser(User user, String message) {
        user.email.ifPresent(s -> send(s, message));
    }

    @Override
    public void emailAdmin(String message) {
        send(adminAddress, message);
    }
}
