package com.github.mrduguo.planeseatplanner.model

import spock.lang.Specification

class ScheduledFlightParseSpec extends Specification {


    void "none exist input file should throw file not fond exception"() {
        given:
        String inputFilePath = "file-not-exist.txt"

        when:
        ScheduledFlight.parse(inputFilePath)

        then:
        FileNotFoundException ex = thrown()
        ex.message.contains('file-not-exist.txt')
    }


    void "invalid plane dimensions with string should throw illegal argument exception with reason"() {
        given:
        String inputFilePath = "build/resources/test/data/invalid/plane-dimensions-not-number/input-file.txt"

        when:
        ScheduledFlight.parse(inputFilePath)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Invalid plane dimensions defined 'seatsPerRow_is_not_a_number 4' at line 1"
    }


    void "invalid plane dimensions with drone should throw illegal argument exception with reason"() {
        given:
        String inputFilePath = "build/resources/test/data/invalid/plane-dimensions-drone/input-file.txt"

        when:
        ScheduledFlight.parse(inputFilePath)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Invalid plane dimensions defined '2 0' at line 1"
        ex.cause.message.contains('No drone is allowed for traveller')
    }


    void "invalid traveller should throw illegal argument exception with reason"() {
        given:
        String inputFilePath = "build/resources/test/data/invalid/traveller/input-file.txt"

        when:
        ScheduledFlight.parse(inputFilePath)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Invalid traveller defined 'traveler_id_is_not_a_number' at line 7"
    }


}