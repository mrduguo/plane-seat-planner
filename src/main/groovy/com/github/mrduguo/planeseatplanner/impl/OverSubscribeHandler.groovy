package com.github.mrduguo.planeseatplanner.impl

import com.github.mrduguo.planeseatplanner.model.ScheduledFlight
import com.github.mrduguo.planeseatplanner.model.Traveller
import com.github.mrduguo.planeseatplanner.model.TravellerGroup
import groovy.transform.CompileStatic

@CompileStatic
class OverSubscribeHandler {

    static void handleOverSubscribe(ScheduledFlight scheduledFlight) {
        populateGroupOwnedSeats(scheduledFlight)

        int numberOfTravellerToRemove = calculateNumberOfTravellerToRemove(scheduledFlight)
        if (numberOfTravellerToRemove > 0) {
            (1..numberOfTravellerToRemove).each {
                removeTravellerFromBottom(scheduledFlight)
            }
        }
    }

    private static void populateGroupOwnedSeats(ScheduledFlight scheduledFlight) {
        scheduledFlight.travellerGroups.each { TravellerGroup travellerGroup ->
            travellerGroup.groupOwnedSeats = travellerGroup.travellers.size()
        }
    }

    private static int calculateNumberOfTravellerToRemove(ScheduledFlight scheduledFlight) {
        int totalSeats = scheduledFlight.seatsPerRow * scheduledFlight.rowsInPlane
        int totalTravellers = (Integer) scheduledFlight.segments.sum { List group -> group.size() }
        int numberOfTravellerToRemove = totalTravellers - totalSeats
        numberOfTravellerToRemove
    }

    private static void removeTravellerFromBottom(ScheduledFlight scheduledFlight) {
        def lastGroup = scheduledFlight.segments.last()
        Traveller removedTraveller = lastGroup.pop()
        removedTraveller.travellerGroup.groupOwnedSeats--
        if (removedTraveller.preferWindowSeat) {
            removedTraveller.travellerGroup.groupDemmandWindowSeats--
        }
        removedTraveller.removedFromPlane = true
        if (!lastGroup) {
            scheduledFlight.segments.pop()
        }
    }

}
