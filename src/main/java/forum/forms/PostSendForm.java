package forum.forms;

import forum.entities.Community;
import lombok.Value;

// dane przekazywane do forum.logic.Posts.send()
@Value
public class PostSendForm {
    public Community community;
    public String title;
    public String content;
}
