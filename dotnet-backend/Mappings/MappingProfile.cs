using AutoMapper;
using DocTruyen.UserService.DTOs;
using DocTruyen.UserService.Entities;

namespace DocTruyen.UserService.Mappings;

public class MappingProfile : Profile
{
    public MappingProfile()
    {
        CreateMap<User, UserDto>();
        CreateMap<CreateUserDto, User>();
        CreateMap<UpdateUserDto, User>();

        CreateMap<Favorite, FavoriteDto>();
        CreateMap<AddFavoriteDto, Favorite>();

        CreateMap<ReadingProgress, ReadingProgressDto>();
        CreateMap<UpdateReadingProgressDto, ReadingProgress>();
    }
}
