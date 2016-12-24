package actor4j.testing;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.Map.Entry;
import java.util.Set;

import actor4j.core.ActorCell;
import actor4j.core.ActorSystem;
import actor4j.core.DefaultActorSystemImpl;
import actor4j.core.actors.Actor;
import actor4j.core.actors.PseudoActor;
import actor4j.core.messages.ActorMessage;
import bdd4j.Story;

public class TestSystemImpl extends DefaultActorSystemImpl  {
	protected PseudoActor pseudoActor;
	protected volatile CompletableFuture<ActorMessage<?>> actualMessage;
	
	public TestSystemImpl(ActorSystem wrapper) {
		this(null, wrapper);
	}

	public TestSystemImpl(String name, ActorSystem wrapper) {
		super(name, wrapper);
	}
	
	public ActorCell underlyingCell(UUID id) {
		return getCells().get(id);
	}
	
	public Actor underlyingActor(UUID id) {
		ActorCell cell = getCells().get(id);
		return (cell!=null)? cell.getActor() : null;
	}
	
	public void testActor(UUID id) {
		Actor actor = underlyingActor(id);
		if (actor!=null && actor instanceof ActorTest) {
			redirectToPseudoActor(actor.getId());
			List<Story> list = ((ActorTest)actor).test();
			if (list!=null)
				for (Story story : list)
					story.run();
			clearRedirections();
		}
	}
	
	public void testAllActors() {
		Iterator<Entry<UUID, ActorCell>> iterator = getCells().entrySet().iterator();
		while (iterator.hasNext()) {
			ActorCell cell = iterator.next().getValue();
			if (cell.getActor() instanceof ActorTest) {
				redirectToPseudoActor(cell.getId());
				List<Story> list = ((ActorTest)cell.getActor()).test();
				if (list!=null)
					for (Story story : list)
						story.run();
				clearRedirections();
			}
		}
	}

	public Future<ActorMessage<?>> awaitMessage() {
		actualMessage = new CompletableFuture<>();
		
		Timer timer = new Timer();
		long timeout = 5000;
		long start = System.currentTimeMillis();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (pseudoActor.runOnce()) 
					cancel();
				
				if ((System.currentTimeMillis()-start)>=timeout) {
					actualMessage.completeExceptionally(new TimeoutException());
					cancel();
				}
			}
		}, 0, 25);
		
		return actualMessage;
	}
	
	public void redirectToPseudoActor(UUID id) {
		Set<UUID> cellIds = getCells().keySet();
		for (UUID cellId : cellIds)
			if (cellId!=id)
				addRedirection(cellId, pseudoActor.getId());
	}
}
