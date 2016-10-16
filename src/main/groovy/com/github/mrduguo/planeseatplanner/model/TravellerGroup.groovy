package com.github.mrduguo.planeseatplanner.model

import groovy.transform.CompileStatic

@CompileStatic
class TravellerGroup {
    int groupId
    List<Traveller> travellers = []

    // data structures used for processing
    int groupOwnedSeats=0
    int groupOwnedWindowSeats=0
    int groupDemmandWindowSeats=0
    List<SegmentShape> groupSeatsShape = []
}
