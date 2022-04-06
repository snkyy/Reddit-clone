package forum.entities;

import lombok.Value;

import java.util.OptionalLong;

// dane postu (tylko do odczytu)
@Value
public class Post {
    public long id;
    public String title;
    public String content;
    public User sender;
    public Community community;
    public long sentTime;
    public long points;
    public OptionalLong editedTime;
}
