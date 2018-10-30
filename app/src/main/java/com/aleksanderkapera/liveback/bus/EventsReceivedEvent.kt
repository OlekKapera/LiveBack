package com.aleksanderkapera.liveback.bus

import com.aleksanderkapera.liveback.model.Event

/**
 * Created by kapera on 03-Jul-18.
 */
data class EventsReceivedEvent(val events: List<Event>,
                               val loadMore: Boolean)