package com.synapse.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Event {
    private Integer eventId;
    private String eventName;
    private String eventDescription;   
    private String eventType;
    private LocalDateTime schedule;
    private BigDecimal prizeMoney;
    private Integer venueId;

    public Event() {}

    public Event(String name, String desc, String type, LocalDateTime schedule,
                 BigDecimal prize, Integer venueId) {
        this.eventName = name;
        this.eventDescription = desc;
        this.eventType = type;
        this.schedule = schedule;
        this.prizeMoney = prize;
        this.venueId = venueId;
    }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getEventDescription() { return eventDescription; }
    public void setEventDescription(String eventDescription) { this.eventDescription = eventDescription; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public LocalDateTime getSchedule() { return schedule; }
    public void setSchedule(LocalDateTime schedule) { this.schedule = schedule; }
    public BigDecimal getPrizeMoney() { return prizeMoney; }
    public void setPrizeMoney(BigDecimal prizeMoney) { this.prizeMoney = prizeMoney; }
    public Integer getVenueId() { return venueId; }
    public void setVenueId(Integer venueId) { this.venueId = venueId; }

    @Override
    public String toString() {
        return "Event{id=" + eventId + ", name='" + eventName + "', desc='" + eventDescription + 
               "', type='" + eventType + "', schedule=" + schedule +
               ", prize=" + prizeMoney + ", venue=" + venueId + "}";
    }
}
