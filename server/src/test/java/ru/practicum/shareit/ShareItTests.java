package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ShareItTests {

	private final TestRestTemplate testRestTemplate;
	private final String urlUsers = "http://localhost:9090/users";
	private final String urlItems = "http://localhost:9090/items";
	private final UserRepository userRepository;
	private final ItemRepository itemRepository;

	@BeforeEach
	public void setup() {
		testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
	}

	@Test
	void contextLoads() {
	}

	@Test
	void updateUser_newName() {
		String name = "Name";
		String email = "name@mail.com";
		UserDto userToSave = new UserDto();
		userToSave.setName(name);
		userToSave.setEmail(email);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		HttpEntity<UserDto> request = new HttpEntity<>(userToSave, headers);

		ResponseEntity<UserDto> response = testRestTemplate
				.exchange(urlUsers, HttpMethod.POST, request, UserDto.class);

		assertNotNull(response);
		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		UserDto savedUser = response.getBody();

		assertNotNull(savedUser.getId());
		Long id = savedUser.getId();

		UserDto userToUpdate = new UserDto();
		String newName = "New Name";
		userToUpdate.setName(newName);

		HttpEntity<UserDto> requestToUpdate = new HttpEntity<>(userToUpdate, headers);
		ResponseEntity<UserDto> resp = testRestTemplate
				.exchange(urlUsers + "/" + id, HttpMethod.PATCH, requestToUpdate, UserDto.class);

		assertNotNull(resp);
		assertEquals(200, resp.getStatusCode().value());
		assertNotNull(resp.getBody());

		UserDto updatedUser = resp.getBody();
		assertEquals(newName, updatedUser.getName());
	}

	@Test
	void updateUser_newEmail() {
		String name = "Name";
		String email = "name@mail.com";
		UserDto userToSave = new UserDto();
		userToSave.setName(name);
		userToSave.setEmail(email);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		HttpEntity<UserDto> request = new HttpEntity<>(userToSave, headers);

		ResponseEntity<UserDto> response = testRestTemplate
				.exchange(urlUsers, HttpMethod.POST, request, UserDto.class);

		assertNotNull(response);
		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		UserDto savedUser = response.getBody();

		assertNotNull(savedUser.getId());
		Long id = savedUser.getId();

		UserDto userToUpdate = new UserDto();
		String newEmail = "new@mail.com";
		userToUpdate.setEmail(newEmail);

		HttpEntity<UserDto> requestToUpdate = new HttpEntity<>(userToUpdate, headers);
		ResponseEntity<UserDto> resp = testRestTemplate
				.exchange(urlUsers + "/" + id, HttpMethod.PATCH, requestToUpdate, UserDto.class);

		assertNotNull(resp);
		assertEquals(200, resp.getStatusCode().value());
		assertNotNull(resp.getBody());

		UserDto updatedUser = resp.getBody();
		assertEquals(newEmail, updatedUser.getEmail());
	}

	@Test
	void updateItem_newName() {
		String name = "Name";
		String email = "name@mail.com";
		UserDto userToSave = new UserDto();
		userToSave.setName(name);
		userToSave.setEmail(email);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		HttpEntity<UserDto> request = new HttpEntity<>(userToSave, headers);

		ResponseEntity<UserDto> response = testRestTemplate
				.exchange(urlUsers, HttpMethod.POST, request, UserDto.class);

		assertNotNull(response);
		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		UserDto savedUser = response.getBody();

		assertNotNull(savedUser.getId());
		Long id = savedUser.getId();

		ItemDto itemToSave = new ItemDto();
		itemToSave.setName("Item Name");
		itemToSave.setAvailable(true);
		itemToSave.setDescription("Item Description");

		headers.set("X-Sharer-User-Id", id.toString());
		HttpEntity<ItemDto> requestItems = new HttpEntity<>(itemToSave, headers);

		ResponseEntity<ItemDto> responseItem = testRestTemplate
				.exchange(urlItems, HttpMethod.POST, requestItems, ItemDto.class);

		assertNotNull(responseItem);
		assertEquals(200, responseItem.getStatusCode().value());
		assertNotNull(responseItem.getBody());
		ItemDto savedItem = responseItem.getBody();

		assertNotNull(savedItem.getId());
		Long itemId = savedItem.getId();

		ItemDto itemToUpdate = new ItemDto();
		String newItemName = "New item";
		itemToUpdate.setName(newItemName);


		HttpEntity<ItemDto> requestToUpdateItem = new HttpEntity<>(itemToUpdate, headers);
		ResponseEntity<ItemDto> responseUpdatedItem = testRestTemplate
				.exchange(urlItems + "/" + itemId, HttpMethod.PATCH, requestToUpdateItem, ItemDto.class);

		assertNotNull(responseUpdatedItem);
		assertEquals(200, responseUpdatedItem.getStatusCode().value());
		assertNotNull(responseUpdatedItem.getBody());

		ItemDto updatedItem = responseUpdatedItem.getBody();
		assertEquals(newItemName, updatedItem.getName());
	}

	@AfterEach
	void tearDown() {
		itemRepository.deleteAll();
		userRepository.deleteAll();
	}

}
