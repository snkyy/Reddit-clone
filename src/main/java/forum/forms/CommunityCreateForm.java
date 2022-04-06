package forum.forms;

import lombok.Value;

// dane przekazywane do forum.logic.Communities.create()
@Value
public class CommunityCreateForm {
    public String name;
    public String description;
}
