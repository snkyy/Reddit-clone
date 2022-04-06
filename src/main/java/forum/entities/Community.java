package forum.entities;

import lombok.Value;

// dane społeczności (tylko do odczytu)
@Value
public class Community {
    public long id;
    public String name;
    public String description;
    public User owner;
    public long createdTime;
}
