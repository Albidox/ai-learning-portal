package com.ailearningportal;

import com.ailearningportal.controller.HomeController;
import com.ailearningportal.controller.GlobalModelAdvice;
import com.ailearningportal.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeController.class)
@Import({SecurityConfig.class, GlobalModelAdvice.class})
class AiLearningPortalApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homePageShouldOpen() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AI Learning Portal")));
    }
}
