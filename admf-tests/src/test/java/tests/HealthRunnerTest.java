package tests;

import com.intuit.karate.junit5.Karate;

public class HealthRunnerTest {

    @Karate.Test
    Karate testAll() {
        return Karate.run("classpath:features").tags("~@degraded-seed", "~@degraded-verify");
    }
}
