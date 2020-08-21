package tourGuide.TU;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

//@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TourGuideControlerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TourGuideService tourGuideService;

    private User user;


    private void setUpEach() {
        user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
    }
    /*---------------------------------------- Get-------------------------------*/
    @Ignore
    @Test
    public void getUserPreference_existingUserName_UserPreferenceIsDone() {
        setUpEach();
       //GIVEN
        Mockito.when(tourGuideService.getUser(anyString())).thenReturn(user);

  //TODO : voir si pour le controleur, ce ne serait pas un test d'integ
    }

}
