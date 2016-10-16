package com.github.mrduguo.planeseatplanner

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
            try{
                println executePlan(args[0])
            }catch (FileNotFoundException ex){
                println "File ${args[0]} not found!!!"
            }
        } else {
            println 'Input data file is required as first parameter!!!'
        }
    }

    def executePlan(String inputFilePath) {
        def scheduledFlight = ScheduledFlight.parse(inputFilePath)
        planner.plan(scheduledFlight)
    }

}
