using AutoMapper;
using BCrypt.Net;
using DocTruyen.UserService.Data;
using DocTruyen.UserService.DTOs;
using DocTruyen.UserService.Entities;
using Microsoft.EntityFrameworkCore;

namespace DocTruyen.UserService.Services;

public interface IUserService
{
    Task<UserDto?> GetUserByIdAsync(long id);
    Task<UserDto?> GetUserByEmailAsync(string email);
    Task<LoginResponseDto> RegisterAsync(CreateUserDto dto);
    Task<LoginResponseDto> LoginAsync(LoginDto dto);
    Task<UserDto> UpdateUserAsync(long id, UpdateUserDto dto);
}

public class UserService : IUserService
{
    private readonly AppDbContext _context;
    private readonly IMapper _mapper;
    private readonly ITokenService _tokenService;
    private readonly ILogger<UserService> _logger;

    public UserService(AppDbContext context, IMapper mapper, ITokenService tokenService, ILogger<UserService> logger)
    {
        _context = context;
        _mapper = mapper;
        _tokenService = tokenService;
        _logger = logger;
    }

    public async Task<UserDto?> GetUserByIdAsync(long id)
    {
        var user = await _context.Users.FindAsync(id);
        return user == null ? null : _mapper.Map<UserDto>(user);
    }

    public async Task<UserDto?> GetUserByEmailAsync(string email)
    {
        var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == email);
        return user == null ? null : _mapper.Map<UserDto>(user);
    }

    public async Task<LoginResponseDto> RegisterAsync(CreateUserDto dto)
    {
        // Check if user exists
        var existingUser = await _context.Users.FirstOrDefaultAsync(u => u.Email == dto.Email);
        if (existingUser != null)
        {
            throw new InvalidOperationException("Email already registered");
        }

        var user = new User
        {
            Username = dto.Username,
            Email = dto.Email,
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(dto.Password),
            EmailVerified = true,
            IsPremium = false
        };

        _context.Users.Add(user);
        await _context.SaveChangesAsync();

        _logger.LogInformation($"User {user.Id} registered successfully");

        var token = _tokenService.GenerateToken(user.Id, user.Email);
        return new LoginResponseDto
        {
            User = _mapper.Map<UserDto>(user),
            Token = token
        };
    }

    public async Task<LoginResponseDto> LoginAsync(LoginDto dto)
    {
        var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == dto.Email);
        if (user == null)
        {
            throw new InvalidOperationException("Invalid email or password");
        }

        if (!BCrypt.Net.BCrypt.Verify(dto.Password, user.PasswordHash))
        {
            throw new InvalidOperationException("Invalid email or password");
        }

        var token = _tokenService.GenerateToken(user.Id, user.Email);
        return new LoginResponseDto
        {
            User = _mapper.Map<UserDto>(user),
            Token = token
        };
    }

    public async Task<UserDto> UpdateUserAsync(long id, UpdateUserDto dto)
    {
        var user = await _context.Users.FindAsync(id);
        if (user == null)
        {
            throw new InvalidOperationException("User not found");
        }

        if (!string.IsNullOrEmpty(dto.Username))
            user.Username = dto.Username;
        if (!string.IsNullOrEmpty(dto.AvatarUrl))
            user.AvatarUrl = dto.AvatarUrl;
        if (!string.IsNullOrEmpty(dto.Bio))
            user.Bio = dto.Bio;

        user.UpdatedAt = DateTime.UtcNow;
        _context.Users.Update(user);
        await _context.SaveChangesAsync();

        _logger.LogInformation($"User {id} updated");
        return _mapper.Map<UserDto>(user);
    }
}
