package com.github.mrduguo.planeseatplanner.model

import groovy.transform.CompileStatic

@CompileStatic
class Traveller {
    int personId
    boolean preferWindowSeat
    TravellerGroup travellerGroup

    // data structures used for processing
    boolean seated =false
    boolean gotWindowSeat=false
    boolean removedFromPlane=false
    boolean separatedFromGroup =false
}
