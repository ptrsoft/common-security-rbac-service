package com.synectiks.security.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synectiks.security.entities.User;
import com.synectiks.security.repositories.UserRepository;
import com.synectiks.security.security.AuthoritiesConstants;
import com.synectiks.security.web.rest.AccountResource;
import com.synectiks.security.web.rest.TestUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(value = UserController.class, secure = false)
@ContextConfiguration(locations =  {"classpath*:/application-context.xml"})
class UserControllerTest {

	@MockBean
	private UserRepository userRepository;

	private MockMvc mockMvc;

	User user1;
	User user2;

	private ObjectMapper mapper;

	@BeforeEach
	public void setup() {
		AccountResource accountUserMockResource = new AccountResource();
		this.mockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource)
//     .setControllerAdvice(exceptionTranslator)
				.build();
		user1 = new User();
		user1.setId(Long.valueOf(101));
		user1.setActive(true);
		user1.setAuthenticatedByUserName(true);
		user1.setUsername("daviddhawan");
		user1.setEmail("daviddhawan@email.com");
		user2 = new User();
		user2.setId(Long.valueOf(102));
		user2.setActive(false);
		user2.setAuthenticatedByUserName(false);
		user2.setUsername("varundhawan");
		user2.setEmail("varundhawan@email.com");
		mapper = new ObjectMapper();
	}

	@Test
	@WithMockUser(username = "test", roles = "ADMIN")
	public void testCreate() throws IOException, Exception {
		when(userRepository.save(Mockito.any(User.class))).thenReturn(user1);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/security/users/create")
                .accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(user1))
                .contentType(MediaType.APPLICATION_JSON);
            MvcResult result = mockMvc.perform(requestBuilder)
            		.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                    .andExpect(jsonPath("$.login").value("test"))
                    .andExpect(jsonPath("$.authorities").value(AuthoritiesConstants.ADMIN))
            		.andReturn();
            MockHttpServletResponse response = result.getResponse();
            String outputInJson = response.getContentAsString();
            assertEquals(HttpStatus.OK.value(), response.getStatus());
	}
	
	@Test
	@WithMockUser(username = "test", roles = "ADMIN")
	public void listAllTest() throws IOException, Exception {
		when(userRepository.findAll()).thenReturn(Stream.of(user1,user2).collect(Collectors.toList()));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/security/users/listAll")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
            MvcResult result = mockMvc.perform(requestBuilder)
            		.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                    .andExpect(jsonPath("$.login").value("test"))
                    .andExpect(jsonPath("$.authorities").value(AuthoritiesConstants.ADMIN))
            		.andReturn();
            MockHttpServletResponse response = result.getResponse();
            String outputInJson = response.getContentAsString();
            assertEquals(HttpStatus.OK.value(), response.getStatus());
	}
	
	@Test
	@WithMockUser(username = "test", roles = "ADMIN")
	public void findByIdTest() throws IOException, Exception {
		when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user1));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/security/users/101")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
            MvcResult result = mockMvc.perform(requestBuilder)
            		.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                    .andExpect(jsonPath("$.login").value("test"))
                    .andExpect(jsonPath("$.authorities").value(AuthoritiesConstants.ADMIN))
            		.andReturn();
            MockHttpServletResponse response = result.getResponse();
            String outputInJson = response.getContentAsString();
            assertEquals(HttpStatus.OK.value(), response.getStatus());
	}
	
	@Test
	@WithMockUser(username = "test", roles = "ADMIN")
	public void deleteByIdTest() throws IOException, Exception {
//		doNothing().when(permissionRepository.deleteById(Mockito.anyString()));
//		when(permissionRepository.findById(Mockito.any())).thenReturn(Mockito.any(VOID));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/security/users/delete/101")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
            MvcResult result = mockMvc.perform(requestBuilder)
            		.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                    .andExpect(jsonPath("$.login").value("test"))
                    .andExpect(jsonPath("$.authorities").value(AuthoritiesConstants.ADMIN))
            		.andReturn();
            MockHttpServletResponse response = result.getResponse();
            String outputInJson = response.getContentAsString();
            assertEquals(HttpStatus.OK.value(), response.getStatus());
	}
	
	@Test
	@WithMockUser(username = "test", roles = "ADMIN")
	public void updateTest() throws IOException, Exception {
		when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user1));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/security/users/update")
                .accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(user1))
                .contentType(MediaType.APPLICATION_JSON);
            MvcResult result = mockMvc.perform(requestBuilder)
            		.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                    .andExpect(jsonPath("$.login").value("test"))
                    .andExpect(jsonPath("$.authorities").value(AuthoritiesConstants.ADMIN))
            		.andReturn();
            MockHttpServletResponse response = result.getResponse();
            String outputInJson = response.getContentAsString();
            assertEquals(HttpStatus.OK.value(), response.getStatus());
	}
	
	@Test
	@WithMockUser(username = "test", roles = "ADMIN")
	public void deleteTest() throws IOException, Exception {
		when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user1));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/security/users/delete")
                .accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(user1))
                .contentType(MediaType.APPLICATION_JSON);
            MvcResult result = mockMvc.perform(requestBuilder)
            		.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                    .andExpect(jsonPath("$.login").value("test"))
                    .andExpect(jsonPath("$.authorities").value(AuthoritiesConstants.ADMIN))
            		.andReturn();
            MockHttpServletResponse response = result.getResponse();
            String outputInJson = response.getContentAsString();
            assertEquals(HttpStatus.OK.value(), response.getStatus());
	}
}
