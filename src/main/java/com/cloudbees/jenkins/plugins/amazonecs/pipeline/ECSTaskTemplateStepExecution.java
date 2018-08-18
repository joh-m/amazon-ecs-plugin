package com.cloudbees.jenkins.plugins.amazonecs.pipeline;

import jenkins.model.Jenkins;
import hudson.slaves.Cloud;
import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import javax.annotation.Nonnull;
import com.cloudbees.jenkins.plugins.amazonecs.ECSCloud;
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.RandomStringUtils;


public class ECSTaskTemplateStepExecution extends AbstractStepExecutionImpl {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ECSTaskTemplateStepExecution.class.getName());

    private static final transient String NAME_FORMAT = "%s-%s";

    private final transient ECSTaskTemplateStep step;
    private final @Nonnull String cloud;
    private final @Nonnull String label;

    private ECSTaskTemplate newTemplate = null;

    ECSTaskTemplateStepExecution(ECSTaskTemplateStep ecsTaskTemplateStep, StepContext context) {
        super(context);
        this.step = ecsTaskTemplateStep;
        this.cloud = ecsTaskTemplateStep.getCloud();
        this.label = ecsTaskTemplateStep.getLabel();
    }

    @Override
    public boolean start() throws Exception {
        LOGGER.log(Level.INFO, "In ECSTaskTemplateExecution run()");
        LOGGER.log(Level.INFO, "cloud: {0}", this.cloud);
        LOGGER.log(Level.INFO, "label: {0}", label);
        String randString = RandomStringUtils.random(5, "bcdfghjklmnpqrstvwxz0123456789");
        String name = String.format(NAME_FORMAT, step.getName(), randString);

        Cloud cloud = Jenkins.getInstance().getCloud(this.cloud);
        ECSCloud ecsCloud = (ECSCloud) cloud;
        newTemplate = new ECSTaskTemplate(name,  // Template name
                                          label, // Node label
                                          null,  // Task definition override
                                          step.getImage(), // Image name
                                          null,  // Launch type
                                          null,  // Remote FS root
                                          step.getMemory(), // Memory
                                          0,     // Memory reservation
                                          step.getCpu(),  // CPU
                                          null,  // Subnets
                                          null,  // Security groups
                                          false, // Assign public IP
                                          false, // Privileged
                                          null,  // Container user
                                          null,  // Log driver options
                                          null,  // Environments
                                          null,  // Extra host entries
                                          null,  // Mount points
                                          null); // Port mappings

        newTemplate.setTaskrole(step.getTaskRoleArn());
        ecsCloud.registerTemplate(newTemplate);
        getContext().newBodyInvoker().withContext(step).withCallback(new ECSTaskTemplateCallback(newTemplate)).start();
        return false;
    }

    @Override
    public void stop(Throwable cause) throws Exception {
        super.stop(cause);
    }

    /**
     * Re-inject the dynamic template when resuming the pipeline
     */
    @Override
    public void onResume() {
        super.onResume();
        Cloud c = Jenkins.getInstance().getCloud(cloud);
        if (c == null) {
            throw new RuntimeException(String.format("Cloud does not exist: %s", cloud));
        }
        if (!(c instanceof ECSCloud)) {
            throw new RuntimeException(String.format("Cloud is not a Kubernetes cloud: %s (%s)", cloud,
                    cloud.getClass().getName()));
        }
        ECSCloud ecsCloud = (ECSCloud) c;
        ecsCloud.registerTemplate(newTemplate);
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
            Cloud c = Jenkins.getInstance().getCloud(cloud);
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
