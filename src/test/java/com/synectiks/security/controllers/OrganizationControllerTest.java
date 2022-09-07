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
import com.synectiks.security.entities.Organization;
import com.synectiks.security.repositories.OrganizationRepository;
import com.synectiks.security.security.AuthoritiesConstants;
import com.synectiks.security.web.rest.AccountResource;
import com.synectiks.security.web.rest.TestUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(value = OrganizationController.class, secure = false)
@ContextConfiguration(locations =  {"classpath*:/application-context.xml"})
class OrganizationControllerTest {

	@MockBean
	private OrganizationRepository organizationRepository;

	private MockMvc mockMvc;

	Organization organization1;
	Organization organization2;

	private ObjectMapper mapper;

	@BeforeEach
	public void setup() {
		AccountResource accountUserMockResource = new AccountResource();
		this.mockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource)
//     .setControllerAdvice(exceptionTranslator)
				.build();
		organization1 = new Organization();
		organization1.setId("101");
		organization1.setName("GSCS");
		organization1.setEmail("info@gscs.com");
		organization2=new Organization();
		organization2.setId("102");
		organization2.setName("Synectis");
		organization2.setEmail("info@Synectis.com");
		mapper = new ObjectMapper();
	}

	@Test
	@WithMockUser(username = "test", roles = "ADMIN")
	public void testCreate() throws IOException, Exception {
		when(organizationRepository.save(Mockito.any(Organization.class))).thenReturn(organization1);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/security/organization/create")
                .accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(organization1))
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
		when(organizationRepository.findAll()).thenReturn(Stream.of(organization1,organization2).collect(Collectors.toList()));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/security/organization/listAll")
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
		when(organizationRepository.findById(Mockito.any())).thenReturn(Optional.of(organization1));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/security/organization/101")
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
                .post("/security/organization/delete/101")
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
		when(organizationRepository.findById(Mockito.any())).thenReturn(Optional.of(organization1));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/security/organization/update")
                .accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(organization1))
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
		when(organizationRepository.findById(Mockito.any())).thenReturn(Optional.of(organization1));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/security/organization/delete")
                .accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(organization1))
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
