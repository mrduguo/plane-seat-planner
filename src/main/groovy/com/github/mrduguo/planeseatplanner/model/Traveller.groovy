package com.github.mrduguo.planeseatplanner.model

import groovy.transform.CompileStatic

@CompileStatic
class Traveller {
    int personId
    boolean preferWindowSeat
    TravellerGroup travellerGroup

    boolean seated =false
    boolean gotWindowSeat=false
    boolean removedFromPlane=false
    boolean separatedFromGroup =false
}
