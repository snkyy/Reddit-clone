package forum.validators;

// interfejs łączący UserValidator i CommunityValidator
public interface Validator extends
        UserValidator,
        CommunityValidator { }
