package forum.views;

import forum.entities.Community;
import lombok.Value;

import java.util.List;

@Value
public class CommunityListView {
    public long pageNumber;
    public List<Community> communities;
}
