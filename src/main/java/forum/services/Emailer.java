package forum.services;

import forum.entities.User;

// interfejs do funkcji wysyłających e-maile
public interface Emailer {
    void emailUser(User user, String message); // wysyła wiadomość do użytkownika (o ile podał swój adres)
    void emailAdmin(String message); // wysyła wiadomość do administratora strony
}
