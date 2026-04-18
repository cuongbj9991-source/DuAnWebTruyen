using DocTruyen.UserService.DTOs;
using DocTruyen.UserService.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace DocTruyen.UserService.Controllers;

[ApiController]
[Route("api/favorites")]
[Produces("application/json")]
[Authorize]
public class FavoritesController : ControllerBase
{
    private readonly IFavoriteService _favoriteService;
    private readonly ILogger<FavoritesController> _logger;

    public FavoritesController(IFavoriteService favoriteService, ILogger<FavoritesController> logger)
    {
        _favoriteService = favoriteService;
        _logger = logger;
    }

    [HttpGet]
    public async Task<ActionResult<List<FavoriteDto>>> GetFavorites()
    {
        try
        {
            var userId = long.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? "0");
            var favorites = await _favoriteService.GetUserFavoritesAsync(userId);
            return Ok(favorites);
        }
        catch (Exception ex)
        {
            _logger.LogError($"Error getting favorites: {ex.Message}");
            return StatusCode(500, new { message = "Internal server error" });
        }
    }

    [HttpPost("{storyId}")]
    public async Task<ActionResult<FavoriteDto>> AddFavorite(long storyId)
    {
        try
        {
            var userId = long.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? "0");
            var favorite = await _favoriteService.AddFavoriteAsync(userId, storyId);
            return Created(string.Empty, favorite);
        }
        catch (InvalidOperationException ex)
        {
            _logger.LogWarning($"Add favorite failed: {ex.Message}");
            return BadRequest(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            _logger.LogError($"Error adding favorite: {ex.Message}");
            return StatusCode(500, new { message = "Internal server error" });
        }
    }

    [HttpDelete("{storyId}")]
    public async Task<ActionResult> RemoveFavorite(long storyId)
    {
        try
        {
            var userId = long.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? "0");
            await _favoriteService.RemoveFavoriteAsync(userId, storyId);
            return NoContent();
        }
        catch (InvalidOperationException ex)
        {
            _logger.LogWarning($"Remove favorite failed: {ex.Message}");
            return NotFound(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            _logger.LogError($"Error removing favorite: {ex.Message}");
            return StatusCode(500, new { message = "Internal server error" });
        }
    }

    [HttpGet("{storyId}")]
    public async Task<ActionResult<object>> CheckFavorite(long storyId)
    {
        try
        {
            var userId = long.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? "0");
            var isFavorite = await _favoriteService.IsFavoriteAsync(userId, storyId);
            return Ok(new { isFavorite });
        }
        catch (Exception ex)
        {
            _logger.LogError($"Error checking favorite: {ex.Message}");
            return StatusCode(500, new { message = "Internal server error" });
        }
    }
}
