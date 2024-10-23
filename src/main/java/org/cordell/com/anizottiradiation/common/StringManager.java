package org.cordell.com.anizottiradiation.common;

import java.util.Random;

public class StringManager {
    public static String getRandomNoiseMessage(int length) {
        var characters = "ABCDEFGHIJ#KL?.a!@#$%^&&*()_+-=OPQRST$UVWXYZab!cdefghi1jklmnopqrstuvwxyz";
        var noise = new StringBuilder();
        var random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            noise.append(characters.charAt(index));
        }

        return noise.toString();
    }
}
