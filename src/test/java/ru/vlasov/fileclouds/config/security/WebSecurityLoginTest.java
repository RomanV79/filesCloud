package ru.vlasov.fileclouds.config.security;


import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.vlasov.fileclouds.web.controllers.LoginController;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@WebMvcTest(LoginController.class)
@Import(TestWebSecurityConfiguration.class)
class WebSecurityLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void login_page_shouldSucceedWith200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/login").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void home_page_shouldRedirectWith3xx() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/home").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection());
    }


}