package event.processing;

import event.events.Event;

import java.util.Comparator;

public class EventInstantComparator implements Comparator<Event> {
    @Override
    public int compare(Event e1, Event e2) {
        int comp = e1.getEventTimestamp().compareTo(e2.getEventTimestamp());

        if (comp == 0) {
            if (e1.getPriority() < e2.getPriority())
                return -1;
            else if (e1.getPriority() > e2.getPriority())
                return 1;
            else
                return 0;
        }
        return comp;
    }
}
