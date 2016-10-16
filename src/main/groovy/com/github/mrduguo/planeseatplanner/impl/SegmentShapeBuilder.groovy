package com.github.mrduguo.planeseatplanner.impl

import com.github.mrduguo.planeseatplanner.model.ScheduledFlight
import com.github.mrduguo.planeseatplanner.model.SegmentShape
import com.github.mrduguo.planeseatplanner.model.Traveller
import groovy.transform.CompileStatic

import static com.github.mrduguo.planeseatplanner.impl.SeatAllocator.generateEmptyPlaneSeats

@CompileStatic
class SegmentShapeBuilder {

    static void buildSegmentShape(ScheduledFlight scheduledFlight) {
        scheduledFlight.segments = sortSegmentsBySizeReversely(scheduledFlight.segments)
        List<List<Traveller>> travellerSeats = generateEmptyPlaneSeats(scheduledFlight)

        List<List<Traveller>> segmentsNeedToBreakDown = []
        scheduledFlight.segments.each { List<Traveller> group ->
            allocateSeatForSegments(scheduledFlight, travellerSeats, group, segmentsNeedToBreakDown)
        }

        // don't care break down group seat together for this algorithm as it not affect satisfaction
        segmentsNeedToBreakDown.each { List<Traveller> segment ->
            segment.each { Traveller traveller ->
                traveller.separatedFromGroup = true
                allocateSeatForSegments(scheduledFlight, travellerSeats, [traveller], null)
            }
        }

    }

    private static List<List> sortSegmentsBySizeReversely(List<List<Traveller>> segments) {
        segments.sort { List<Traveller> segment ->
            int sortFactor = segment.size() * 1000
            if (segment.find { it.preferWindowSeat }) {
                // same segment size with prefer window seat traveller will rank higher at the end
                sortFactor++
            }
            sortFactor
        }.reverse()
    }


    private static void allocateSeatForSegments(
            ScheduledFlight scheduledFlight,
            List<List<Traveller>> travellerSeats,
            List<Traveller> segment,
            List<List<Traveller>> segmentsNeedToBreakDown
    ) {
        if (!allocateSegmentWithWindowSeatPreference(scheduledFlight, travellerSeats, segment, true)) {
            if (!allocateSegmentWithWindowSeatPreference(scheduledFlight, travellerSeats, segment, false)) {
                segmentsNeedToBreakDown << segment
            }
        }
    }

    private static def boolean allocateSegmentWithWindowSeatPreference(
            ScheduledFlight scheduledFlight,
            List<List<Traveller>> travellerSeats,
            List<Traveller> segment,
            boolean respectWindowSeat
    ) {
        for (List<Traveller> row : travellerSeats) {
            def seatsSum = row.sum { it != null ? 0 : 1 }
            int rowRemainSeats = (Integer) seatsSum
            if (rowRemainSeats == scheduledFlight.seatsPerRow) {
                putSegmentIntoRow(segment, row)
                return true
            } else {
                if (rowRemainSeats >= segment.size()) {
                    if (respectWindowSeat) {
                        boolean thisSegmentHasTravellerPreferWindow = segment.find { it.preferWindowSeat } != null
                        if (!thisSegmentHasTravellerPreferWindow || isRowStillHasWindowSeat(row)) {
                            putSegmentIntoRow(segment, row)
                            return true
                        }
                    } else {
                        putSegmentIntoRow(segment, row)
                        return true
                    }
                }
            }
        }
        return false
    }

    private static boolean isRowStillHasWindowSeat(List<Traveller> row) {
        def groupIdsPreferWindowSeat = new HashSet<Integer>()
        row.each { Traveller traveller ->
            if (traveller?.preferWindowSeat) {
                groupIdsPreferWindowSeat.add(traveller.travellerGroup.groupId)
            }
        }
        groupIdsPreferWindowSeat.size() < 2
    }

    private static void putSegmentIntoRow(List<Traveller> segment, List<Traveller> row) {
        for (int i = 0; i < row.size(); i++) {
            if (!row[i]) {
                int emptySeatStartIndex = i
                def travellerGroup = segment.first().travellerGroup
                boolean gotWindowSeat = false
                if (emptySeatStartIndex == 0) {
                    travellerGroup.groupOwnedWindowSeats++
                    gotWindowSeat = true
                }
                if (emptySeatStartIndex + segment.size() == row.size()) {
                    travellerGroup.groupOwnedWindowSeats++
                    gotWindowSeat = true
                }
                def segmentShape = new SegmentShape()
                segmentShape.length = segment.size()
                segmentShape.hasWindowSeat = gotWindowSeat
                travellerGroup.groupSeatsShape << segmentShape
                segment.each { Traveller traveller ->
                    row.set(emptySeatStartIndex, traveller)
                    emptySeatStartIndex++
                    if (traveller.preferWindowSeat) {
                        travellerGroup.groupDemmandWindowSeats++
                    }
                }
                return
            }
        }
    }

}
