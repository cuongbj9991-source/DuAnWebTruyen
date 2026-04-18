using System;

namespace DocTruyen.UserService.Entities;

public class ReadingProgress
{
    public long Id { get; set; }
    public long UserId { get; set; }
    public long StoryId { get; set; }
    public int LastChapterRead { get; set; }
    public int ScrollPosition { get; set; }
    public DateTime LastReadAt { get; set; } = DateTime.UtcNow;
}
