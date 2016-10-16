package com.github.mrduguo.planeseatplanner.impl

import com.github.mrduguo.planeseatplanner.model.ScheduledFlight
import com.github.mrduguo.planeseatplanner.model.Traveller
import com.github.mrduguo.planeseatplanner.model.TravellerGroup
import groovy.transform.CompileStatic

@CompileStatic
class SegmentBuilder {
    static void buildSegmentsFromGroups(ScheduledFlight scheduledFlight) {
        List<List> segments = scheduledFlight.
                travellerGroups.
                collect { it }.
                sort {
                    0 - it.travellers.size()
                }.
                collectMany { TravellerGroup group ->
                    if (group.travellers.size() <= scheduledFlight.seatsPerRow) {
                        // in same group, traveller prefer window seat rank higher
                        [group.travellers.collect { it }.sort { it.preferWindowSeat ? 0 : 1 }]
                    } else {
                        def totoalGroupNumberRaw = group.travellers.size() / scheduledFlight.seatsPerRow
                        def totalGroups = Math.ceil(totoalGroupNumberRaw)
                        boolean allRowFull = totalGroups == totoalGroupNumberRaw
                        def splittedSegments = (1..totalGroups).collect { [] }
                        def windowSeatCount = 0
                        def noWindowSeatTravlers = []
                        group.travellers.each { Traveller traveller ->
                            int currentWindowIndex = (windowSeatCount / 2).intValue()
                            if (traveller.preferWindowSeat) {
                                if (totalGroups - currentWindowIndex > 1) {
                                    splittedSegments[currentWindowIndex] << traveller
                                    windowSeatCount++
                                    return
                                } else if (totalGroups - currentWindowIndex == 1) {
                                    // last row, may not have two window seats
                                    splittedSegments[currentWindowIndex] << traveller
                                    windowSeatCount = allRowFull ? windowSeatCount + 1 : windowSeatCount + 2
                                    return
                                } else {
                                    noWindowSeatTravlers << traveller
                                }
                            } else {
                                noWindowSeatTravlers << traveller
                            }
                        }
                        splittedSegments.each { def splittedSegment ->
                            int remainSeats = scheduledFlight.seatsPerRow - splittedSegment.size()
                            splittedSegment.addAll(noWindowSeatTravlers.take(remainSeats))
                            noWindowSeatTravlers = noWindowSeatTravlers.drop(remainSeats)
                        }
                        splittedSegments
                    }
                }
        scheduledFlight.segments = (List) segments
    }
}
