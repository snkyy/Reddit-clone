package forum.logic;

import lombok.AllArgsConstructor;

// klasa realizująca logikę biznesową całego forum (tematycznie podzielona na cztery klasy)
@AllArgsConstructor
public class Forum {
    private final Users users;
    private final Communities communities;
    private final Posts posts;
    private final Comments comments;

    public Users users() {
        return users;
    }

    public Communities communities() {
        return communities;
    }

    public Posts posts() {
        return posts;
    }

    public Comments comments() {
        return comments;
    }
}
