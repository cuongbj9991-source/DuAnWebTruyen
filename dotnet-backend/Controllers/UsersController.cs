using DocTruyen.UserService.DTOs;
using DocTruyen.UserService.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace DocTruyen.UserService.Controllers;

[ApiController]
[Route("api/users")]
[Produces("application/json")]
public class UsersController : ControllerBase
{
    private readonly IUserService _userService;
    private readonly IReadingProgressService _readingProgressService;
    private readonly ILogger<UsersController> _logger;

    public UsersController(
        IUserService userService,
        IReadingProgressService readingProgressService,
        ILogger<UsersController> logger)
    {
        _userService = userService;
        _readingProgressService = readingProgressService;
        _logger = logger;
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<UserDto>> GetUser(long id)
    {
        try
        {
            var user = await _userService.GetUserByIdAsync(id);
            if (user == null)
                return NotFound(new { message = "User not found" });

            return Ok(user);
        }
        catch (Exception ex)
        {
            _logger.LogError($"Error getting user: {ex.Message}");
            return StatusCode(500, new { message = "Internal server error" });
        }
    }

    [Authorize]
    [HttpPut("{id}")]
    public async Task<ActionResult<UserDto>> UpdateUser(long id, [FromBody] UpdateUserDto dto)
    {
        try
        {
            var userId = long.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? "0");
            if (userId != id)
                return Forbid();

            var user = await _userService.UpdateUserAsync(id, dto);
            return Ok(user);
        }
        catch (InvalidOperationException ex)
        {
            _logger.LogWarning($"Update failed: {ex.Message}");
            return NotFound(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            _logger.LogError($"Update error: {ex.Message}");
            return StatusCode(500, new { message = "Internal server error" });
        }
    }

    [Authorize]
    [HttpGet("{id}/reading-history")]
    public async Task<ActionResult<List<ReadingProgressDto>>> GetReadingHistory(long id)
    {
        try
        {
            var userId = long.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? "0");
            if (userId != id)
                return Forbid();

            var history = await _readingProgressService.GetUserReadingHistoryAsync(id);
            return Ok(history);
        }
        catch (Exception ex)
        {
            _logger.LogError($"Error getting reading history: {ex.Message}");
            return StatusCode(500, new { message = "Internal server error" });
        }
    }
}
