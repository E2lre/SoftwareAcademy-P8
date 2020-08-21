package tourGuide.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserPreferenceEmptyException extends Exception {
    private Logger logger = LoggerFactory.getLogger(UserPreferenceEmptyException.class);

    public UserPreferenceEmptyException(String s) {
        super(s);
        logger.error(s);
    }
}
