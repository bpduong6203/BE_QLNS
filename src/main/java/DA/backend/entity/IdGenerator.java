package DA.backend.entity;

import java.time.Year;
import java.util.Random;

public class IdGenerator {
    private static final Random random = new Random();

    public  static String generateUniqueId(){
        int currentYear = Year.now().getValue()%100;
        int randomNumber = 10000000 + random.nextInt(99999999);
        return currentYear + String.valueOf(randomNumber);
    }

}
