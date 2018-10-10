package jobs;

import common.CodeManager;
import demo.Bar;
import demo.Foo;
import demo.Order;
import demo.User;
import org.rythmengine.logger.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import org.rythmengine.play.RythmPlugin;

/**
 * User: freewind
 * Date: 13-3-21
 * Time: 下午7:11
 */
@OnApplicationStart
public class InitJobs extends Job {

    /**
     * Here you do the job
     */
    @Override
    public void doJob() throws Exception {
        CodeManager.CODE_ROOT.mkdirs();
        CodeManager.reload();
//        loadModelClassesBeforeHand();
        //Code.initRythmEngine();
    }

    /**
     * Since I am not using this demo for implicitVariables, I have
     * commented out the code that calls this method.
     *
     * This method causes the following error
     *
     * Error during the 500 response generation
     * java.lang.NoSuchMethodError: play.Play.plugin(Ljava/lang/Class;)Ljava/lang/Object;
     * 	at org.rythmengine.play.ImplicitVariables$5.evaluate(ImplicitVariables.java:76)
     * 	at org.rythmengine.play.RythmPlugin$8.setRenderArgs(RythmPlugin.java:480)
     * 	at org.rythmengine.internal.EventBus$7.handleEvent(EventBus.java:239)
     * 	at org.rythmengine.internal.EventBus$7.handleEvent(EventBus.java:234)
     * 	at org.rythmengine.internal.EventBus.accept(EventBus.java:156)
     * 	at org.rythmengine.RythmEngine.accept(RythmEngine.java:1910)
     * 	at org.rythmengine.internal.RythmEvents.trigger(RythmEvents.java:138)
     * 	at org.rythmengine.template.TagBase.__triggerRenderEvent(TagBase.java:93)
     * 	at org.rythmengine.template.TemplateBase.render(TemplateBase.java:594)
     * 	at org.rythmengine.play.RythmTemplate.internalRender(RythmTemplate.java:151)
     * 	at play.templates.Template.render(Template.java:28)
     * 	at play.server.PlayHandler.serve500(PlayHandler.java:785)
     * 	at play.server.PlayHandler$NettyInvocation.run(PlayHandler.java:254)
     * 	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
     * 	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$201(ScheduledThreadPoolExecutor.java:180)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:293)
     * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
     * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     * 	at java.lang.Thread.run(Thread.java:748)
     */
    private void loadModelClassesBeforeHand() {
        new Bar();
        new Foo();
        new Order();
        new User();
        RythmPlugin.render("@args demo.Bar bar, demo.Foo foo, demo.Order order, demo.User user");
        Logger.info("model classes loaded");
    }

}
