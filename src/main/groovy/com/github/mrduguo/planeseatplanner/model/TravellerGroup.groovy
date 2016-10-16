package com.github.mrduguo.planeseatplanner.model

import groovy.transform.CompileStatic

@CompileStatic
class TravellerGroup {
    int groupId
    List<Traveller> travellers = []

    // rest of them are internal processing data structures

    int groupOwnedSeats=0
    int groupOwnedWindowSeats=0
    int groupDemmandWindowSeats=0
    List<SegmentShape> groupSeatsShape = []

    static class SegmentShape implements Serializable {
        int length
        boolean hasWindowSeat
    }
}
