package event.sequencing;

import java.time.Instant;

public interface Sequenceable {
	public void execute(Instant t,Object... args);
}
