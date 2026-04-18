using AutoMapper;
using DocTruyen.UserService.Data;
using DocTruyen.UserService.DTOs;
using DocTruyen.UserService.Entities;
using Microsoft.EntityFrameworkCore;

namespace DocTruyen.UserService.Services;

public interface IReadingProgressService
{
    Task<ReadingProgressDto?> GetReadingProgressAsync(long userId, long storyId);
    Task<List<ReadingProgressDto>> GetUserReadingHistoryAsync(long userId);
    Task<ReadingProgressDto> UpdateReadingProgressAsync(long userId, long storyId, UpdateReadingProgressDto dto);
}

public class ReadingProgressService : IReadingProgressService
{
    private readonly AppDbContext _context;
    private readonly IMapper _mapper;
    private readonly ILogger<ReadingProgressService> _logger;

    public ReadingProgressService(AppDbContext context, IMapper mapper, ILogger<ReadingProgressService> logger)
    {
        _context = context;
        _mapper = mapper;
        _logger = logger;
    }

    public async Task<ReadingProgressDto?> GetReadingProgressAsync(long userId, long storyId)
    {
        var progress = await _context.ReadingProgresses
            .FirstOrDefaultAsync(p => p.UserId == userId && p.StoryId == storyId);

        return progress == null ? null : _mapper.Map<ReadingProgressDto>(progress);
    }

    public async Task<List<ReadingProgressDto>> GetUserReadingHistoryAsync(long userId)
    {
        var progresses = await _context.ReadingProgresses
            .Where(p => p.UserId == userId)
            .OrderByDescending(p => p.LastReadAt)
            .ToListAsync();

        return _mapper.Map<List<ReadingProgressDto>>(progresses);
    }

    public async Task<ReadingProgressDto> UpdateReadingProgressAsync(long userId, long storyId, UpdateReadingProgressDto dto)
    {
        var progress = await _context.ReadingProgresses
            .FirstOrDefaultAsync(p => p.UserId == userId && p.StoryId == storyId);

        if (progress == null)
        {
            progress = new ReadingProgress
            {
                UserId = userId,
                StoryId = storyId,
                LastChapterRead = dto.LastChapterRead,
                ScrollPosition = dto.ScrollPosition,
                LastReadAt = DateTime.UtcNow
            };

            _context.ReadingProgresses.Add(progress);
        }
        else
        {
            progress.LastChapterRead = dto.LastChapterRead;
            progress.ScrollPosition = dto.ScrollPosition;
            progress.LastReadAt = DateTime.UtcNow;
            _context.ReadingProgresses.Update(progress);
        }

        await _context.SaveChangesAsync();
        _logger.LogInformation($"Reading progress updated for user {userId}, story {storyId}");

        return _mapper.Map<ReadingProgressDto>(progress);
    }
}
