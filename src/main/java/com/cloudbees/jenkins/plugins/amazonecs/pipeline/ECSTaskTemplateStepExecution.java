package com.cloudbees.jenkins.plugins.amazonecs.pipeline;

import jenkins.model.Jenkins;
import hudson.slaves.Cloud;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import javax.annotation.Nonnull;
import com.cloudbees.jenkins.plugins.amazonecs.ECSCloud;
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate;
import com.cloudbees.jenkins.plugins.amazonecs.pipeline.ECSTaskTemplateAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.RandomStringUtils;



//public class ECSTaskTemplateStepExecution extends SynchronousNonBlockingStepExecution<String> {
public class ECSTaskTemplateStepExecution extends AbstractStepExecutionImpl {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ECSTaskTemplateStepExecution.class.getName());

    private static final transient String NAME_FORMAT = "%s-%s";

    private final transient ECSTaskTemplateStep step;
    private final @Nonnull String cloud;
    private final @Nonnull String label;

    ECSTaskTemplateStepExecution(ECSTaskTemplateStep ecsTaskTemplateStep, StepContext context) {
        super(context);
        this.step = ecsTaskTemplateStep;
        this.cloud = ecsTaskTemplateStep.getCloud();
        this.label = ecsTaskTemplateStep.getLabel();
    }

    @Override
    //public String run() throws Exception {
    public boolean start() throws Exception {
        LOGGER.log(Level.INFO, "In ECSTaskTemplateExecution run()");
        LOGGER.log(Level.INFO, "cloud: {0}", this.cloud);
        LOGGER.log(Level.INFO, "label: {0}", label);
        //Run<?, ?> run = getContext().get(Run.class);
        //ECSTaskTemplateAction taskTemplateAction = run.getAction(ECSTaskTemplateAction.class);
        String randString = RandomStringUtils.random(5, "bcdfghjklmnpqrstvwxz0123456789");
        String name = String.format(NAME_FORMAT, step.getName(), randString);
        Cloud cloud = Jenkins.get().getCloud(this.cloud);
        ECSCloud ecsCloud = (ECSCloud) cloud;
        ECSTaskTemplate template = new ECSTaskTemplate(name,
                                                       label,
                                                       null,
                                                       step.getImage(),
                                                       null,
                                                       null,
                                                       step.getMemory(),
                                                       0,
                                                       step.getCpu(),
                                                       null,
                                                       null,
                                                       false,
                                                       false,
                                                       null,
                                                       null,
                                                       null,
                                                       null,
                                                       null,
                                                       null);

        template.setTaskrole(step.getTaskRoleArn());
        ecsCloud.registerTemplate(template);
        getContext().newBodyInvoker().withContext(step).withCallback(new ECSTaskTemplateCallback(template)).start();
        //ECSTaskTemplateAction.push(run, name);
        //return "myname";
        return false;
    }

    @Override
    public void stop(Throwable cause) throws Exception {
        super.stop(cause);
        //new ECSTaskTemplateAction(getContext().get(Run.class)).pop();
    }

    private class ECSTaskTemplateCallback extends BodyExecutionCallback.TailCall {

        private static final long serialVersionUID = 6043919968776851324L;

        private final ECSTaskTemplate taskTemplate;

        private ECSTaskTemplateCallback(ECSTaskTemplate taskTemplate) {
            this.taskTemplate = taskTemplate;
        }

        @Override
        /**
         * Remove the template after step is done
         */
        protected void finished(StepContext context) throws Exception {
            Cloud c = Jenkins.get().getCloud(cloud);
            if (c == null) {
                LOGGER.log(Level.WARNING, "Cloud {0} no longer exists, cannot delete task template {1}",
                        new Object[] { cloud, taskTemplate.getTemplateName() });
                return;
            }
            if (c instanceof ECSCloud) {
                LOGGER.log(Level.INFO, "Removing task template {1} from cloud {0}",
                        new Object[] { c.name, taskTemplate.getTemplateName() });
                ECSCloud ecsCloud = (ECSCloud) c;
                ecsCloud.removeTemplate(taskTemplate);
            } else {
                LOGGER.log(Level.WARNING, "Cloud is not an ECSCloud: {0} {1}",
                        new String[] { c.name, c.getClass().getName() });
            }
        }
    }

}
