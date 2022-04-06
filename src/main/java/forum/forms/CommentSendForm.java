package forum.forms;

import forum.entities.Comment;
import forum.entities.Post;
import lombok.Value;

import java.util.Optional;

// dane przekazywane do forum.logic.Comments.send()
@Value
public class CommentSendForm {
    public Post post;
    public String content;
    public Optional<Comment> parent;
}
