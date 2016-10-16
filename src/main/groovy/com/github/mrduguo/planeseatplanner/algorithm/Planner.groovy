package com.github.mrduguo.planeseatplanner.algorithm

import com.github.mrduguo.planeseatplanner.model.ScheduledFlight
import com.github.mrduguo.planeseatplanner.model.SeatPlan
import com.github.mrduguo.planeseatplanner.model.Traveller
import com.github.mrduguo.planeseatplanner.model.TravellerGroup
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

import static com.github.mrduguo.planeseatplanner.algorithm.OverSubscribeHandler.handleOverSubscribe
import static com.github.mrduguo.planeseatplanner.algorithm.SatisfactionCalculator.calculateSatisfaction
import static com.github.mrduguo.planeseatplanner.model.TravellerGroup.SegmentShape


@CompileStatic
@Component
class Planner {
    SeatPlan plan(ScheduledFlight scheduledFlight) {
        scheduledFlight.segments = breakDownGroupIntoSegments(scheduledFlight)
        handleOverSubscribe(scheduledFlight)
        allocateSeats(scheduledFlight)


        def travellerSeats = generateNaturalOrderBasedSeats(scheduledFlight)
        def satisfaction = calculateSatisfaction(scheduledFlight)
        new SeatPlan(travellerSeats: travellerSeats, satisfaction: satisfaction)
    }

    List breakDownGroupIntoSegments(ScheduledFlight scheduledFlight) {
        scheduledFlight.
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
    }

    List<List> sortGroupBySizeReversely(List<List<Traveller>> groups) {
        groups.sort { List<Traveller> group ->
            int sortFactor = group.size() * 1000
            if (group.find { it.preferWindowSeat }) {
                // same group size with prefer window seat traveller will rank higher at the end
                sortFactor++
            }
            sortFactor
        }.reverse()
    }


    List<List<Traveller>> allocateSeats(ScheduledFlight scheduledFlight) {
        scheduledFlight.segments = sortGroupBySizeReversely(scheduledFlight.segments)

        List<List<Traveller>> travellerSeats = (1..scheduledFlight.rowsInPlane).collect {
            (1..scheduledFlight.seatsPerRow).collect { null }
        }.asType(List.class)

        List<List<Traveller>> groupsNeedToBreakDown = []
        scheduledFlight.segments.each { List<Traveller> group ->
            allocateSeatForGroup(scheduledFlight, travellerSeats, group, groupsNeedToBreakDown)
        }

        // we don't care break down group seat together for this algorithm as it not affect satisfaction
        groupsNeedToBreakDown.each { List<Traveller> group ->
            group.each { Traveller traveller ->
                traveller.separatedFromGroup = true
                allocateSeatForGroup(scheduledFlight, travellerSeats, [traveller], null)
            }
        }

        travellerSeats
    }

    void allocateSeatForGroup(
            ScheduledFlight scheduledFlight,
            List<List<Traveller>> travellerSeats,
            List<Traveller> group,
            List<List<Traveller>> groupsNeedToBreakDown
    ) {
        if (!allocateGroupWithWindowSeatPreference(scheduledFlight, travellerSeats, group, true)) {
            if (!allocateGroupWithWindowSeatPreference(scheduledFlight, travellerSeats, group, false)) {
                groupsNeedToBreakDown << group
            }
        }
    }

    List<List<Traveller>> generateNaturalOrderBasedSeats(ScheduledFlight scheduledFlight) {
        List<List<Traveller>> travellerSeats = (1..scheduledFlight.rowsInPlane).collect {
            (1..scheduledFlight.seatsPerRow).collect { null }
        }.asType(List.class)

        scheduledFlight.travellerGroups.each { TravellerGroup travellerGroup ->
            for (int i = 0; i < travellerGroup.groupOwnedWindowSeats; i++) {
                for (Traveller traveller : travellerGroup.travellers) {
                    if (traveller.preferWindowSeat && !traveller.removedFromPlane && !traveller.gotWindowSeat) {
                        traveller.gotWindowSeat = true
                        break
                    }
                }
            }
            while (travellerGroup.groupSeatsShape) {
                List result = findFirstAvaliableShape(scheduledFlight, travellerSeats, travellerGroup.groupSeatsShape)
                int row = (Integer) result[0]
                int column = (Integer) result[1]
                SegmentShape segmentShape = (SegmentShape) result[2]
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
        }
        travellerSeats
    }

    List findFirstAvaliableShape(ScheduledFlight scheduledFlight, List<List<Traveller>> travellerSeats, List<SegmentShape> segmentShapes) {
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

    def boolean allocateGroupWithWindowSeatPreference(
            ScheduledFlight scheduledFlight,
            List<List<Traveller>> travellerSeats,
            List<Traveller> group,
            boolean respectWindowSeat
    ) {
        for (List<Traveller> row : travellerSeats) {
            def seatsSum = row.sum { it != null ? 0 : 1 }
            int rowRemainSeats = (Integer) seatsSum
            if (rowRemainSeats == scheduledFlight.seatsPerRow) {
                putGroupIntoRow(group, row)
                return true
            } else {
                if (rowRemainSeats >= group.size()) {
                    if (respectWindowSeat) {
                        boolean thisGroupHasTravellerPreferWindow = group.find { it.preferWindowSeat } != null
                        if (!thisGroupHasTravellerPreferWindow || isRowStillHasWindowSeat(row)) {
                            putGroupIntoRow(group, row)
                            return true
                        }
                    } else {
                        putGroupIntoRow(group, row)
                        return true
                    }
                }
            }
        }
        return false
    }

    boolean isRowStillHasWindowSeat(List<Traveller> row) {
        def groupIdsPreferWindowSeat = new HashSet<Integer>()
        row.each { Traveller traveller ->
            if (traveller?.preferWindowSeat) {
                groupIdsPreferWindowSeat.add(traveller.travellerGroup.groupId)
            }
        }
        groupIdsPreferWindowSeat.size() < 2
    }

    void putGroupIntoRow(List<Traveller> segment, List<Traveller> row) {
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
