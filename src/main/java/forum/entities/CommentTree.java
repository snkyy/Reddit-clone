package forum.entities;

import lombok.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// pomocnicza klasa reprezentująca drzewo komentarzy
@Value
public class CommentTree {
    public Comment comment;
    public List<CommentTree> children;

    // konstruuje listę drzew komentarzy na podstawie listy korzeni i listy wszystkich węzłów
    public static List<CommentTree> build(List<Comment> comments, List<Comment> descendants) {
        var roots = new ArrayList<CommentTree>();
        var map = new HashMap<Long, CommentTree>();

        for (var comment : comments) {
            var root = new CommentTree(comment, new ArrayList<>());
            roots.add(root);
            map.put(comment.id, root);
        }

        for (var descendant : descendants) {
            var node = new CommentTree(descendant, new ArrayList<>());
            map.put(descendant.id, node);
        }

        for (var descendant : descendants) {
            if (descendant.parentId.isPresent()) {
                var parentId = descendant.parentId.getAsLong();
                if (map.containsKey(parentId))
                    map.get(parentId).children.add(map.get(descendant.id));
            }
        }

        return roots;
    }
}
