package controllers;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withTaskName;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

public class Trigger extends Controller{

	@Before
	public static void log(){
		Logger.info("Trigger call:" + request.url);
	}

	public static void imdb() {
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add(withTaskName("imdb " + System.currentTimeMillis())
				.url(Router.reverse("Task.imdb").url)
				.method(Method.GET));
		flash.success("Queued imdb update for background processing.");
		Application.index();
	}
	
	public static void tmdb() {
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add(withTaskName("tmdb" + System.currentTimeMillis())
				.url(Router.reverse("Task.tmdb").url)
				.method(Method.GET));
		flash.success("Queued tmdb update for background processing.");
		Application.index();
	}

	public static void yelo() {
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add(withTaskName("yelo" + System.currentTimeMillis())
				.url(Router.reverse("Task.yelo").url)
				.method(Method.GET));
		flash.success("Queued yelo update for background processing.");
		Application.index();
	}

	public static void clear() {
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add(withTaskName("clear" + System.currentTimeMillis())
				.url(Router.reverse("Task.clear").url)
				.method(Method.GET));
		flash.success("Queued database clear for background processing.");
		Application.index();
	}
}
