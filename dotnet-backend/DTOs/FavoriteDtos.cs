namespace DocTruyen.UserService.DTOs;

public class FavoriteDto
{
    public long Id { get; set; }
    public long UserId { get; set; }
    public long StoryId { get; set; }
    public DateTime CreatedAt { get; set; }
}

public class AddFavoriteDto
{
    public long StoryId { get; set; }
}
