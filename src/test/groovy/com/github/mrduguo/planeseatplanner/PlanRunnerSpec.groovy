package com.github.mrduguo.planeseatplanner

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationConfiguration
import spock.lang.Specification

@SpringApplicationConfiguration(classes = PlannerRunner.class)
@IntegrationTest
class PlanRunnerSpec extends Specification {

    @Autowired
    PlannerRunner planRunner


    void "valid sample data should generate expected output"(String sampleDataName) {
        given:
        String inputFilePath = "build/resources/test/data/valid/$sampleDataName/input-file.txt"
        File expectedOutputFile = new File("build/resources/test/data/valid/$sampleDataName/expected-output.txt")

        when:
        String seatPlanOutput = planRunner.executePlan(inputFilePath)

        then:
        expectedOutputFile.text == seatPlanOutput

        where:
        sampleDataName                           | _
        'full-capacity-100-percent-satisfaction' | _
        'low-occupy-but-high-window-seat-demand' | _
        'over-subscribed'                        | _
        'large-group'                            | _
    }

}