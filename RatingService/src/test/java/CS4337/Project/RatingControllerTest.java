package CS4337.Project;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RatingController.class)
public class RatingControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private RatingRepository ratingRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();

  //  @Test
  //  public void testAddRating() throws Exception {
  //    Map<String, Object> payload =
  //        Map.of(
  //            "shopid", 1,
  //            "userid", 1,
  //            "message", "Great service!",
  //            "rating", 5);
  //
  //    when(ratingRepository.addRating(1, 1, "Great service!", 5)).thenReturn(1);
  //    when(ratingRepository.checkRating(1, 1)).thenReturn(List.of());
  //
  //    mockMvc
  //        .perform(
  //            post("/ratings/add")
  //                .contentType(MediaType.APPLICATION_JSON)
  //                .content(objectMapper.writeValueAsString(payload)))
  //        .andExpect(status().isOk())
  //        .andExpect(jsonPath("$.result").value("Rating added successfully"));
  //  }
  //
  //  @Test
  //  public void testUpdateRating() throws Exception {
  //    Map<String, Object> payload = Map.of("message", "Updated review", "rating", 4);
  //
  //    when(ratingRepository.updateRating(1, "Updated review", 4)).thenReturn(1);
  //
  //    mockMvc
  //        .perform(
  //            put("/ratings/update/1")
  //                .contentType(MediaType.APPLICATION_JSON)
  //                .content(objectMapper.writeValueAsString(payload)))
  //        .andExpect(status().isOk())
  //        .andExpect(jsonPath("$.result").value("Rating updated successfully"));
  //  }
  //
  //  @Test
  //  public void testDeleteRating() throws Exception {
  //    when(ratingRepository.deleteRating(1)).thenReturn(1);
  //
  //    mockMvc
  //        .perform(delete("/ratings/delete/1"))
  //        .andExpect(status().isOk())
  //        .andExpect(jsonPath("$.result").value("Rating deleted successfully"));
  //  }
  //
  //  @Test
  //  public void testGetRatingsByShopId() throws Exception {
  //    Rating rating = new Rating();
  //    rating.setId(1);
  //    rating.setShopid(1);
  //    rating.setUserid(1);
  //    rating.setMessage("Great service!");
  //    rating.setRating(5);
  //
  //    when(ratingRepository.getRatingByShopId(1)).thenReturn(List.of(rating));
  //
  //    mockMvc
  //        .perform(get("/ratings/shop/1"))
  //        .andExpect(status().isOk())
  //        .andExpect(jsonPath("$[0].id").value(1))
  //        .andExpect(jsonPath("$[0].shopid").value(1))
  //        .andExpect(jsonPath("$[0].userid").value(1))
  //        .andExpect(jsonPath("$[0].message").value("Great service!"))
  //        .andExpect(jsonPath("$[0].rating").value(5));
  //  }
}
