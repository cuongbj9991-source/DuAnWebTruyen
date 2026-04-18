using System;

namespace DocTruyen.UserService.Entities;

public class Favorite
{
    public long Id { get; set; }
    public long UserId { get; set; }
    public long StoryId { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
