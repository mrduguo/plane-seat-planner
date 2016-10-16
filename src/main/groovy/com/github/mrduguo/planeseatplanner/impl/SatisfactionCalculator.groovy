package com.github.mrduguo.planeseatplanner.impl

import com.github.mrduguo.planeseatplanner.model.ScheduledFlight
import com.github.mrduguo.planeseatplanner.model.Traveller
import com.github.mrduguo.planeseatplanner.model.TravellerGroup
import groovy.transform.CompileStatic


@CompileStatic
class SatisfactionCalculator {

    static int calculateSatisfaction(ScheduledFlight scheduledFlight) {
        int totalPassengers = countTotalTravellersIncludeOverSubscribed(scheduledFlight)
        int unhappyPassengers = countUnhappyTravellers(scheduledFlight)

        calculateHappyTravellerPercent(totalPassengers, unhappyPassengers)
    }

    private static int calculateHappyTravellerPercent(int totalPassengers, int unhappyPassengers) {
        ((totalPassengers - unhappyPassengers) * 100 / totalPassengers).intValue()
    }

    private static int countTotalTravellersIncludeOverSubscribed(ScheduledFlight scheduledFlight) {
        (Integer) scheduledFlight.travellerGroups.sum { TravellerGroup group -> group.travellers.size() }
    }

    private static int countUnhappyTravellers(ScheduledFlight scheduledFlight) {
        int unhappyPassengers = 0
        scheduledFlight.travellerGroups.each { TravellerGroup travellerGroup ->
            travellerGroup.travellers.each { Traveller traveller ->
                if (isTravellerUnhappy(traveller)) {
                    unhappyPassengers++
                }
            }
        }
        unhappyPassengers
    }

    private static boolean isTravellerUnhappy(Traveller traveller) {
        traveller.removedFromPlane ||
                traveller.separatedFromGroup ||
                (traveller.preferWindowSeat && !traveller.gotWindowSeat)
    }

}
