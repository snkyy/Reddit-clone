package forum.entities;

import lombok.Value;

import java.util.OptionalLong;

// dane komentarza (tylko do odczytu)
@Value
public class Comment {
    public long id;
    public String content;
    public User sender;
    public Post post;
    public OptionalLong parentId; // ze względu na rekurencyjną naturę komentarzy, trzymamy tylko id rodzica
    public long sentTime;
    public long points;
    public OptionalLong editedTime;
}
