package forum.validators;

// interfejs do funkcji sprawdzających poprawność nazwy użytkownika i adresu e-mail;
// używany przez forum.logic.Users
public interface UserValidator {
    boolean validateUserName(String name);
    boolean validateEmail(String email);
}
