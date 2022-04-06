package forum.validators;

// interfejs do funkcji sprawdzającej poprawność nazwy społeczności;
// używany przez forum.logic.Communities
public interface CommunityValidator {
    boolean validateCommunityName(String name);
}
