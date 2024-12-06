package CS4337.Project;

import CS4337.Project.Shared.Security.AuthUtils;
import org.springframework.stereotype.Service;

@Service
public class RatingService {
  public boolean isUserRatingOwner(int ratingOwnerId) {
    int userId = AuthUtils.getUserId();
    return userId == ratingOwnerId;
  }
}
