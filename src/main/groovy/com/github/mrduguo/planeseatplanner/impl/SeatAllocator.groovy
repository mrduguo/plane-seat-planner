package com.github.mrduguo.planeseatplanner.impl

import com.github.mrduguo.planeseatplanner.model.ScheduledFlight
import com.github.mrduguo.planeseatplanner.model.SegmentShape
import com.github.mrduguo.planeseatplanner.model.Traveller
import com.github.mrduguo.planeseatplanner.model.TravellerGroup
import groovy.transform.CompileStatic

@CompileStatic
class SeatAllocator {

    static List<List<Traveller>> generateNaturalOrderBasedSeats(ScheduledFlight scheduledFlight) {
        List<List<Traveller>> travellerSeats = generateEmptyPlaneSeats(scheduledFlight)

        scheduledFlight.travellerGroups.each { TravellerGroup travellerGroup ->
            markWindowSeatTravellers(travellerGroup)
            while (travellerGroup.groupSeatsShape) {
                List shapeInfo = findFirstAvailableShape(scheduledFlight, travellerSeats, travellerGroup.groupSeatsShape)
                fillInSegmentShape(scheduledFlight, travellerSeats, travellerGroup, shapeInfo)
            }
        }
        travellerSeats
    }

    static List<List<Traveller>> generateEmptyPlaneSeats(ScheduledFlight scheduledFlight) {
        (1..scheduledFlight.rowsInPlane).collect {
            (1..scheduledFlight.seatsPerRow).collect { null }
        }.asType(List.class)
    }

    private static void fillInSegmentShape(
            ScheduledFlight scheduledFlight,
            List<List<Traveller>> travellerSeats,
            TravellerGroup travellerGroup,
            List shapeInfo) {
        int row = (Integer) shapeInfo[0]
        int column = (Integer) shapeInfo[1]
        SegmentShape segmentShape = (SegmentShape) shapeInfo[2]
        for (int i = 0; i < segmentShape.length; i++) {
            boolean isWindowSeat = ((column == 0 && i == 0) || (i + column + 1 == scheduledFlight.seatsPerRow))
            if (isWindowSeat) {
                boolean shortOfWindowSeat = travellerGroup.groupDemmandWindowSeats >= travellerGroup.groupOwnedWindowSeats
                if (shortOfWindowSeat) {
                    for (Traveller traveller : travellerGroup.travellers) {
                        if (!traveller.seated && !traveller.removedFromPlane && traveller.gotWindowSeat) {
                            List<Traveller> rowSeats = travellerSeats.get(row)
                            rowSeats.set((i + column), traveller)
                            travellerGroup.groupDemmandWindowSeats--
                            travellerGroup.groupOwnedWindowSeats--
                            traveller.seated = true
                            break
                        }
                    }
                    continue
                } else {
                    travellerGroup.groupOwnedWindowSeats--
                }
            }
            for (Traveller traveller : travellerGroup.travellers) {
                if (!traveller.seated && !traveller.removedFromPlane && !traveller.gotWindowSeat) {
                    List<Traveller> rowSeats = travellerSeats.get(row)
                    rowSeats.set((i + column), traveller)
                    traveller.seated = true
                    break
                }
            }
        }
    }

    private static void markWindowSeatTravellers(TravellerGroup travellerGroup) {
        for (int i = 0; i < travellerGroup.groupOwnedWindowSeats; i++) {
            for (Traveller traveller : travellerGroup.travellers) {
                if (traveller.preferWindowSeat && !traveller.removedFromPlane && !traveller.gotWindowSeat) {
                    traveller.gotWindowSeat = true
                    break
                }
            }
        }
    }

    private static List findFirstAvailableShape(
            ScheduledFlight scheduledFlight,
            List<List<Traveller>> travellerSeats,
            List<SegmentShape> segmentShapes) {
        for (int row; row < travellerSeats.size(); row++) {
            int rowRemainSeats = 0
            boolean rowHasWindowSeats = false
            int columnStart = -1
            travellerSeats.get(row).eachWithIndex { Traveller traveller, int column ->
                if (!traveller) {
                    rowRemainSeats++
                    if (columnStart < 0) {
                        columnStart = column
                    }
                }
                if (column == 0 || column == scheduledFlight.seatsPerRow - 1) {
                    rowHasWindowSeats = true
                }
            }
            for (SegmentShape segmentShape : segmentShapes) {
                if (segmentShape.length <= rowRemainSeats) {
                    if (rowHasWindowSeats || !segmentShape.hasWindowSeat) {
                        segmentShapes.remove(segmentShape)
                        return [row, columnStart, segmentShape]
                    }
                }
            }
        }
        throw new UnknownError('robot suppose to not come here, please report a bug to the human')
    }

}
