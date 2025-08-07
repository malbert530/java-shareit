package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserControllerTest {
    @MockBean
    UserService userService;
    private final ObjectMapper mapper;
    private final MockMvc mockMvc;
    private UserDto userDto;
    private final Long id = 1L;
    private final String name = "Name";
    private final String email = "name@mail.com";

    @BeforeEach
    void setData() {
        userDto = new UserDto();
        userDto.setName(name);
        userDto.setEmail(email);
    }

    @Test
    @SneakyThrows
    void createUserTest() {

        UserDto savedUser = new UserDto();
        savedUser.setId(id);
        savedUser.setName(name);
        savedUser.setEmail(email);

        when(userService.create(userDto)).thenReturn(savedUser);

        MvcResult mvcResult =
                mockMvc
                        .perform(
                                post("/users")
                                        .content(mapper.writeValueAsString(userDto))
                                        .characterEncoding(StandardCharsets.UTF_8)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();

        UserDto actualUserDto = mapper.readValue(responseBody, UserDto.class);

        assertNotNull(actualUserDto.getId());
        assertThat(actualUserDto).usingRecursiveComparison().ignoringFields("id").isEqualTo(userDto);
    }

    @Test
    @SneakyThrows
    void updateUserTest() {
        UserDto updatedUser = new UserDto();
        updatedUser.setId(id);
        updatedUser.setName(name);
        updatedUser.setEmail(email);

        when(userService.update(userDto, id)).thenReturn(updatedUser);

        MvcResult mvcResult =
                mockMvc
                        .perform(
                                patch("/users/" + id)
                                        .content(mapper.writeValueAsString(userDto))
                                        .characterEncoding(StandardCharsets.UTF_8)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();

        UserDto actualUserDto = mapper.readValue(responseBody, UserDto.class);

        assertEquals(actualUserDto.getId(), id);
        assertThat(actualUserDto).usingRecursiveComparison().ignoringFields("id").isEqualTo(userDto);
    }

    @Test
    @SneakyThrows
    void deleteUserTest() {

        UserDto deletedUser = new UserDto();
        deletedUser.setId(id);
        deletedUser.setName(name);
        deletedUser.setEmail(email);

        when(userService.delete(id)).thenReturn(deletedUser);

        MvcResult mvcResult =
                mockMvc
                        .perform(delete("/users/" + id))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();

        UserDto actualUserDto = mapper.readValue(responseBody, UserDto.class);

        assertThat(actualUserDto).usingRecursiveComparison().ignoringFields("id").isEqualTo(userDto);
    }

    @Test
    @SneakyThrows
    void getUserByIdTest() {
        UserDto user = new UserDto();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);

        when(userService.getUserById(id)).thenReturn(user);

        MvcResult mvcResult =
                mockMvc
                        .perform(get("/users/" + id))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();

        UserDto actualUserDto = mapper.readValue(responseBody, UserDto.class);

        assertThat(actualUserDto).usingRecursiveComparison().isEqualTo(user);
    }

}
