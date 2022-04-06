package impl;

import forum.validators.Validator;

import java.util.regex.Pattern;

// proste sprawdzanie regexów
public class DefaultValidator implements Validator {
    private final Pattern communityNamePattern = Pattern.compile("\\w{2,64}");
    private final Pattern userNamePattern = Pattern.compile("\\w{2,64}");
    private final Pattern emailPattern = Pattern.compile(".*@.*");

    @Override
    public boolean validateCommunityName(String name) {
        return communityNamePattern.matcher(name).matches();
    }

    @Override
    public boolean validateUserName(String name) {
        return userNamePattern.matcher(name).matches();
    }

    @Override
    public boolean validateEmail(String email) {
        return emailPattern.matcher(email).matches();
    }
}
