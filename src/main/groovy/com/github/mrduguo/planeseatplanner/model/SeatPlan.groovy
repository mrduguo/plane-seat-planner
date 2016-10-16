package com.github.mrduguo.planeseatplanner.model

import groovy.transform.CompileStatic

@CompileStatic
class SeatPlan {
    List<List<Traveller>> travellerSeats
    int satisfaction

    @Override
    String toString() {
        def output = new StringBuilder()
        travellerSeats.each { List<Traveller> row ->
            row.eachWithIndex { Traveller traveller, int i ->
                if (i > 0) {
                    output.append(' ');
                }
                if (traveller != null) {
                    output.append(traveller.personId)
                } else {
                    output.append(0)
                }
            }
            output.append('\n')
        }
        output.append(satisfaction)
        output.append('%')
        output.toString()
    }
}
