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
import com.synectiks.security.entities.Role;
import com.synectiks.security.repositories.RoleRepository;
import com.synectiks.security.security.AuthoritiesConstants;
import com.synectiks.security.web.rest.AccountResource;
import com.synectiks.security.web.rest.TestUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(value = RoleController.class, secure = false)
@ContextConfiguration(locations =  {"classpath*:/application-context.xml"})
class RoleControllerTest {

	@MockBean
	private RoleRepository roleRepository;

	private MockMvc mockMvc;

	Role role1;
	Role role2;

	private ObjectMapper mapper;

	@BeforeEach
	public void setup() {
		AccountResource accountUserMockResource = new AccountResource();
		this.mockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource)
//     .setControllerAdvice(exceptionTranslator)
				.build();
		role1 = new Role();
		role1.setId("101");
		role1.setName("Edtior Role");
		role2=new Role();
		role2.setId("102");
		role2.setName("Manager Permission");
		mapper = new ObjectMapper();
	}

	@Test
	@WithMockUser(username = "test", roles = "ADMIN")
	public void testCreate() throws IOException, Exception {
		when(roleRepository.save(Mockito.any(Role.class))).thenReturn(role1);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/security/roles/create")
                .accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(role1))
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
		when(roleRepository.findAll()).thenReturn(Stream.of(role1,role2).collect(Collectors.toList()));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/security/roles/listAll")
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
		when(roleRepository.findById(Mockito.any())).thenReturn(Optional.of(role1));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/security/roles/101")
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
                .post("/security/roles/delete/101")
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
		when(roleRepository.findById(Mockito.any())).thenReturn(Optional.of(role1));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/security/roles/update")
                .accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(role1))
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
		when(roleRepository.findById(Mockito.any())).thenReturn(Optional.of(role1));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/security/roles/delete")
                .accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(role1))
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
