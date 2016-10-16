package com.github.mrduguo.planeseatplanner.algorithm

import com.github.mrduguo.planeseatplanner.model.ScheduledFlight
import com.github.mrduguo.planeseatplanner.model.Traveller
import com.github.mrduguo.planeseatplanner.model.TravellerGroup
import groovy.transform.CompileStatic

@CompileStatic
class OverSubscribeHandler {

    static void handleOverSubscribe(ScheduledFlight scheduledFlight) {
        int totalSeats = scheduledFlight.seatsPerRow * scheduledFlight.rowsInPlane
        int totalTravellers = (Integer) scheduledFlight.segments.sum { List group -> group.size() }
        int numberOfTravellerToRemove = totalTravellers - totalSeats
        scheduledFlight.travellerGroups.each { TravellerGroup travellerGroup ->
            travellerGroup.groupOwnedSeats = travellerGroup.travellers.size()
        }
        if (numberOfTravellerToRemove > 0) {
            (1..numberOfTravellerToRemove).each {
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
    }

}
