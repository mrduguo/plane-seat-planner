package com.github.mrduguo.planeseatplanner

import com.github.mrduguo.planeseatplanner.algorithm.Planner
import com.github.mrduguo.planeseatplanner.model.ScheduledFlight
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@CompileStatic
@SpringBootApplication
public class PlannerRunner implements CommandLineRunner {

    @Autowired
    Planner planner

    public static void main(String... args) {
        SpringApplication.run(PlannerRunner.class, args)
    }

    @Override
    void run(String... args) {
        if (args) {
            println executePlan(args[0])
        } else {
            println 'Input data file is required as first parameter!!!'
        }
    }

    def executePlan(String inputFilePath) {
        planner.plan(ScheduledFlight.parse(inputFilePath))
    }

}
