using AutoMapper;
using DocTruyen.UserService.Data;
using DocTruyen.UserService.DTOs;
using DocTruyen.UserService.Entities;
using Microsoft.EntityFrameworkCore;

namespace DocTruyen.UserService.Services;

public interface IFavoriteService
{
    Task<List<FavoriteDto>> GetUserFavoritesAsync(long userId);
    Task<FavoriteDto> AddFavoriteAsync(long userId, long storyId);
    Task RemoveFavoriteAsync(long userId, long storyId);
    Task<bool> IsFavoriteAsync(long userId, long storyId);
}

public class FavoriteService : IFavoriteService
{
    private readonly AppDbContext _context;
    private readonly IMapper _mapper;
    private readonly ILogger<FavoriteService> _logger;

    public FavoriteService(AppDbContext context, IMapper mapper, ILogger<FavoriteService> logger)
    {
        _context = context;
        _mapper = mapper;
        _logger = logger;
    }

    public async Task<List<FavoriteDto>> GetUserFavoritesAsync(long userId)
    {
        var favorites = await _context.Favorites
            .Where(f => f.UserId == userId)
            .ToListAsync();

        return _mapper.Map<List<FavoriteDto>>(favorites);
    }

    public async Task<FavoriteDto> AddFavoriteAsync(long userId, long storyId)
    {
        var existing = await _context.Favorites
            .FirstOrDefaultAsync(f => f.UserId == userId && f.StoryId == storyId);

        if (existing != null)
        {
            throw new InvalidOperationException("Already in favorites");
        }

        var favorite = new Favorite
        {
            UserId = userId,
            StoryId = storyId
        };

        _context.Favorites.Add(favorite);
        await _context.SaveChangesAsync();

        _logger.LogInformation($"Story {storyId} added to favorites for user {userId}");
        return _mapper.Map<FavoriteDto>(favorite);
    }

    public async Task RemoveFavoriteAsync(long userId, long storyId)
    {
        var favorite = await _context.Favorites
            .FirstOrDefaultAsync(f => f.UserId == userId && f.StoryId == storyId);

        if (favorite == null)
        {
            throw new InvalidOperationException("Favorite not found");
        }

        _context.Favorites.Remove(favorite);
        await _context.SaveChangesAsync();

        _logger.LogInformation($"Story {storyId} removed from favorites for user {userId}");
    }

    public async Task<bool> IsFavoriteAsync(long userId, long storyId)
    {
        return await _context.Favorites
            .AnyAsync(f => f.UserId == userId && f.StoryId == storyId);
    }
}
