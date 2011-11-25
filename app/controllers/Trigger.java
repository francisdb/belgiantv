package controllers;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;

public class Trigger extends Controller{

	@Before
	public static void log(){
		Logger.info("Trigger call:" + request.url);
	}

	public static void imdb() {
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add(withUrl(Router.reverse("Task.imdb").url)
				.taskName("imdb"+System.currentTimeMillis()));
		flash.success("Queued imdb update for background processing.");
		Application.index();
	}
	
	public static void tmdb() {
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add(withUrl(Router.reverse("Task.tmdb").url)
				.taskName("tmdb"+System.currentTimeMillis()));
		flash.success("Queued tmdb update for background processing.");
		Application.index();
	}

	public static void yelo() {
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add(withUrl(Router.reverse("Task.yelo").url)
				.taskName("yelo"+System.currentTimeMillis()));
		flash.success("Queued yelo update for background processing.");
		Application.index();
	}

	public static void clear() {
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add(withUrl(Router.reverse("Task.clear").url)
				.taskName("clear"+System.currentTimeMillis()));
		flash.success("Queued database clear for background processing.");
		Application.index();
	}

	public static void keepalive() {
		ok();
	}
}
