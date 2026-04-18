using DocTruyen.UserService.DTOs;
using DocTruyen.UserService.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace DocTruyen.UserService.Controllers;

[ApiController]
[Route("api/auth")]
[Produces("application/json")]
public class AuthController : ControllerBase
{
    private readonly IUserService _userService;
    private readonly ILogger<AuthController> _logger;

    public AuthController(IUserService userService, ILogger<AuthController> logger)
    {
        _userService = userService;
        _logger = logger;
    }

    [HttpPost("register")]
    public async Task<ActionResult<LoginResponseDto>> Register([FromBody] CreateUserDto dto)
    {
        try
        {
            var result = await _userService.RegisterAsync(dto);
            _logger.LogInformation($"User registered: {dto.Email}");
            return Created(string.Empty, result);
        }
        catch (InvalidOperationException ex)
        {
            _logger.LogWarning($"Registration failed: {ex.Message}");
            return BadRequest(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            _logger.LogError($"Registration error: {ex.Message}");
            return StatusCode(500, new { message = "Internal server error" });
        }
    }

    [HttpPost("login")]
    public async Task<ActionResult<LoginResponseDto>> Login([FromBody] LoginDto dto)
    {
        try
        {
            var result = await _userService.LoginAsync(dto);
            _logger.LogInformation($"User logged in: {dto.Email}");
            return Ok(result);
        }
        catch (InvalidOperationException ex)
        {
            _logger.LogWarning($"Login failed: {ex.Message}");
            return Unauthorized(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            _logger.LogError($"Login error: {ex.Message}");
            return StatusCode(500, new { message = "Internal server error" });
        }
    }
}
