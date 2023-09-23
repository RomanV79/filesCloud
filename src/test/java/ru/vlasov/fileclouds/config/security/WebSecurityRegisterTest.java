package ru.vlasov.fileclouds.config.security;


import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.vlasov.fileclouds.service.AppUserServiceImpl;
import ru.vlasov.fileclouds.service.StorageService;
import ru.vlasov.fileclouds.web.controllers.RegisterController;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@WebMvcTest(RegisterController.class)
@Import(TestWebSecurityConfiguration.class)
class WebSecurityRegisterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AppUserServiceImpl userService;

    @MockBean
    StorageService storageService;

    @WithMockUser(value = "spring")
    @Test
    public void register_page_shouldSucceedWith200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/register").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}