package com.github.mrduguo.planeseatplanner

import com.github.mrduguo.planeseatplanner.model.ScheduledFlight
import com.github.mrduguo.planeseatplanner.model.SeatPlan
import groovy.transform.CompileStatic

@CompileStatic
interface Planner {
    SeatPlan plan(ScheduledFlight scheduledFlight)
}
