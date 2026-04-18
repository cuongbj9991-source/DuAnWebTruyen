using DocTruyen.UserService.DTOs;
using DocTruyen.UserService.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace DocTruyen.UserService.Controllers;

[ApiController]
[Route("api/reading-progress")]
[Produces("application/json")]
[Authorize]
public class ReadingProgressController : ControllerBase
{
    private readonly IReadingProgressService _readingProgressService;
    private readonly ILogger<ReadingProgressController> _logger;

    public ReadingProgressController(IReadingProgressService readingProgressService, ILogger<ReadingProgressController> logger)
    {
        _readingProgressService = readingProgressService;
        _logger = logger;
    }

    [HttpGet("story/{storyId}")]
    public async Task<ActionResult<ReadingProgressDto>> GetReadingProgress(long storyId)
    {
        try
        {
            var userId = long.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? "0");
            var progress = await _readingProgressService.GetReadingProgressAsync(userId, storyId);
            
            if (progress == null)
                return NotFound(new { message = "No reading progress found" });

            return Ok(progress);
        }
        catch (Exception ex)
        {
            _logger.LogError($"Error getting reading progress: {ex.Message}");
            return StatusCode(500, new { message = "Internal server error" });
        }
    }

    [HttpPut("story/{storyId}")]
    public async Task<ActionResult<ReadingProgressDto>> UpdateReadingProgress(
        long storyId,
        [FromBody] UpdateReadingProgressDto dto)
    {
        try
        {
            var userId = long.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? "0");
            var progress = await _readingProgressService.UpdateReadingProgressAsync(userId, storyId, dto);
            return Ok(progress);
        }
        catch (Exception ex)
        {
            _logger.LogError($"Error updating reading progress: {ex.Message}");
            return StatusCode(500, new { message = "Internal server error" });
        }
    }
}
